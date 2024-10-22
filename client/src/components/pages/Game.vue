<template>
  <div class="map-wrapper">
    <MapLayer></MapLayer>
  </div>
  <ActionOverlay />
</template>

<script setup lang="ts">
import MapLayer from "@/components/partials/game/MapLayer.vue";
import ActionOverlay from "@/components/partials/game/ActionOverlay.vue";
import { onMounted, watch } from "vue";
import { useBuildingsStore } from "@/store/buildingsStore.ts";
import { useMapStore } from "@/store/mapStore.ts";

const buildingsStore = useBuildingsStore();
const mapStore = useMapStore();

onMounted(async () => {
  // We need to make a first map load e.g. if no start village is
  // given...
  await mapStore.loadMap();
  await buildingsStore.loadBuildings();
  // As we are changing the map position, we need don't need to
  // explicit load the map and buildings afterward. The watchers
  // will take care of that.
  await buildingsStore.loadStartVillage();
  mapStore.goToPosition(buildingsStore.startVillage ?? { x: 0, y: 0 });
});

watch(
  () => mapStore.centerPosition,
  async () => {
    await mapStore.loadMap();
    await buildingsStore.loadBuildings();
  },
);
</script>

<style lang="scss" scoped>
.map-wrapper {
  position: relative;
  width: 100%;
  height: calc(100vh - 4rem);
  overflow: hidden;
  background: black;
}
</style>
