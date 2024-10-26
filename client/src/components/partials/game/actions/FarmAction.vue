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
    class="small"
    @click="openMoveUnitActionOverlay"
  >
    {{ t("villageAction.moveUnit") }}
  </CButton>
  <CButton class="small" @click="close">
    {{ t("general.close") }}
  </CButton>
</template>

<script lang="ts" setup>
import { computed, onBeforeUnmount, onMounted } from "vue";
import { useMapStore } from "@/store/mapStore.ts";
import { useBuildingsStore } from "@/store/buildingsStore.ts";
import { handleFatalError } from "@/core/util.ts";
import CButton from "@/components/partials/general/CButton.vue";
import { useI18n } from "vue-i18n";
import { ACTION, MAP_TILE_CLICKED } from "@/events.ts";
import { useUnitsStore } from "@/store/unitsStore.ts";
import { useAuthStore } from "@/store/authStore.ts";
import UnitMoveAction from "@/components/partials/game/actions/UnitMoveAction.vue";

const mapStore = useMapStore();
const buildingsStore = useBuildingsStore();
const unitsStore = useUnitsStore();
const authStore = useAuthStore();
const emit = defineEmits(["close-action"]);
const { t } = useI18n();

const unitAtPosition = computed(() => {
  return unitsStore.units.find((unit) => {
    return (
      unit.x === buildingsStore.activeBuilding!.x &&
      unit.y === buildingsStore.activeBuilding!.y
    );
  });
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
  mapStore.mapTileSize = 200;
  mapStore.goToPosition({
    x: buildingsStore.activeBuilding.x,
    y: buildingsStore.activeBuilding.y,
  });
});

onBeforeUnmount(() => {
  mapStore.mapControlsDisabled = false;
  mapStore.mapTileSize = 100;

  MAP_TILE_CLICKED.off(close);

  if (buildingsStore.activeBuilding) {
    mapStore.goToPosition({
      x: buildingsStore.activeBuilding.x,
      y: buildingsStore.activeBuilding.y,
    });
    buildingsStore.activeBuilding = undefined;
  }
});

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
