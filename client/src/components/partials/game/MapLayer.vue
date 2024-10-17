<template>
  <div class="map" ref="map" :style="getMapStyle()">
    <MapTile
      v-for="mapTile in mapStore.mapTiles"
      :key="mapTile.id"
      :style="getMapTileStyle(mapTile)"
      :map-tile="mapTile"
    />
  </div>
</template>

<script lang="ts" setup>
import { computed, onBeforeUnmount, onMounted, ref } from "vue";
import { MapGateway } from "@/gateways/MapGateway.ts";
import { useMapStore } from "@/store/mapStore.ts";
import MapTile from "@/components/partials/game/MapTile.vue";
import { MapTileEntity } from "@/types/model/MapTileEntity.ts";
import { Optional } from "@/types/Optional.ts";

interface MapTileStyle {
  left: string;
  top: string;
  zIndex: number;
}

// region variables

const map = ref<Optional<HTMLElement>>();
const mapStore = useMapStore();
const mapTileSize = ref(100);
const mapTileDiagonal = ref(Math.sqrt(2 * Math.pow(mapTileSize.value, 2)));
const windowWidth = ref(window.innerWidth);
const windowHeight = ref(window.innerHeight);
const offsetX = ref(0);
const offsetY = ref(0);
const isDragging = ref(false);
const mouseDownTime = ref(0);

// endregion
// region computed

// The center position is used to load the right amount of
// tiles around the player. This is actually hard to calculate
// because the map is rotated 45deg.
const centerPosition = computed(() => {
  const centerX = Math.round(
    (-offsetX.value + windowWidth.value / 2) / mapTileDiagonal.value,
  );
  const centerY = Math.round(
    (-offsetY.value - windowHeight.value / 2) / mapTileDiagonal.value,
  );

  return {
    x: Math.round(centerX - centerY),
    y: Math.round(centerY + centerX),
  };
});

// The amount of tiles in x and y direction is calculated
// based on the window size and the tile size
const amountOfTilesX = computed(() =>
  Math.ceil(windowWidth.value / mapTileSize.value),
);
const amountOfTilesY = computed(() =>
  Math.ceil(windowHeight.value / mapTileSize.value),
);

// endregion
// region hooks

onMounted(async () => {
  // TODO: Add touch screen support too

  window.addEventListener("resize", onWindowResize);
  window.addEventListener("mousedown", onMapMouseDown);
  window.addEventListener("mousemove", onMapMouseMove);
  window.addEventListener("mouseup", onMapMouseUp);
  load();
});

onBeforeUnmount(() => {
  window.removeEventListener("resize", onWindowResize);
  window.removeEventListener("mousedown", onMapMouseDown);
  window.removeEventListener("mousemove", onMapMouseMove);
  window.removeEventListener("mouseup", onMapMouseUp);
});

// endregion
// region methods

function getMapStyle(): Record<string, string> {
  return {
    transform: `translateX(${offsetX.value}px) translateY(${offsetY.value}px) rotate(-45deg)`,
  };
}

function getMapTileStyle(mapTile: MapTileEntity): MapTileStyle {
  const x = mapTile.x * mapTileSize.value - mapTileSize.value / 2;
  const y = mapTile.y * mapTileSize.value - mapTileSize.value / 2;

  return {
    left: x + "px",
    top: y + "px",
    zIndex: 9999 - mapTile.x + mapTile.y * 10,
  };
}

async function load(): Promise<void> {
  const amountOfTiles = Math.max(amountOfTilesX.value, amountOfTilesY.value);
  mapStore.mapTiles = await MapGateway.instance.getMapTiles({
    x1: Math.floor(centerPosition.value.x - amountOfTiles),
    y1: Math.floor(centerPosition.value.y - amountOfTiles),
    x2: Math.ceil(centerPosition.value.x + amountOfTiles),
    y2: Math.ceil(centerPosition.value.y + amountOfTiles),
  });
}

function onWindowResize(): void {
  windowWidth.value = window.innerWidth;
  windowHeight.value = window.innerHeight;
  load();
}

function onMapMouseDown(): void {
  isDragging.value = true;
  mouseDownTime.value = Date.now();
}

function onMapMouseMove(event: MouseEvent): void {
  if (!isDragging.value) {
    return;
  }

  offsetX.value += event.movementX;
  offsetY.value += event.movementY;
}

function onMapMouseUp(): void {
  isDragging.value = false;

  if (Date.now() - mouseDownTime.value > 200) {
    load();
  }
}

// endregion
</script>

<style lang="scss" scoped>
.map {
  position: relative;
  width: 100%;
  height: 100vh;
  transform: rotate(-45deg);
  background: aquamarine;
  transform-origin: center;
}
</style>
