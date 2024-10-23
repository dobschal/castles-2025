<template>
  <p>Village of {{ buildingsStore.activeBuilding?.user.username }}</p>
  <CButton
    v-if="isOwnBuilding && unit"
    class="small"
    @click="openUnitActionOverlay"
  >
    {{ t("openVillageAction.moveUnit") }}
  </CButton>
  <CButton v-if="isOwnBuilding" class="small" @click="createWorker">
    {{ t("openVillageAction.createWorker") }}
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
import { useAuthStore } from "@/store/authStore.ts";
import { UnitGateway } from "@/gateways/UnitGateway.ts";
import { UnitType } from "@/types/enum/UnitType.ts";
import { useUnitsStore } from "@/store/unitsStore.ts";
import UnitAction from "@/components/partials/game/actions/UnitAction.vue";
import { Optional } from "@/types/core/Optional.ts";
import { UnitEntity } from "@/types/model/UnitEntity.ts";

const mapStore = useMapStore();
const buildingsStore = useBuildingsStore();
const authStore = useAuthStore();
const unitsStore = useUnitsStore();
const emit = defineEmits(["close-action"]);
const { t } = useI18n();

const isOwnBuilding = computed(() => {
  // Ignore case when both are undefined
  return buildingsStore.activeBuilding?.user.id === authStore.user?.id;
});

const unit = computed<Optional<UnitEntity>>(() => {
  return unitsStore.units.find((unit) => {
    return (
      unit.x === buildingsStore.activeBuilding!.x &&
      unit.y === buildingsStore.activeBuilding!.y
    );
  });
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

async function createWorker(): Promise<void> {
  try {
    await UnitGateway.instance.createUnit({
      x: buildingsStore.activeBuilding!.x,
      y: buildingsStore.activeBuilding!.y,
      type: UnitType.WORKER,
    });
    await unitsStore.loadUnits();
    close();
  } catch (error) {
    handleFatalError(error);
  }
}

function openUnitActionOverlay(): void {
  close();
  unitsStore.activeUnit = unit.value!;
  ACTION.dispatch(UnitAction);
}
</script>

<style lang="scss" scoped></style>
