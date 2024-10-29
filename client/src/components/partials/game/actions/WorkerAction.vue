<template>
  <p>👉 {{ t("unitAction.chooseAction") }}</p>
  <CButton
    v-if="unitsStore.activeUnit"
    class="small"
    @click="showMoveAction"
    :disabled="isLoading"
  >
    {{ t("villageAction.moveUnit") }}
    <BeerDisplay
      :beer="pricesStore.getMovePrice(unitsStore.activeUnit?.type)"
    />
  </CButton>
  <template v-if="unitsStore.activeUnit?.type === UnitType.WORKER">
    <CButton
      class="small"
      @click="saveBuilding(BuildingType.FARM)"
      :disabled="!isAllowedToBuildFarm || isLoading"
    >
      {{ t("unitAction.buildFarm") }}
      <BeerDisplay :beer="pricesStore.getBuildPrice(BuildingType.BREWERY)" />
    </CButton>
    <CButton
      class="small"
      @click="saveBuilding(BuildingType.BREWERY)"
      :disabled="!isAllowedToBuildBrewery || isLoading"
    >
      {{ t("unitAction.buildBrewery") }}
      <BeerDisplay :beer="pricesStore.getBuildPrice(BuildingType.FARM)" />
    </CButton>
    <CButton
      class="small"
      @click="saveBuilding(BuildingType.CASTLE)"
      :disabled="!isAllowedToBuildCastle || isLoading"
    >
      {{ t("unitAction.buildCastle") }}
      <BeerDisplay :beer="pricesStore.getBuildPrice(BuildingType.CASTLE)" />
    </CButton>
    <CButton
      class="small"
      @click="saveBuilding(BuildingType.VILLAGE)"
      :disabled="!isAllowedToBuildVillage || isLoading"
    >
      {{ t("unitAction.buildVillage") }}
      <BeerDisplay :beer="pricesStore.getBuildPrice(BuildingType.VILLAGE)" />
    </CButton>
  </template>
  <CButton class="small" @click="close" :disabled="isLoading">
    {{ t("general.cancel") }}
  </CButton>
</template>

<script lang="ts" setup>
import { useI18n } from "vue-i18n";
import CButton from "@/components/partials/general/CButton.vue";
import { computed, onBeforeUnmount, onMounted, ref } from "vue";
import { useUnitsStore } from "@/store/unitsStore.ts";
import { ACTION } from "@/events.ts";
import UnitMoveAction from "@/components/partials/game/actions/UnitMoveAction.vue";
import BeerDisplay from "@/components/partials/game/BeerDisplay.vue";
import { usePricesStore } from "@/store/pricesStore.ts";
import { UnitType } from "@/types/enum/UnitType.ts";
import { useMapStore } from "@/store/mapStore.ts";
import { MapTileType } from "@/types/enum/MapTileType.ts";
import { BuildingType } from "@/types/enum/BuildingType.ts";
import { useBuildingsStore } from "@/store/buildingsStore.ts";
import { BuildingGateway } from "@/gateways/BuildingGateway.ts";
import { handleFatalError } from "@/core/util.ts";
import { useAuthStore } from "@/store/authStore.ts";

const unitsStore = useUnitsStore();
const pricesStore = usePricesStore();
const mapStore = useMapStore();
const buildingStore = useBuildingsStore();
const authStore = useAuthStore();
const isLoading = ref(false);

const { t } = useI18n();
const emit = defineEmits(["close-action"]);

const buildingOnPosition = computed(() => {
  return buildingStore.buildings.find(
    (building) =>
      building.x === unitsStore.activeUnit?.x &&
      building.y === unitsStore.activeUnit?.y,
  );
});

const mapTile = computed(() => {
  return mapStore.mapTiles.find(
    (tile) =>
      tile.x === unitsStore.activeUnit?.x &&
      tile.y === unitsStore.activeUnit?.y,
  );
});

const isAllowedToBuild = computed(() => {
  return (
    unitsStore.activeUnit?.type === UnitType.WORKER &&
    mapTile.value?.type === MapTileType.PLAIN &&
    !buildingOnPosition.value
  );
});

const isAllowedToBuildBrewery = computed(() => {
  const price = pricesStore.getBuildPrice(BuildingType.FARM);
  const beer = authStore.user?.beer ?? 0;
  const farmNearBy = buildingStore.findFarmNextTo(
    unitsStore.activeUnit?.x ?? 0,
    unitsStore.activeUnit?.y ?? 0,
    authStore.user?.id ?? -1,
  );

  return isAllowedToBuild.value && farmNearBy && beer >= price;
});

const isAllowedToBuildCastle = computed(() => {
  const price = pricesStore.getBuildPrice(BuildingType.CASTLE);
  const beer = authStore.user?.beer ?? 0;

  return isAllowedToBuild.value && beer >= price;
});

const isAllowedToBuildVillage = computed(() => {
  const price = pricesStore.getBuildPrice(BuildingType.VILLAGE);
  const beer = authStore.user?.beer ?? 0;

  return isAllowedToBuild.value && beer >= price;
});

const isAllowedToBuildFarm = computed(() => {
  const price = pricesStore.getBuildPrice(BuildingType.FARM);
  const beer = authStore.user?.beer ?? 0;

  return isAllowedToBuild.value && beer >= price;
});

onMounted(() => {
  mapStore.goToPosition({
    x: unitsStore.activeUnit?.x ?? 0,
    y: unitsStore.activeUnit?.y ?? 0,
  });
});

onBeforeUnmount(() => {
  unitsStore.activeUnit = undefined;
});

function close(): void {
  emit("close-action");
}

function showMoveAction(): void {
  unitsStore.activeMoveUnit = unitsStore.activeUnit;
  close();
  ACTION.dispatch(UnitMoveAction);
}

async function saveBuilding(type: BuildingType): Promise<void> {
  try {
    if (isLoading.value) {
      return;
    }

    isLoading.value = true;
    await BuildingGateway.instance.createBuilding(
      {
        x: unitsStore.activeUnit?.x ?? 0,
        y: unitsStore.activeUnit?.y ?? 0,
      },
      type,
    );
    close();
  } catch (error) {
    handleFatalError(error);
  } finally {
    isLoading.value = false;
  }
}
</script>
