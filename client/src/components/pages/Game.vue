<template>
  <div class="map-wrapper" :class="{ loading: isLoading }">
    <canvas ref="canvas"></canvas>
  </div>
  <div v-if="isLoading" class="loading-indicator">
    <img src="@/assets/logo_black.svg" alt="Castles" />
    <p>{{ t("general.loading") }}</p>
    <div class="bar">
      <div :style="{ width: 100 * (assetsLoaded / assetsToLoad) + '%' }"></div>
    </div>
  </div>
  <EventsOverlay v-if="!actionStore.isActionActive" />
  <ActionOverlay />
  <StatsOverlay />
  <TutorialOverlay />
  <ZoomOverlay v-if="!actionStore.isActionActive" />
  <div ref="assetHolder" class="asset-holder"></div>
</template>

<script setup lang="ts">
import ActionOverlay from "@/components/partials/game/overlays/ActionOverlay.vue";
import { onBeforeUnmount, onMounted, ref, watch } from "vue";
import { useBuildingsStore } from "@/store/buildingsStore.ts";
import { useMapStore } from "@/store/mapStore.ts";
import { useAuthStore } from "@/store/authStore.ts";
import { useUnitsStore } from "@/store/unitsStore.ts";
import { useEventsStore } from "@/store/eventsStore.ts";
import EventsOverlay from "@/components/partials/game/overlays/EventsOverlay.vue";
import { useActionStore } from "@/store/actionStore.ts";
import { usePricesStore } from "@/store/pricesStore.ts";
import { useI18n } from "vue-i18n";
import { DIALOG } from "@/events.ts";
import { useTutorialStore } from "@/store/tutorialStore.ts";
import TutorialOverlay from "@/components/partials/game/overlays/TutorialOverlay.vue";
import ZoomOverlay from "@/components/partials/game/overlays/MapControlOverlay.vue";
import { useRoute } from "vue-router";
import router from "@/core/router.ts";
import { Optional } from "@/types/core/Optional.ts";
import { useMapRenderer } from "@/composables/game/MapRenderer.ts";
import StatsOverlay from "@/components/partials/game/overlays/StatsOverlay.vue";

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
const { t } = useI18n();
const route = useRoute();
let eventLoopTimeout: ReturnType<typeof setTimeout>;
const canvas = ref<HTMLCanvasElement>();
const assetHolder = ref<Optional<HTMLDivElement>>();
const images = import.meta.glob("@/assets/tiles/*-min.png");
const cachedImageAssets: Array<HTMLImageElement> = [];
const assetsToLoad = ref(0);
const assetsLoaded = ref(0);

useMapRenderer(canvas);

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
  setTimeout(() => {
    isLoading.value = false;
  }, 500);

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
  assetsToLoad.value = Object.keys(images).length;
  for (const imagesKey in images) {
    const image = (await images[imagesKey]()) as { default: string };
    const url: string = image.default;

    promises.push(
      new Promise((resolve) => {
        const img = new Image();
        img.onload = () => {
          cachedImageAssets.push(img);
          resolve();
          assetsLoaded.value++;
        };

        img.src = url;
        assetHolder.value?.appendChild(img);
      }),
    );
  }

  await Promise.all(promises);

  console.info(
    "[Game] Loaded ",
    promises.length,
    " image assets in",
    Date.now() - t1,
    "ms",
  );
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

.asset-holder {
  opacity: 0;
  pointer-events: none;
  position: absolute;
}

.loading-indicator {
  position: fixed;
  top: 50%;
  left: 50%;
  font-size: 1rem;
  color: black;
  text-align: center;
  width: 4rem;
  margin-left: -2rem;
  margin-top: -2rem;

  img {
    width: 100%;
    animation: pulsate 2s infinite;
  }

  .bar {
    width: 150%;
    height: 10px;
    background: #ccc;
    border: solid 1px black;
    padding: 1px;
    border-radius: 0.25rem;
    overflow: hidden;
    margin-left: -22%;

    div {
      height: 100%;
      background: yellowgreen;
      background: linear-gradient(to left, green 0%, yellowgreen 100%);
      transition: width 0.5s ease-in-out;
    }
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
