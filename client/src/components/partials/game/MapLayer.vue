<template>
  <div class="map" ref="map" :style="getMapStyle()">
    <MapTile
      v-for="mapTile in mapStore.mapTiles"
      :key="mapTile.id"
      :style="getMapTileStyle(mapTile)"
      :map-tile="mapTile"
    />
    <template v-if="eventsStore.showEventsOnMap && !actionStore.isActionActive">
      <EventTile
        :key="event.id"
        v-for="event in eventsStore.events"
        :event="event"
        :style="getMapTileStyle(event)"
      />
    </template>
  </div>
</template>

<script lang="ts" setup>
import { onBeforeUnmount, onMounted, ref } from "vue";
import { useMapStore } from "@/store/mapStore.ts";
import MapTile from "@/components/partials/game/tiles/MapTile.vue";
import { Optional } from "@/types/core/Optional.ts";
import { isTouchDevice } from "@/core/util.ts";
import EventTile from "@/components/partials/game/tiles/EventTile.vue";
import { useEventsStore } from "@/store/eventsStore.ts";
import { PointDto } from "@/types/dto/PointDto.ts";
import { useActionStore } from "@/store/actionStore.ts";

interface MapTileStyle {
  left: string;
  top: string;
  zIndex: number;
  width: string;
  height: string;
}

// region variables

const map = ref<Optional<HTMLElement>>();
const mapStore = useMapStore();
const eventsStore = useEventsStore();
const actionStore = useActionStore();
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

function getMapTileStyle(mapTile: PointDto): MapTileStyle {
  const x = mapTile.x * mapStore.mapTileSize - mapStore.mapTileSize / 2;
  const y = mapTile.y * mapStore.mapTileSize - mapStore.mapTileSize / 2;

  return {
    width: mapStore.mapTileSize + "px",
    height: mapStore.mapTileSize + "px",
    left: x + "px",
    top: y + "px",
    zIndex: 999 - mapTile.x + mapTile.y * 10,
  };
}

function onWindowResize(): void {
  mapStore.windowWidth = window.innerWidth;
  mapStore.windowHeight = window.innerHeight;
}

function onMapMouseDown(): void {
  if (mapStore.mapControlsDisabled) {
    return;
  }

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
  if (mapStore.mapControlsDisabled) {
    return;
  }

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
  will-change: transform;
}
</style>
