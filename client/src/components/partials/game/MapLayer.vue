<template>
  <div class="map">
    <div
      v-for="mapTile in mapStore.mapTiles"
      :key="mapTile.id"
      class="map-tile"
      :style="{
        left: mapTile.x * mapTileWidth + 'px',
        top: mapTile.y * mapTileWidth + 'px',
        zIndex: 999 - mapTile.x * 10,
      }"
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
    </div>
  </div>
</template>

<script lang="ts" setup>
import { onMounted } from "vue";
import { MapGateway } from "@/gateways/MapGateway.ts";
import { useMapStore } from "@/store/mapStore.ts";
import { MapTileType } from "@/types/enum/MapTileType.ts";
import mountainTile from "@/assets/tiles/mountain.png";
import waterTile from "@/assets/tiles/water.png";
import forestTile from "@/assets/tiles/forest.png";
import plainTile from "@/assets/tiles/plain.png";

const mapStore = useMapStore();
const mapTileWidth = 100;

// Have current position as tile x and y in center of screen
// Fetch map tiles around this position from API

// On drag, move map tiles around and fetch new ones

onMounted(async () => {
  mapStore.mapTiles = await MapGateway.instance.getMapTiles({
    x1: 0,
    y1: 0,
    x2: 10,
    y2: 10,
  });
});
</script>

<style lang="scss" scoped>
.map {
  position: relative;
  width: 100%;
  height: 100vh;
  transform: rotate(-45deg);
  background: aquamarine;

  .map-tile {
    position: absolute;
    width: 100px;
    height: 100px;
    border: 3px red solid;

    &:hover {
      background-color: yellow;
    }

    img {
      display: block;

      &.water {
        margin-left: -50%;
        margin-top: -40%;
        width: 180%;
      }

      &.forest {
        width: 165%;
        margin-left: -30%;
        margin-top: -35%;
      }

      &.plain {
        width: 165%;
        margin-left: -30%;
        margin-top: -35%;
      }

      &.mountain {
        margin-left: -50%;
        margin-top: -60%;
        width: 190%;
      }
    }
  }
}
</style>
