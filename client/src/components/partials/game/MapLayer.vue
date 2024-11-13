<template>
  <div class="map" ref="map" :style="getMapStyle()">
    <template v-if="isOverviewMap">
      <OverviewMapTile
        v-for="mapTile in mapStore.mapTiles"
        :key="mapTile.id"
        :style="getMapTileStyle(mapTile)"
        :map-tile="mapTile"
      />
    </template>
    <template v-else>
      <MapTile
        v-for="mapTile in mapStore.mapTiles"
        :key="mapTile.id"
        :style="getMapTileStyle(mapTile)"
        :map-tile="mapTile"
      />
      <template v-if="authStore.showEventsOnMap && !actionStore.isActionActive">
        <EventTile
          :key="event.id"
          v-for="event in eventsStore.events"
          :event="event"
          :style="getMapTileStyle(event)"
        />
      </template>
    </template>
  </div>
</template>

<script lang="ts" setup>
import { computed, onBeforeUnmount, onMounted, ref } from "vue";
import { useMapStore } from "@/store/mapStore.ts";
import MapTile from "@/components/partials/game/tiles/MapTile.vue";
import { Optional } from "@/types/core/Optional.ts";
import { isTouchDevice } from "@/core/util.ts";
import EventTile from "@/components/partials/game/tiles/EventTile.vue";
import { useEventsStore } from "@/store/eventsStore.ts";
import { PointDto } from "@/types/dto/PointDto.ts";
import { useActionStore } from "@/store/actionStore.ts";
import { useAuthStore } from "@/store/authStore.ts";
import OverviewMapTile from "@/components/partials/game/tiles/OverviewMapTile.vue";

interface MapTileStyle {
  left: string;
  top: string;
  zIndex: number;
  width: string;
  height: string;
}

// region variables

const map = ref<Optional<HTMLElement>>();
const authStore = useAuthStore();
const mapStore = useMapStore();
const eventsStore = useEventsStore();
const actionStore = useActionStore();
const isDragging = ref(false);
const mouseDownTime = ref(0);
const touchDownTime = ref(0);
const lastTouch = ref({ x: 0, y: 0 });

const isOverviewMap = computed(() => {
  return mapStore.mapTiles.length > 400;
});

// endregion
// region hooks

onMounted(async () => {
  window.addEventListener("resize", onWindowResize);

  if (!isTouchDevice()) {
    map.value!.addEventListener("mousedown", onMapMouseDown);
    map.value!.addEventListener("mousemove", onMapMouseMove);
    map.value!.addEventListener("mouseup", onMapMouseUp);
  } else {
    map.value!.addEventListener("touchstart", onTouchStart);
    map.value!.addEventListener("touchmove", onTouchMove);
    map.value!.addEventListener("touchend", onToucheEnd);
  }
});

onBeforeUnmount(() => {
  map.value!.removeEventListener("resize", onWindowResize);
  map.value!.removeEventListener("mousedown", onMapMouseDown);
  map.value!.removeEventListener("mousemove", onMapMouseMove);
  map.value!.removeEventListener("mouseup", onMapMouseUp);
  map.value!.removeEventListener("touchstart", onTouchStart);
  map.value!.removeEventListener("touchmove", onTouchMove);
  map.value!.removeEventListener("touchend", onToucheEnd);
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
    zIndex: 99 - mapTile.x + mapTile.y,
  };
}

function onWindowResize(): void {
  mapStore.windowWidth = window.innerWidth;
  mapStore.windowHeight = window.innerHeight;
  mapStore.adjustMapTileSizeToScreen();
  mapStore.updateCenterPosition();
}

function onMapMouseDown(event: MouseEvent): void {
  if (mapStore.mapControlsDisabled || event.button !== 0) {
    return;
  }

  isDragging.value = true;
  mouseDownTime.value = Date.now();
}

function onMapMouseMove(event: MouseEvent): void {
  if (!isDragging.value) {
    return;
  }

  setTimeout(() => {
    mapStore.offsetX += event.movementX;
    mapStore.offsetY += event.movementY;
  });
}

function onMapMouseUp(): void {
  isDragging.value = false;
  mapStore.updateCenterPosition();
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
  mapStore.updateCenterPosition();
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
