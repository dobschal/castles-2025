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
  <TutorialOverlay />
  <ZoomOverlay v-if="!actionStore.isActionActive" />
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
import { useI18n } from "vue-i18n";
import { DIALOG } from "@/events.ts";
import { useTutorialStore } from "@/store/tutorialStore.ts";
import TutorialOverlay from "@/components/partials/game/TutorialOverlay.vue";
import ZoomOverlay from "@/components/partials/game/MapControlOverlay.vue";
import { useRoute } from "vue-router";
import router from "@/core/router.ts";

const images = import.meta.glob("@/assets/tiles/*-min.png");
const buildingsStore = useBuildingsStore();
const mapStore = useMapStore();
const authStore = useAuthStore();
const unitsStore = useUnitsStore();
const eventsStore = useEventsStore();
const actionStore = useActionStore();
const pricesStore = usePricesStore();
const tutorialStore = useTutorialStore();
let isMounted = false;
const isLoading = ref(false);
const cachedImageAssets: Array<HTMLImageElement> = [];
const { t } = useI18n();
const route = useRoute();
let eventLoopTimeout: ReturnType<typeof setTimeout>;

onMounted(async () => {
  isMounted = true;
  isLoading.value = true;
  mapStore.adjustMapTileSizeToScreen();
  await Promise.all([
    loadAssets(),
    authStore.loadUser(),
    pricesStore.loadPrices(),
    mapStore.loadMap(),
    buildingsStore.loadBuildings(),
    unitsStore.loadUnits(),
    keepLoadingEvents(),
  ]);
  isLoading.value = false;

  // The promise here might be resolved very late if the user
  // needs to select the start village
  await buildingsStore.loadStartVillage();

  // If the user is redirected to the map with x and y query params
  // we need to go to that position.
  if (route.query.x && route.query.y) {
    mapStore.goToPosition({
      x: Number(route.query.x),
      y: Number(route.query.y),
    });
  } else {
    mapStore.goToPosition(buildingsStore.startVillage ?? { x: 0, y: 0 });
  }

  setTimeout(async () => {
    await tutorialStore.loadAndShowTutorial();
  }, 2000);
});

onBeforeUnmount(() => {
  isMounted = false;
});

watch(
  () => mapStore.centerPosition,
  async () => {
    await router.replace({
      query: {
        x: mapStore.centerPosition.x,
        y: mapStore.centerPosition.y,
      },
    });
    await Promise.all([
      mapStore.loadMap(),
      buildingsStore.loadBuildings(),
      unitsStore.loadUnits(),
    ]);
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

watch(
  () => eventsStore.ownActionHappened,
  () => {
    if (eventsStore.ownActionHappened) {
      eventsStore.ownActionHappened = false;
      keepLoadingEvents();
    }
  },
);

async function keepLoadingEvents(): Promise<void> {
  try {
    if (eventLoopTimeout) {
      clearTimeout(eventLoopTimeout);
    }

    await eventsStore.loadEvents();
    eventLoopTimeout = setTimeout(() => {
      if (isMounted) keepLoadingEvents();
    }, 1000);
  } catch (e) {
    console.error("Error while loading events: ", e);
    DIALOG.dispatch({
      questionKey: "general.serverError",
      onYes: () => {
        window.location.reload();
      },
    });
  }
}

async function loadAssets(): Promise<void> {
  const t1 = Date.now();
  const promises: Array<Promise<void>> = [];
  for (const imagesKey in images) {
    const image = (await images[imagesKey]()) as { default: string };
    const url: string = image.default;

    promises.push(
      new Promise((resolve) => {
        const img = new Image();
        img.onload = () => {
          cachedImageAssets.push(img);
          resolve();
        };

        img.src = url;
      }),
    );
  }

  await Promise.all(promises);

  console.info("Loaded ", promises.length, " assets in", Date.now() - t1, "ms");
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
