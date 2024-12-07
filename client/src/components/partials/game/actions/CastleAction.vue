<template>
  <p>
    {{
      t("castleAction.castleOf", {
        playerName: buildingsStore.activeBuilding?.user.username,
      })
    }}
  </p>
  <template v-if="isOwnBuilding">
    <template v-if="unitMenuOpen">
      <template v-if="level == 2">
        <CButton
          class="small with-icon"
          @click="createUnit(UnitType.DRAGON)"
          :disabled="!isBuildingDragonAvailable"
        >
          {{ t("castleAction.createDragon") }}
          <GoldDisplay :gold="pricesStore.getCreationPrice(UnitType.DRAGON)" />
        </CButton>
        <CButton
          class="small with-icon"
          @click="createUnit(UnitType.ARCHER)"
          :disabled="!isBuildingArcherAvailable"
        >
          {{ t("castleAction.createArcher") }}
          <GoldDisplay :gold="pricesStore.getCreationPrice(UnitType.ARCHER)" />
        </CButton>
      </template>
      <CButton
        class="small with-icon"
        @click="createUnit(UnitType.SWORDSMAN)"
        :disabled="!isBuildingSwordsmanAvailable"
      >
        {{ t("castleAction.createSwordsman") }}
        <BeerDisplay :beer="pricesStore.getCreationPrice(UnitType.SWORDSMAN)" />
      </CButton>
      <CButton
        class="small with-icon"
        @click="createUnit(UnitType.HORSEMAN)"
        :disabled="!isBuildingHorsemanAvailable"
      >
        {{ t("castleAction.createHorseman") }}
        <BeerDisplay :beer="pricesStore.getCreationPrice(UnitType.HORSEMAN)" />
      </CButton>
      <CButton
        class="small with-icon"
        @click="createUnit(UnitType.SPEARMAN)"
        :disabled="!isBuildingSpearmanAvailable"
      >
        {{ t("castleAction.createSpearman") }}
        <BeerDisplay :beer="pricesStore.getCreationPrice(UnitType.SPEARMAN)" />
      </CButton>
      <CButton class="small" @click="unitMenuOpen = false">
        {{ t("general.close") }}
      </CButton>
    </template>
    <template v-else>
      <SelectUnitButton @close="close" :unit="unitAtPosition" />
      <CButton class="small with-icon" @click="unitMenuOpen = true">
        {{ t("castleAction.units") }}
      </CButton>
      <CButton
        v-if="buildingsStore.activeBuilding?.level === 1"
        class="small with-icon"
        @click="upgradeCastle"
      >
        {{ t("castleAction.upgrade") }}
        <GoldDisplay :gold="levelUpPrice" />
      </CButton>
      <CButton class="small with-icon" @click="destroy">
        {{ t("castleAction.destroy") }}
      </CButton>
      <CButton class="small" @click="close">
        {{ t("general.close") }}
      </CButton>
    </template>
  </template>
  <template v-else>
    <!-- Not own building -->
    <CButton class="small" @click="close">
      {{ t("general.close") }}
    </CButton>
  </template>
</template>

<script lang="ts" setup>
import { computed, onBeforeUnmount, onMounted, ref } from "vue";
import { useMapStore } from "@/store/mapStore.ts";
import { useBuildingsStore } from "@/store/buildingsStore.ts";
import { handleFatalError } from "@/core/util.ts";
import CButton from "@/components/partials/general/CButton.vue";
import { useI18n } from "vue-i18n";
import { DIALOG, MAP_TILE_CLICKED } from "@/events.ts";
import { useUnitsStore } from "@/store/unitsStore.ts";
import { useAuthStore } from "@/store/authStore.ts";
import { UnitType } from "@/types/enum/UnitType.ts";
import BeerDisplay from "@/components/partials/game/displays/BeerDisplay.vue";
import { usePricesStore } from "@/store/pricesStore.ts";
import { UnitGateway } from "@/gateways/UnitGateway.ts";
import { BuildingGateway } from "@/gateways/BuildingGateway.ts";
import { useEventsStore } from "@/store/eventsStore.ts";
import GoldDisplay from "@/components/partials/game/displays/GoldDisplay.vue";
import { BuildingType } from "@/types/enum/BuildingType.ts";
import SelectUnitButton from "@/components/partials/game/SelectUnitButton.vue";

const mapStore = useMapStore();
const buildingsStore = useBuildingsStore();
const unitsStore = useUnitsStore();
const authStore = useAuthStore();
const pricesStore = usePricesStore();
const eventsStore = useEventsStore();
const emit = defineEmits(["close-action"]);
const { t } = useI18n();
const zoomMapTileSizeBeforeAction = ref(100);
const unitMenuOpen = ref(false);

const isBuildingArcherAvailable = computed(() => {
  return pricesStore.getCreationPrice(UnitType.ARCHER) <= authStore.user!.gold;
});

const isBuildingDragonAvailable = computed(() => {
  return pricesStore.getCreationPrice(UnitType.DRAGON) <= authStore.user!.gold;
});

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

const level = computed(() => {
  return buildingsStore.activeBuilding?.level;
});

const isOwnBuilding = computed(() => {
  // Ignore case when both are undefined
  return buildingsStore.activeBuilding?.user.id === authStore.user?.id;
});

const levelUpPrice = computed(() => {
  return pricesStore.prices?.buildingLevelUpPrices[BuildingType.CASTLE] ?? 0;
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

async function upgradeCastle(): Promise<void> {
  try {
    await BuildingGateway.instance.upgradeBuilding(
      buildingsStore.activeBuilding!.id,
    );
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

async function createUnit(type): Promise<void> {
  try {
    await UnitGateway.instance.createUnit({
      x: buildingsStore.activeBuilding!.x,
      y: buildingsStore.activeBuilding!.y,
      type,
    });
    eventsStore.ownActionHappened = true;
    close();
  } catch (error) {
    handleFatalError(error);
  }
}
</script>

<style lang="scss" scoped></style>
