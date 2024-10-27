<template>
  <div class="map-wrapper" :class="{ loading: isLoading }">
    <MapLayer></MapLayer>
  </div>
  <div v-if="isLoading" class="loading-indicator">
    <img src="@/assets/logo_black.svg" alt="Castles" />
    <p>{{ t("general.loading") }}</p>
  </div>
  <EventsOverlay v-if="!actionStore.isActionActive" />
  <ActionOverlay />
  <StatsOverlay />
</template>

<script setup lang="ts">
import MapLayer from "@/components/partials/game/MapLayer.vue";
import ActionOverlay from "@/components/partials/game/ActionOverlay.vue";
import { onBeforeUnmount, onMounted, ref, watch } from "vue";
import { useBuildingsStore } from "@/store/buildingsStore.ts";
import { useMapStore } from "@/store/mapStore.ts";
import { useAuthStore } from "@/store/authStore.ts";
import { useUnitsStore } from "@/store/unitsStore.ts";
import { useEventsStore } from "@/store/eventsStore.ts";
import EventsOverlay from "@/components/partials/game/EventsOverlay.vue";
import { useActionStore } from "@/store/actionStore.ts";
import StatsOverlay from "@/components/partials/game/StatsOverlay.vue";
import { usePricesStore } from "@/store/pricesStore.ts";
import { Optional } from "@/types/core/Optional.ts";
import breweryImage from "@/assets/tiles/brewery.png";
import breweryDisabledImage from "@/assets/tiles/brewery-disabled.png";
import breweryTopLayerImage from "@/assets/tiles/brewery-top-layer.png";
import breweryTopLayerDisabledImage from "@/assets/tiles/brewery-top-layer-disabled.png";
import farmImage from "@/assets/tiles/farm.png";
import farmDisabledImage from "@/assets/tiles/farm-disabled.png";
import farmTopLayerImage from "@/assets/tiles/farm-top-layer.png";
import farmTopLayerDisabledImage from "@/assets/tiles/farm-top-layer-disabled.png";
import forestImage from "@/assets/tiles/forest.png";
import forestDisabledImage from "@/assets/tiles/forest-disabled.png";
import forestTopLayerImage from "@/assets/tiles/forest-top-layer.png";
import forestTopLayerDisabledImage from "@/assets/tiles/forest-top-layer-disabled.png";
import mountainImage from "@/assets/tiles/mountain.png";
import mountainDisabledImage from "@/assets/tiles/mountain-disabled.png";
import plainImage from "@/assets/tiles/plain.png";
import plainDisabledImage from "@/assets/tiles/plain-disabled.png";
import villageImage from "@/assets/tiles/village.png";
import villageDisabledImage from "@/assets/tiles/village-disabled.png";
import villageTopLayerImage from "@/assets/tiles/village-top-layer.png";
import villageTopLayerDisabledImage from "@/assets/tiles/village-top-layer-disabled.png";
import waterImage from "@/assets/tiles/water.png";
import waterDisabledImage from "@/assets/tiles/water-disabled.png";
import workerImage from "@/assets/tiles/worker.png";
import workerDisabledImage from "@/assets/tiles/worker-disabled.png";
import { useI18n } from "vue-i18n";

const buildingsStore = useBuildingsStore();
const mapStore = useMapStore();
const authStore = useAuthStore();
const unitsStore = useUnitsStore();
const eventsStore = useEventsStore();
const actionStore = useActionStore();
const pricesStore = usePricesStore();
let isMounted = false;
let loadTimeout: Optional<ReturnType<typeof setTimeout>>;
const isLoading = ref(false);
const cachedImageAssets: Array<HTMLImageElement> = [];
const { t } = useI18n();

onMounted(async () => {
  isMounted = true;
  isLoading.value = true;
  await Promise.all([
    loadAssets(),
    authStore.loadUser(),
    pricesStore.loadPrices(),
    mapStore.loadMap(),
    buildingsStore.loadBuildings(),
    unitsStore.loadUnits(),
    keepLoadingEvents(),
  ]);
  setTimeout(() => (isLoading.value = false), 500);
  // The promise here might be resolved very late if the user
  // needs to select the start village
  await buildingsStore.loadStartVillage();
  mapStore.goToPosition(buildingsStore.startVillage ?? { x: 0, y: 0 });
});

onBeforeUnmount(() => {
  isMounted = false;
});

watch(
  () => mapStore.centerPosition,
  () => {
    if (loadTimeout) {
      clearTimeout(loadTimeout);
    }

    loadTimeout = setTimeout(async () => {
      await Promise.all([
        mapStore.loadMap(),
        buildingsStore.loadBuildings(),
        unitsStore.loadUnits(),
      ]);
    }, 200);
  },
);

watch(
  () => eventsStore.events,
  () => {
    setTimeout(async () => {
      await Promise.all([
        authStore.loadUser(),
        buildingsStore.loadBuildings(),
        unitsStore.loadUnits(),
        pricesStore.loadPrices(),
      ]);
    });
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

async function loadAssets(): Promise<void> {
  const t1 = Date.now();
  const images = [
    breweryImage,
    breweryDisabledImage,
    breweryTopLayerImage,
    breweryTopLayerDisabledImage,
    farmImage,
    farmDisabledImage,
    farmTopLayerImage,
    farmTopLayerDisabledImage,
    forestImage,
    forestDisabledImage,
    forestTopLayerImage,
    forestTopLayerDisabledImage,
    mountainImage,
    mountainDisabledImage,
    plainImage,
    plainDisabledImage,
    villageImage,
    villageDisabledImage,
    villageTopLayerImage,
    villageTopLayerDisabledImage,
    waterImage,
    waterDisabledImage,
    workerImage,
    workerDisabledImage,
  ];
  await Promise.all(
    images.map((image) => {
      return new Promise<void>((resolve) => {
        const img = new Image();
        img.src = image;
        img.onload = () => resolve();
        cachedImageAssets.push(img);
      });
    }),
  );
  console.info("Loaded assets in", Date.now() - t1, "ms");
}
</script>

<style lang="scss" scoped>
@keyframes pulsate {
  0% {
    transform: scale(0.9);
    opacity: 0.5;
  }
  50% {
    transform: scale(1.1);
    opacity: 1;
  }
  100% {
    transform: scale(0.9);
    opacity: 0.5;
  }
}

.loading-indicator {
  position: fixed;
  top: 50%;
  left: 50%;
  font-size: 1rem;
  color: black;
  animation: pulsate 2s infinite;
  text-align: center;
  width: 4rem;
  margin-left: -2rem;
  margin-top: -2rem;

  img {
    width: 100%;
  }
}

.map-wrapper {
  position: relative;
  width: 100%;
  height: calc(100vh - 4rem);
  overflow: hidden;
  background: black;
  transition: opacity 0.5s ease-in-out;

  &.loading {
    opacity: 0;
  }
}
</style>
