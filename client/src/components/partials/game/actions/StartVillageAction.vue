<template>
  <div>👉 {{ t("startVillageAction.text") }}</div>
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
import { handleFatalError, TODO } from "@/core/util.ts";

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

watch(() => mapStore.mapTiles, setMapTilesStates);

function setMapTilesStates(): void {
  mapStore.mapTiles.forEach((tile) => {
    if (tile.type !== MapTileType.PLAIN) {
      tile.state = MapTileState.FORBIDDEN;
    } else {
      tile.state = MapTileState.ACCEPTABLE;
    }
  });
}

function onMapTileClicked(mapTile: MapTileDto): void {
  if (mapTile.state === MapTileState.ACCEPTABLE) {
    ensureNoBuildingsAreCloseBy(mapTile);
    ensureNoUnitIsOnTile(mapTile);
    DIALOG.dispatch({
      questionKey: "startVillageAction.dialog",
      yesButtonKey: "general.yes",
      noButtonKey: "general.no",
      onYes: () => {
        startVillage(mapTile);
      },
    });
  }
}

function ensureNoBuildingsAreCloseBy(mapTile: MapTileDto): void {
  TODO("check that no buildings are close by", mapTile);
}

function ensureNoUnitIsOnTile(mapTile: MapTileDto): void {
  TODO("check that no unit is on field", mapTile);
}

async function startVillage(mapTile: MapTileDto): Promise<void> {
  try {
    await BuildingGateway.instance.saveStartVillage(mapTile);
    mapStore.mapTiles.forEach((tile) => {
      tile.state = MapTileState.DEFAULT;
    });
    emit("close-action");
  } catch (error) {
    handleFatalError(error);
  }
}
</script>
