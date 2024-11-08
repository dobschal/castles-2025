<template>
  <template v-if="isOwnUnit">
    <p>👉 {{ t("unitAction.chooseAction") }}</p>
    <CButton
      v-if="unitsStore.activeUnit && !areBuildingActionsVisible"
      class="small with-icon"
      @click="showMoveAction"
      :disabled="isLoading || movesLastHour >= movesPerHourLimit"
    >
      {{
        t("villageAction.moveUnit", [
          movesPerHourLimit - movesLastHour,
          movesPerHourLimit,
        ])
      }}
      <BeerDisplay
        :beer="pricesStore.getMovePrice(unitsStore.activeUnit?.type)"
      />
    </CButton>
    <CButton
      class="small with-icon"
      v-if="!areBuildingActionsVisible"
      @click="areBuildingActionsVisible = true"
      :disabled="unitsStore.activeUnit?.type !== UnitType.WORKER || isLoading"
    >
      {{ t("unitAction.build") }}
    </CButton>
    <template
      v-if="
        unitsStore.activeUnit?.type === UnitType.WORKER &&
        areBuildingActionsVisible
      "
    >
      <CButton
        class="small with-icon"
        @click="saveBuilding(BuildingType.FARM)"
        :disabled="!isAllowedToBuildFarm || isLoading"
      >
        {{ t("unitAction.buildFarm") }}
        <BeerDisplay :beer="pricesStore.getBuildPrice(BuildingType.FARM)" />
      </CButton>
      <CButton
        class="small with-icon"
        @click="saveBuilding(BuildingType.BREWERY)"
        :disabled="!isAllowedToBuildBrewery || isLoading"
      >
        {{ t("unitAction.buildBrewery") }}
        <BeerDisplay :beer="pricesStore.getBuildPrice(BuildingType.BREWERY)" />
      </CButton>
      <CButton
        class="small with-icon"
        @click="saveBuilding(BuildingType.CASTLE)"
        :disabled="!isAllowedToBuildCastle || isLoading"
      >
        {{ t("unitAction.buildCastle") }}
        <BeerDisplay :beer="pricesStore.getBuildPrice(BuildingType.CASTLE)" />
      </CButton>
      <CButton
        class="small with-icon"
        @click="saveBuilding(BuildingType.VILLAGE)"
        :disabled="!isAllowedToBuildVillage || isLoading"
      >
        {{ t("unitAction.buildVillage") }}
        <BeerDisplay :beer="pricesStore.getBuildPrice(BuildingType.VILLAGE)" />
      </CButton>
      <CButton
        class="small with-icon"
        @click="saveBuilding(BuildingType.MARKET)"
        :disabled="!isAllowedToBuildMarket || isLoading"
      >
        {{ t("unitAction.buildMarket") }}
        <BeerDisplay :beer="pricesStore.getBuildPrice(BuildingType.MARKET)" />
      </CButton>
    </template>
  </template>
  <p v-else>
    {{
      t("unitAction.unitOf", {
        playerName: activeUnit?.user.username,
        x: activeUnit?.x,
        y: activeUnit?.y,
      })
    }}
  </p>
  <CButton
    v-if="!areBuildingActionsVisible"
    class="small"
    @click="close"
    :disabled="isLoading"
  >
    {{ t("general.cancel") }}
  </CButton>
  <CButton
    v-else
    class="small"
    @click="areBuildingActionsVisible = false"
    :disabled="isLoading"
  >
    {{ t("general.cancel") }}
  </CButton>
</template>

<script lang="ts" setup>
import { useI18n } from "vue-i18n";
import CButton from "@/components/partials/general/CButton.vue";
import { computed, onBeforeUnmount, onMounted, ref } from "vue";
import { useUnitsStore } from "@/store/unitsStore.ts";
import { ACTION, MAP_TILE_CLICKED } from "@/events.ts";
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
import { useTutorialStore } from "@/store/tutorialStore.ts";
import { TutorialType } from "@/types/enum/TutorialType.ts";
import { useEventsStore } from "@/store/eventsStore.ts";

const unitsStore = useUnitsStore();
const pricesStore = usePricesStore();
const mapStore = useMapStore();
const buildingStore = useBuildingsStore();
const authStore = useAuthStore();
const tutorialStore = useTutorialStore();
const eventsStore = useEventsStore();
const isLoading = ref(false);
const areBuildingActionsVisible = ref(false);

const { t } = useI18n();
const emit = defineEmits(["close-action"]);

const activeUnit = computed(() => {
  return unitsStore.activeUnit;
});

const isOwnUnit = computed(() => {
  return activeUnit.value?.user.id === authStore.user?.id;
});

const movesLastHour = computed(() => {
  if (!activeUnit.value) return -1;

  return unitsStore.movesLastHour(activeUnit.value);
});

const movesPerHourLimit = computed(() => {
  if (!activeUnit.value) return -1;

  return unitsStore.movesPerHourLimit(activeUnit.value);
});

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
  const price = pricesStore.getBuildPrice(BuildingType.BREWERY);
  const beer = authStore.user?.beer ?? 0;

  return isAllowedToBuild.value && beer >= price;
});

const isAllowedToBuildMarket = computed(() => {
  const price = pricesStore.getBuildPrice(BuildingType.MARKET);
  const beer = authStore.user?.beer ?? 0;

  return isAllowedToBuild.value && beer >= price;
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
  MAP_TILE_CLICKED.on(close);
  mapStore.goToPosition({
    x: unitsStore.activeUnit?.x ?? 0,
    y: unitsStore.activeUnit?.y ?? 0,
  });
});

onBeforeUnmount(() => {
  MAP_TILE_CLICKED.off(close);
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
    eventsStore.ownActionHappened = true;
    close();

    if (
      tutorialStore.tutorial &&
      [
        TutorialType.FIRST_BREWERY,
        TutorialType.FIRST_FARM,
        TutorialType.FIRST_CASTLE,
      ].includes(tutorialStore.tutorial?.type)
    ) {
      await tutorialStore.loadAndShowTutorial();
    }
  } catch (error) {
    handleFatalError(error);
  } finally {
    isLoading.value = false;
  }
}
</script>
