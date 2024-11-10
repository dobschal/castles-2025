<template>
  <p>
    {{
      t("farmAction.farmOf", {
        playerName: buildingsStore.activeBuilding?.user.username,
      })
    }}
  </p>
  <CButton
    v-if="isOwnBuilding && unitAtPosition"
    class="small with-icon"
    @click="openMoveUnitActionOverlay"
  >
    {{
      t("villageAction.moveUnit", [
        movesPerHourLimit - movesLastHour,
        movesPerHourLimit,
      ])
    }}
    <BeerDisplay :beer="pricesStore.getMovePrice(unitAtPosition?.type)" />
  </CButton>
  <CButton v-if="isOwnBuilding" class="small with-icon" @click="destroy">
    {{ t("destroyBuilding.button") }}
  </CButton>
  <CButton class="small" @click="close">
    {{ t("general.close") }}
  </CButton>
</template>

<script lang="ts" setup>
import { computed, onBeforeUnmount, onMounted, ref } from "vue";
import { useMapStore } from "@/store/mapStore.ts";
import { useBuildingsStore } from "@/store/buildingsStore.ts";
import { handleFatalError } from "@/core/util.ts";
import CButton from "@/components/partials/general/CButton.vue";
import { useI18n } from "vue-i18n";
import { ACTION, DIALOG, MAP_TILE_CLICKED } from "@/events.ts";
import { useUnitsStore } from "@/store/unitsStore.ts";
import { useAuthStore } from "@/store/authStore.ts";
import UnitMoveAction from "@/components/partials/game/actions/UnitMoveAction.vue";
import BeerDisplay from "@/components/partials/game/BeerDisplay.vue";
import { usePricesStore } from "@/store/pricesStore.ts";
import { BuildingGateway } from "@/gateways/BuildingGateway.ts";
import { useEventsStore } from "@/store/eventsStore.ts";

const pricesStore = usePricesStore();
const mapStore = useMapStore();
const buildingsStore = useBuildingsStore();
const unitsStore = useUnitsStore();
const authStore = useAuthStore();
const eventsStore = useEventsStore();
const emit = defineEmits(["close-action"]);
const { t } = useI18n();
const zoomMapTileSizeBeforeAction = ref(100);

const unitAtPosition = computed(() => {
  return unitsStore.units.find((unit) => {
    return (
      unit.x === buildingsStore.activeBuilding!.x &&
      unit.y === buildingsStore.activeBuilding!.y
    );
  });
});

const movesLastHour = computed(() => {
  if (!unitAtPosition.value) return -1;

  return unitsStore.movesLastHour(unitAtPosition.value);
});

const movesPerHourLimit = computed(() => {
  if (!unitAtPosition.value) return -1;

  return unitsStore.movesPerHourLimit(unitAtPosition.value);
});

const isOwnBuilding = computed(() => {
  // Ignore case when both are undefined
  return buildingsStore.activeBuilding?.user.id === authStore.user?.id;
});

onMounted(() => {
  if (!buildingsStore.activeBuilding) {
    return handleFatalError(new Error("No active building set"));
  }

  // When clicking on the map, close the action overlay
  MAP_TILE_CLICKED.on(close);

  mapStore.mapControlsDisabled = true;
  zoomMapTileSizeBeforeAction.value = mapStore.mapTileSize;
  mapStore.mapTileSize = mapStore.findMaxZoomInMapTileSize();
  mapStore.goToPosition({
    x: buildingsStore.activeBuilding.x,
    y: buildingsStore.activeBuilding.y,
  });
});

onBeforeUnmount(() => {
  mapStore.mapControlsDisabled = false;
  mapStore.mapTileSize = zoomMapTileSizeBeforeAction.value;

  MAP_TILE_CLICKED.off(close);

  if (buildingsStore.activeBuilding) {
    mapStore.goToPosition({
      x: buildingsStore.activeBuilding.x,
      y: buildingsStore.activeBuilding.y,
    });
    buildingsStore.activeBuilding = undefined;
  }
});

async function destroy(): Promise<void> {
  DIALOG.dispatch({
    questionKey: "destroyBuilding.question",
    onYes: async () => {
      try {
        if (!buildingsStore.activeBuilding) return;
        await BuildingGateway.instance.destroyBuilding(
          buildingsStore.activeBuilding.x,
          buildingsStore.activeBuilding.y,
        );
        eventsStore.ownActionHappened = true;
        close();
      } catch (error) {
        handleFatalError(error);
      }
    },
  });
}

function close(): void {
  emit("close-action");
}

function openMoveUnitActionOverlay(): void {
  close();
  unitsStore.activeMoveUnit = unitAtPosition.value!;
  ACTION.dispatch(UnitMoveAction);
}
</script>

<style lang="scss" scoped></style>
