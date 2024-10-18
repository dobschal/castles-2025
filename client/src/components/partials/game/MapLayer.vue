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
import { useMap } from "@/composables/Map.ts";
import { MapTileDto } from "@/types/dto/MapTileDto.ts";

interface MapTileStyle {
  left: string;
  top: string;
  zIndex: number;
}

// region variables

const map = ref<Optional<HTMLElement>>();
const mapStore = useMapStore();
const {
  load,
  centerPosition,
  windowHeight,
  windowWidth,
  offsetX,
  offsetY,
  mapTileSize,
} = useMap();

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

  await load();
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
  // const aspectRatio = windowWidth.value / windowHeight.value;

  // We need to move the map a bit, because of teh 45deg rotation the map is a bit off...
  return {
    // marginLeft: -windowWidth.value * 0.3 * (aspectRatio - 2) + "px",
    // marginTop:
    //   -windowHeight.value * 0.3 * (aspectRatio - 2) +
    //   windowHeight.value * 0.2 +
    //   "px",
    transform: `translateX(${offsetX.value}px) translateY(${offsetY.value}px) rotate(-45deg)`,
  };
}

function getMapTileStyle(mapTile: MapTileDto): MapTileStyle {
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
