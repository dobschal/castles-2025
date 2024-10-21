<template>
  <div class="map" ref="map" :style="getMapStyle()">
    <MapTile
      v-for="mapTile in mapStore.mapTiles"
      :key="mapTile.id"
      :style="getMapTileStyle(mapTile)"
      :map-tile="mapTile"
    />
  </div>
  <div class="center"></div>
</template>

<script lang="ts" setup>
import { onBeforeUnmount, onMounted, ref } from "vue";
import { useMapStore } from "@/store/mapStore.ts";
import MapTile from "@/components/partials/game/MapTile.vue";
import { Optional } from "@/types/core/Optional.ts";
import { isTouchDevice } from "@/core/util.ts";
import { MapTileDto } from "@/types/dto/MapTileDto.ts";

interface MapTileStyle {
  left: string;
  top: string;
  zIndex: number;
}

// region variables

const map = ref<Optional<HTMLElement>>();
const mapStore = useMapStore();
const isDragging = ref(false);
const mouseDownTime = ref(0);
const touchDownTime = ref(0);
const lastTouch = ref({ x: 0, y: 0 });

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
  return {
    transform: `translateX(${mapStore.offsetX}px) translateY(${mapStore.offsetY}px) rotate(-45deg)`,
  };
}

function getMapTileStyle(mapTile: MapTileDto): MapTileStyle {
  const x = mapTile.x * mapStore.mapTileSize - mapStore.mapTileSize / 2;
  const y = mapTile.y * mapStore.mapTileSize - mapStore.mapTileSize / 2;

  return {
    left: x + "px",
    top: y + "px",
    zIndex: 9999 - mapTile.x + mapTile.y * 10,
  };
}

function onWindowResize(): void {
  mapStore.windowWidth = window.innerWidth;
  mapStore.windowHeight = window.innerHeight;
}

function onMapMouseDown(): void {
  isDragging.value = true;
  mouseDownTime.value = Date.now();
}

function onMapMouseMove(event: MouseEvent): void {
  if (!isDragging.value) {
    return;
  }

  mapStore.offsetX += event.movementX;
  mapStore.offsetY += event.movementY;
}

function onMapMouseUp(): void {
  isDragging.value = false;
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

  mapStore.offsetX += event.touches[0].clientX - lastTouch.value.x;
  mapStore.offsetY += event.touches[0].clientY - lastTouch.value.y;

  lastTouch.value.x = event.touches[0].clientX;
  lastTouch.value.y = event.touches[0].clientY;
}

function onToucheEnd(): void {
  isDragging.value = false;
}

// endregion
</script>

<style lang="scss" scoped>
.map {
  position: relative;
  width: 100%;
  height: 100vh;
  transform: rotate(-45deg);
  transform-origin: top left;
}

.center {
  position: absolute;
  width: 10px;
  height: 10px;
  background-color: red;
  border-radius: 50%;
  left: 50%;
  top: 50%;
  transform: translate(-50%, -50%);
  z-index: 99999;
}
</style>
