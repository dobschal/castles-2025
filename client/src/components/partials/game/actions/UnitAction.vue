<template>
  <p>👉 {{ t("unitAction.moveText") }}</p>
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
import { handleFatalError } from "@/core/util.ts";
import { UnitGateway } from "@/gateways/UnitGateway.ts";

const unitsStore = useUnitsStore();
const buildingsStore = useBuildingsStore();
const mapStore = useMapStore();
const authStore = useAuthStore();
const { t } = useI18n();
const emit = defineEmits(["close-action"]);

const activeUnit = computed(() => {
  return unitsStore.activeUnit;
});

watch(
  () => [mapStore.mapTiles, buildingsStore.buildings, unitsStore.units],
  setMapTilesStates,
);

onMounted(() => {
  setMapTilesStates();
  MAP_TILE_CLICKED.on(onMapTileClicked);
});

onBeforeUnmount(() => {
  MAP_TILE_CLICKED.off(onMapTileClicked);
  unitsStore.activeUnit = undefined;
  mapStore.mapTiles.forEach((tile) => {
    tile.state = MapTileState.DEFAULT;
  });
});

function setMapTilesStates(): void {
  mapStore.mapTiles.forEach((tile) => {
    if (!activeUnit.value) return;
    // Aligned with server side logic!
    const tileIsOutOfRange =
      Math.abs(tile.x - activeUnit.value.x) > 1 ||
      Math.abs(tile.y - activeUnit.value.y) > 1;

    const conflictingUnit = unitsStore.units.find((unit) => {
      return (
        unit.x === tile.x &&
        unit.y === tile.y &&
        unit.user.id === authStore.user?.id &&
        unit.id
      );
    });

    if (
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
  console.info("Move unit to: ", mapTile);

  if (mapTile.state !== MapTileState.ACCEPTABLE) return;

  try {
    await UnitGateway.instance.moveUnit(
      mapTile.x,
      mapTile.y,
      activeUnit.value!.id,
    );
    await unitsStore.loadUnits();
    close();
  } catch (e) {
    handleFatalError(e);
  }
}
</script>
