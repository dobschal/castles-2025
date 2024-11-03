<template>
  <p>
    {{
      t("castleAction.castleOf", {
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
  <CButton
    v-if="isOwnBuilding"
    class="small with-icon"
    @click="createUnit(UnitType.SWORDSMAN)"
    :disabled="!isBuildingSwordsmanAvailable"
  >
    {{ t("castleAction.createSwordsman") }}
    <BeerDisplay :beer="pricesStore.getCreationPrice(UnitType.SWORDSMAN)" />
  </CButton>
  <CButton
    v-if="isOwnBuilding"
    class="small with-icon"
    @click="createUnit(UnitType.HORSEMAN)"
    :disabled="!isBuildingHorsemanAvailable"
  >
    {{ t("castleAction.createHorseman") }}
    <BeerDisplay :beer="pricesStore.getCreationPrice(UnitType.HORSEMAN)" />
  </CButton>
  <CButton
    v-if="isOwnBuilding"
    class="small with-icon"
    @click="createUnit(UnitType.SPEARMAN)"
    :disabled="!isBuildingSpearmanAvailable"
  >
    {{ t("castleAction.createSpearman") }}
    <BeerDisplay :beer="pricesStore.getCreationPrice(UnitType.SPEARMAN)" />
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
import { ACTION, MAP_TILE_CLICKED } from "@/events.ts";
import { useUnitsStore } from "@/store/unitsStore.ts";
import { useAuthStore } from "@/store/authStore.ts";
import UnitMoveAction from "@/components/partials/game/actions/UnitMoveAction.vue";
import { UnitType } from "@/types/enum/UnitType.ts";
import BeerDisplay from "@/components/partials/game/BeerDisplay.vue";
import { usePricesStore } from "@/store/pricesStore.ts";
import { UnitGateway } from "@/gateways/UnitGateway.ts";

const mapStore = useMapStore();
const buildingsStore = useBuildingsStore();
const unitsStore = useUnitsStore();
const authStore = useAuthStore();
const pricesStore = usePricesStore();
const emit = defineEmits(["close-action"]);
const { t } = useI18n();
const zoomMapTileSizeBeforeAction = ref(100);

const isBuildingSwordsmanAvailable = computed(() => {
  return (
    pricesStore.getCreationPrice(UnitType.SWORDSMAN) <= authStore.user!.beer
  );
});

const isBuildingHorsemanAvailable = computed(() => {
  return (
    pricesStore.getCreationPrice(UnitType.HORSEMAN) <= authStore.user!.beer
  );
});

const isBuildingSpearmanAvailable = computed(() => {
  return (
    pricesStore.getCreationPrice(UnitType.SPEARMAN) <= authStore.user!.beer
  );
});

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

function close(): void {
  emit("close-action");
}

function openMoveUnitActionOverlay(): void {
  close();
  unitsStore.activeMoveUnit = unitAtPosition.value!;
  ACTION.dispatch(UnitMoveAction);
}

async function createUnit(type): Promise<void> {
  try {
    await UnitGateway.instance.createUnit({
      x: buildingsStore.activeBuilding!.x,
      y: buildingsStore.activeBuilding!.y,
      type,
    });
    close();
  } catch (error) {
    handleFatalError(error);
  }
}
</script>

<style lang="scss" scoped></style>
