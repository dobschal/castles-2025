<template>
  <img class="unit-tile" src="@/assets/tiles/worker.png" alt="Unit" />
</template>

<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted } from "vue";
import { ACTION, MAP_TILE_CLICKED } from "@/events.ts";
import { MapTileDto } from "@/types/dto/MapTileDto.ts";
import { UnitEntity } from "@/types/model/UnitEntity.ts";
import { useUnitsStore } from "@/store/unitsStore.ts";
import UnitAction from "@/components/partials/game/actions/UnitAction.vue";
import { useBuildingsStore } from "@/store/buildingsStore.ts";

const buildingsStore = useBuildingsStore();
const unitsStore = useUnitsStore();
const props = defineProps<{
  unit: UnitEntity;
}>();
const buildingOnPosition = computed(() => {
  return buildingsStore.buildings.find((building) => {
    return building.x === props.unit.x && building.y === props.unit.y;
  });
});

onMounted(() => {
  MAP_TILE_CLICKED.on(onMapTileClicked);
});

onBeforeUnmount(() => {
  MAP_TILE_CLICKED.off(onMapTileClicked);
});

function onMapTileClicked(mapTile: MapTileDto): void {
  if (
    props.unit.x !== mapTile.x ||
    props.unit.y !== mapTile.y ||
    buildingOnPosition.value // If there is a building on that field --> open the building action first
  ) {
    return;
  }

  unitsStore.activeUnit = props.unit;
  ACTION.dispatch(UnitAction);
}
</script>

<style lang="scss" scoped>
.unit-tile {
  position: absolute;
  width: 140%;
  height: 140%;
  margin-left: -35%;
  margin-top: -20%;
  pointer-events: none;
  z-index: 3;
}
</style>
