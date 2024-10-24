<template>
  <div class="map-wrapper">
    <MapLayer></MapLayer>
  </div>
  <EventsOverlay v-if="!actionStore.isActionActive" />
  <ActionOverlay />
  <StatsOverlay />
</template>

<script setup lang="ts">
import MapLayer from "@/components/partials/game/MapLayer.vue";
import ActionOverlay from "@/components/partials/game/ActionOverlay.vue";
import { onBeforeUnmount, onMounted, watch } from "vue";
import { useBuildingsStore } from "@/store/buildingsStore.ts";
import { useMapStore } from "@/store/mapStore.ts";
import { useAuthStore } from "@/store/authStore.ts";
import { useUnitsStore } from "@/store/unitsStore.ts";
import { useEventsStore } from "@/store/eventsStore.ts";
import EventsOverlay from "@/components/partials/game/EventsOverlay.vue";
import { useActionStore } from "@/store/actionStore.ts";
import StatsOverlay from "@/components/partials/game/StatsOverlay.vue";

const buildingsStore = useBuildingsStore();
const mapStore = useMapStore();
const authStore = useAuthStore();
const unitsStore = useUnitsStore();
const eventsStore = useEventsStore();
const actionStore = useActionStore();
let isMounted = false;

onMounted(async () => {
  isMounted = true;
  await authStore.loadUser();
  // We need to make a first map load e.g. if no start village is
  // given...
  await mapStore.loadMap();
  await buildingsStore.loadBuildings();
  await unitsStore.loadUnits();
  // As we are changing the map position, we need don't need to
  // explicit load the map and buildings afterward. The watchers
  // will take care of that.
  await buildingsStore.loadStartVillage();
  mapStore.goToPosition(buildingsStore.startVillage ?? { x: 0, y: 0 });
  await keepLoadingEvents();
});

onBeforeUnmount(() => {
  isMounted = false;
});

watch(
  () => mapStore.centerPosition,
  async () => {
    await mapStore.loadMap();
    await buildingsStore.loadBuildings();
    await unitsStore.loadUnits();
  },
);

watch(
  () => eventsStore.events,
  async () => {
    await authStore.loadUser();
    await buildingsStore.loadBuildings();
    await unitsStore.loadUnits();
  },
  { deep: true },
);

async function keepLoadingEvents(): Promise<void> {
  await eventsStore.loadEvents();
  setTimeout(() => {
    if (isMounted) {
      keepLoadingEvents();
    }
  }, 500);
}
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
