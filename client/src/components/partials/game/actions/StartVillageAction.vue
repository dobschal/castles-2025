<template>
  <p>👉 {{ t("startVillageAction.text") }}</p>
</template>

<script lang="ts" setup>
import { onBeforeUnmount, onMounted, watch } from "vue";
import { DIALOG, MAP_TILE_CLICKED } from "@/events.ts";
import { useMapStore } from "@/store/mapStore.ts";
import { MapTileType } from "@/types/enum/MapTileType.ts";
import { MapTileState } from "@/types/enum/MapTileState.ts";
import { MapTileDto } from "@/types/dto/MapTileDto.ts";
import { useI18n } from "vue-i18n";
import { BuildingGateway } from "@/gateways/BuildingGateway.ts";
import { handleFatalError } from "@/core/util.ts";
import { useBuildingsStore } from "@/store/buildingsStore.ts";
import { useUnitsStore } from "@/store/unitsStore.ts";

const unitsStore = useUnitsStore();
const buildingsStore = useBuildingsStore();
const mapStore = useMapStore();
const { t } = useI18n();

const emit = defineEmits(["close-action"]);

onMounted(() => {
  setMapTilesStates();
  MAP_TILE_CLICKED.on(onMapTileClicked);
});

onBeforeUnmount(() => {
  MAP_TILE_CLICKED.off(onMapTileClicked);
});

watch(() => [mapStore.mapTiles, buildingsStore.buildings], setMapTilesStates);

function setMapTilesStates(): void {
  mapStore.mapTiles.forEach((tile) => {
    // Aligned with server-side logic!
    const conflictingBuilding = buildingsStore.buildings.find((building) => {
      return (
        building.x >= tile.x - 2 &&
        building.x < tile.x + 3 &&
        building.y >= tile.y - 2 &&
        building.y < tile.y + 3
      );
    });

    const conflictingUnit = unitsStore.units.find((unit) => {
      return unit.x === tile.x && unit.y === tile.y;
    });

    if (
      conflictingUnit ||
      conflictingBuilding ||
      tile.type !== MapTileType.PLAIN
    ) {
      tile.state = MapTileState.FORBIDDEN;
    } else {
      tile.state = MapTileState.ACCEPTABLE;
    }
  });
}

function onMapTileClicked(mapTile: MapTileDto): void {
  if (mapTile.state === MapTileState.ACCEPTABLE) {
    DIALOG.dispatch({
      questionKey: "startVillageAction.dialog",
      yesButtonKey: "general.yes",
      noButtonKey: "general.no",
      onYes: () => {
        createStartVillage(mapTile);
      },
    });
  }
}

async function createStartVillage(mapTile: MapTileDto): Promise<void> {
  try {
    await BuildingGateway.instance.saveStartVillage(mapTile);
    mapStore.mapTiles.forEach((tile) => {
      tile.state = MapTileState.DEFAULT;
    });
    await buildingsStore.loadBuildings();
    emit("close-action");
  } catch (error) {
    handleFatalError(error);
  }
}
</script>
