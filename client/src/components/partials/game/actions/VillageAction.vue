<template>
  <p>
    {{
      t("villageAction.villageOf", {
        playerName: buildingsStore.activeBuilding?.user.username,
      })
    }}
  </p>
  <SelectUnitButton @close="close" :unit="unitAtPosition" />
  <CButton
    v-if="isOwnBuilding"
    class="small with-icon"
    @click="createWorker"
    :disabled="!isBuildingWorkerAvailable"
  >
    {{ t("villageAction.createWorker") }}
    <BeerDisplay :beer="pricesStore.getCreationPrice(UnitType.WORKER)" />
  </CButton>
  <CButton
    v-if="isOwnBuilding"
    :disabled="!isUpgradeAvailable"
    class="small with-icon"
    @click="upgradeToCity"
  >
    {{ t("villageAction.upgradeToCity") }}
    <BeerDisplay :beer="pricesStore.getBuildPrice(BuildingType.CITY)" />
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
import { DIALOG, MAP_TILE_CLICKED } from "@/events.ts";
import { useAuthStore } from "@/store/authStore.ts";
import { UnitGateway } from "@/gateways/UnitGateway.ts";
import { UnitType } from "@/types/enum/UnitType.ts";
import { useUnitsStore } from "@/store/unitsStore.ts";
import { usePricesStore } from "@/store/pricesStore.ts";
import BeerDisplay from "@/components/partials/game/displays/BeerDisplay.vue";
import { useTutorialStore } from "@/store/tutorialStore.ts";
import { TutorialType } from "@/types/enum/TutorialType.ts";
import { BuildingGateway } from "@/gateways/BuildingGateway.ts";
import { useEventsStore } from "@/store/eventsStore.ts";
import { BuildingType } from "@/types/enum/BuildingType.ts";
import SelectUnitButton from "@/components/partials/game/SelectUnitButton.vue";

const mapStore = useMapStore();
const buildingsStore = useBuildingsStore();
const authStore = useAuthStore();
const unitsStore = useUnitsStore();
const pricesStore = usePricesStore();
const tutorialStore = useTutorialStore();
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

const isOwnBuilding = computed(() => {
  // Ignore case when both are undefined
  return buildingsStore.activeBuilding?.user.id === authStore.user?.id;
});

const isBuildingWorkerAvailable = computed(() => {
  const price = pricesStore.getCreationPrice(UnitType.WORKER);
  const beer = authStore.user?.beer ?? 0;

  return !unitAtPosition.value && beer >= price;
});

const isUpgradeAvailable = computed(() => {
  const price = pricesStore.getBuildPrice(BuildingType.CITY);
  const beer = authStore.user?.beer ?? 0;

  return beer >= price;
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

async function upgradeToCity(): Promise<void> {
  try {
    if (!buildingsStore.activeBuilding) return;
    await BuildingGateway.instance.createCity({
      x: buildingsStore.activeBuilding.x,
      y: buildingsStore.activeBuilding.y,
    });
    eventsStore.ownActionHappened = true;
    close();
  } catch (error) {
    handleFatalError(error);
  }
}

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

async function createWorker(): Promise<void> {
  try {
    await UnitGateway.instance.createUnit({
      x: buildingsStore.activeBuilding!.x,
      y: buildingsStore.activeBuilding!.y,
      type: UnitType.WORKER,
    });
    eventsStore.ownActionHappened = true;
    close();

    if (tutorialStore.tutorial?.type === TutorialType.FIRST_WORKER) {
      await tutorialStore.loadAndShowTutorial();
    }
  } catch (error) {
    handleFatalError(error);
  }
}
</script>

<style lang="scss" scoped></style>
