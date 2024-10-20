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
    <div class="position-text">{{ mapTile.x }} / {{ mapTile.y }}</div>
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

const props = defineProps<{
  mapTile: MapTileDto;
}>();
let mouseDownTimestamp = 0;

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
