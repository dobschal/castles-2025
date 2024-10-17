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
import { isTouchDevice } from "@/core/util.ts";

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
const touchDownTime = ref(0);
const lastTouch = ref({ x: 0, y: 0 });

// endregion
// region computed

// The center position is used to load the right amount of
// tiles around the player. This is actually hard to calculate
// because the map is rotated 45deg... if it is working, leave it as it is
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
  window.addEventListener("resize", onWindowResize);

  if (!isTouchDevice()) {
    window.addEventListener("mousedown", onMapMouseDown);
    window.addEventListener("mousemove", onMapMouseMove);
    window.addEventListener("mouseup", onMapMouseUp);
  } else {
    window.addEventListener("touchstart", onTouchStart);
    window.addEventListener("touchmove", onTouchMove);
    window.addEventListener("touchend", onToucheEnd);
  }

  load();
});

onBeforeUnmount(() => {
  window.removeEventListener("resize", onWindowResize);
  window.removeEventListener("mousedown", onMapMouseDown);
  window.removeEventListener("mousemove", onMapMouseMove);
  window.removeEventListener("mouseup", onMapMouseUp);
  window.removeEventListener("touchstart", onTouchStart);
  window.removeEventListener("touchmove", onTouchMove);
  window.removeEventListener("touchend", onToucheEnd);
});

// endregion
// region methods

function getMapStyle(): Record<string, string> {
  const aspectRatio = windowWidth.value / windowHeight.value;

  // We need to move the map a bit, because of teh 45deg rotation the map is a bit off...
  return {
    marginLeft: -windowWidth.value * 0.3 * (aspectRatio - 2) + "px",
    marginTop:
      -windowHeight.value * 0.3 * (aspectRatio - 2) +
      windowHeight.value * 0.2 +
      "px",
    transform: `translateX(${offsetX.value}px) translateY(${offsetY.value}px) rotate(-45deg)`,
  };
}

function getMapTileStyle(mapTile: MapTileEntity): MapTileStyle {
  const x = mapTile.x * mapTileSize.value - mapTileSize.value / 2;
  const y = mapTile.y * mapTileSize.value - mapTileSize.value / 2;

  return {
    ...(mapTile.x === centerPosition.value.x &&
    mapTile.y === centerPosition.value.y
      ? { border: "10px solid red" }
      : {}),
    left: x + "px",
    top: y + "px",
    zIndex: 9999 - mapTile.x + mapTile.y * 10,
  };
}

async function load(): Promise<void> {
  const amountOfTiles = Math.max(amountOfTilesX.value, amountOfTilesY.value);
  mapStore.mapTiles = await MapGateway.instance.getMapTiles({
    x1: Math.floor(centerPosition.value.x - amountOfTiles / 1.5),
    y1: Math.floor(centerPosition.value.y - amountOfTiles / 1.5),
    x2: Math.ceil(centerPosition.value.x + amountOfTiles / 1.5),
    y2: Math.ceil(centerPosition.value.y + amountOfTiles / 1.5),
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

function onTouchStart(event: TouchEvent): void {
  isDragging.value = true;
  touchDownTime.value = Date.now();
  lastTouch.value.x = event.touches[0].clientX;
  lastTouch.value.y = event.touches[0].clientY;
}

function onTouchMove(event: TouchEvent): void {
  if (!isDragging.value) {
    return;
  }

  offsetX.value += event.touches[0].clientX - lastTouch.value.x;
  offsetY.value += event.touches[0].clientY - lastTouch.value.y;

  lastTouch.value.x = event.touches[0].clientX;
  lastTouch.value.y = event.touches[0].clientY;
}

function onToucheEnd(): void {
  isDragging.value = false;

  if (Date.now() - touchDownTime.value > 200) {
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
  transform-origin: center;
}
</style>
