<template>
  <div
    class="overview-map-tile"
    :class="[mapTile.type, { 'is-owned': isOwned }]"
  ></div>
</template>

<script lang="ts" setup>
import { MapTileDto } from "@/types/dto/MapTileDto.ts";
import { computed } from "vue";
import { useBuildingsStore } from "@/store/buildingsStore.ts";

const buildingsStore = useBuildingsStore();
const props = defineProps<{
  mapTile: MapTileDto;
}>();

const isOwned = computed(() => {
  buildingsStore.buildings.find((building) => {
    return building.x === props.mapTile.x && building.y === props.mapTile.y;
  });
});
</script>

<style lang="scss" scoped>
.overview-map-tile {
  position: absolute;
  user-select: none;

  &.user-list {
    background: #ff0000;
  }

  &.FOREST {
    background: #638500;
  }

  &.WATER {
    background: #00a2ff;
  }

  &.MOUNTAIN {
    background: #a0a0a0;
  }

  &.PLAIN {
    background: #82ad00;
  }
}
</style>
