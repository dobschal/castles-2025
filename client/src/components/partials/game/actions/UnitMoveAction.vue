<template>
  <p>
    {{
      t("unitAction.movesRemaining", [
        movesPerHourLimit - movesLastHour,
        movesPerHourLimit,
      ])
    }}
  </p>
  <p>
    👉
    <span v-html="t('unitAction.moveText')"></span>
  </p>
  <CButton class="small" @click="close">
    {{ t("general.cancel") }}
  </CButton>
</template>

<script lang="ts" setup>
import { useI18n } from "vue-i18n";
import CButton from "@/components/partials/general/CButton.vue";
import { computed, onBeforeUnmount, onMounted, watch } from "vue";
import { MAP_TILE_CLICKED } from "@/events.ts";
import { MapTileType } from "@/types/enum/MapTileType.ts";
import { MapTileState } from "@/types/enum/MapTileState.ts";
import { useUnitsStore } from "@/store/unitsStore.ts";
import { useBuildingsStore } from "@/store/buildingsStore.ts";
import { useMapStore } from "@/store/mapStore.ts";
import { useAuthStore } from "@/store/authStore.ts";
import { MapTileDto } from "@/types/dto/MapTileDto.ts";
import { handleFatalError, parseServerDateString } from "@/core/util.ts";
import { UnitGateway } from "@/gateways/UnitGateway.ts";
import { UnitType } from "@/types/enum/UnitType.ts";
import { EventType } from "@/types/enum/EventType.ts";
import { useEventsStore } from "@/store/eventsStore.ts";

const unitsStore = useUnitsStore();
const buildingsStore = useBuildingsStore();
const mapStore = useMapStore();
const authStore = useAuthStore();
const eventsStore = useEventsStore();
const { t } = useI18n();
const emit = defineEmits(["close-action"]);

const activeMoveUnit = computed(() => {
  return unitsStore.activeMoveUnit;
});

const movesLastHour = computed(() => {
  return eventsStore.events.filter(
    (event) =>
      event.unit?.id === activeMoveUnit.value?.id &&
      event.type === EventType.UNIT_MOVED &&
      Date.now() - parseServerDateString(event.createdAt).getTime() < 3600000,
  ).length;
});

const movesPerHourLimit = computed(() => {
  switch (activeMoveUnit.value?.type) {
    case UnitType.WORKER:
      return unitsStore.workerMovesPerHour;
    case UnitType.SWORDSMAN:
      return unitsStore.swordsmanMovesPerHour;
    case UnitType.SPEARMAN:
      return unitsStore.spearmanMovesPerHour;
    case UnitType.HORSEMAN:
      return unitsStore.horsemanMovesPerHour;
    default:
      return 0;
  }
});

watch(
  () => [mapStore.mapTiles, buildingsStore.buildings, unitsStore.units],
  setMapTilesStates,
);

onMounted(() => {
  setMapTilesStates();
  MAP_TILE_CLICKED.on(onMapTileClicked);
  mapStore.goToPosition({
    x: unitsStore.activeMoveUnit?.x ?? 0,
    y: unitsStore.activeMoveUnit?.y ?? 0,
  });
});

onBeforeUnmount(() => {
  MAP_TILE_CLICKED.off(onMapTileClicked);
  unitsStore.activeMoveUnit = undefined;
  mapStore.mapTiles.forEach((tile) => {
    tile.state = MapTileState.DEFAULT;
  });
});

function setMapTilesStates(): void {
  mapStore.mapTiles.forEach((tile) => {
    if (!activeMoveUnit.value) return;
    // Aligned with server side logic!
    const tileIsOutOfRange =
      Math.abs(tile.x - activeMoveUnit.value.x) > 1 ||
      Math.abs(tile.y - activeMoveUnit.value.y) > 1;

    const conflictingUnit = unitsStore.units.find((unit) => {
      return (
        unit.x === tile.x &&
        unit.y === tile.y &&
        (unit.user.id === authStore.user?.id ||
          activeMoveUnit.value?.type === UnitType.WORKER) &&
        unit.id !== activeMoveUnit.value!.id
      );
    });

    const conflictingBuilding = buildingsStore.buildings.find((building) => {
      return (
        building.x === tile.x &&
        building.y === tile.y &&
        activeMoveUnit.value?.type === UnitType.WORKER &&
        building.user.id !== authStore.user?.id
      );
    });

    if (
      conflictingBuilding ||
      tileIsOutOfRange ||
      conflictingUnit ||
      tile.type === MapTileType.WATER
    ) {
      tile.state = MapTileState.FORBIDDEN;
    } else {
      tile.state = MapTileState.ACCEPTABLE;
    }
  });
}

function close(): void {
  emit("close-action");
}

async function onMapTileClicked(mapTile: MapTileDto): Promise<void> {
  if (mapTile.state !== MapTileState.ACCEPTABLE) return;

  try {
    await UnitGateway.instance.moveUnit(
      mapTile.x,
      mapTile.y,
      activeMoveUnit.value!.id,
    );
    close();
  } catch (e) {
    handleFatalError(e);
  }
}
</script>
