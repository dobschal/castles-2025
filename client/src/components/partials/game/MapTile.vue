<template>
  <div
    class="map-tile"
    @mousedown="onMouseDown"
    @mouseup="onMouseUp"
    :class="[mapTile.state]"
  >
    <img
      v-if="mapTile.type === MapTileType.FOREST"
      :src="forestTile"
      class="forest"
      alt="Forest"
    />
    <img
      v-else-if="mapTile.type === MapTileType.PLAIN"
      :src="plainTile"
      class="plain"
      alt="Plain"
    />
    <img
      v-else-if="mapTile.type === MapTileType.WATER"
      :src="waterTile"
      class="water"
      alt="Water"
    />
    <img
      v-else-if="mapTile.type === MapTileType.MOUNTAIN"
      :src="mountainTile"
      class="mountain"
      alt="Mountain"
    />
    <!--    <div class="position-text">{{ mapTile.x }} / {{ mapTile.y }}</div>-->
    <BuildingTile v-if="building" :building="building"></BuildingTile>
  </div>
</template>

<script setup lang="ts">
import { MapTileType } from "@/types/enum/MapTileType.ts";
import mountainTile from "@/assets/tiles/mountain.png";
import waterTile from "@/assets/tiles/water.png";
import forestTile from "@/assets/tiles/forest.png";
import plainTile from "@/assets/tiles/plain.png";
import { MAP_TILE_CLICKED } from "@/events.ts";
import { MapTileDto } from "@/types/dto/MapTileDto.ts";
import BuildingTile from "@/components/partials/game/BuildingTile.vue";
import { computed } from "vue";
import { useBuildingsStore } from "@/store/buildingsStore.ts";
import { BuildingEntity } from "@/types/model/BuildingEntity.ts";
import { Optional } from "@/types/core/Optional.ts";

const buildingsStore = useBuildingsStore();
const props = defineProps<{
  mapTile: MapTileDto;
}>();
let mouseDownTimestamp = 0;

const building = computed<Optional<BuildingEntity>>(() => {
  return buildingsStore.buildings.find((building) => {
    return building.x === props.mapTile.x && building.y === props.mapTile.y;
  });
});

// TODO: Need touch support too

function onMouseDown(): void {
  mouseDownTimestamp = Date.now();
}

function onMouseUp(): void {
  if (Date.now() - mouseDownTimestamp > 200) {
    return;
  }

  MAP_TILE_CLICKED.dispatch(props.mapTile);
}
</script>

<style lang="scss" scoped>
.map-tile {
  position: absolute;
  width: 100px;
  height: 100px;
  user-select: none;

  &.FORBIDDEN {
    opacity: 0.5;
  }

  &:hover {
    background-color: yellow;
  }

  img {
    position: absolute;
    z-index: 0;
    display: block;
    pointer-events: none;

    &.water {
      margin-left: -30%;
      margin-top: -30%;
      width: 160%;
    }

    &.forest {
      width: 160%;
      margin-left: -25%;
      margin-top: -35%;
    }

    &.plain {
      width: 150%;
      margin-left: -30%;
      margin-top: -25%;
    }

    &.mountain {
      margin-left: -25%;
      margin-top: -30%;
      width: 150%;
    }
  }

  .position-text {
    position: absolute;
    bottom: 0;
    right: 0;
    background-color: rgba(255, 255, 255, 0.5);
    padding: 0.5rem;
  }
}
</style>
