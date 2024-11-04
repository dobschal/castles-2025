import { defineStore } from "pinia";
import { computed, ref } from "vue";
import { BuildingEntity } from "@/types/model/BuildingEntity.ts";
import { Optional } from "@/types/core/Optional.ts";
import { BuildingGateway } from "@/gateways/BuildingGateway.ts";
import { ACTION } from "@/events.ts";
import StartVillageAction from "@/components/partials/game/actions/StartVillageAction.vue";
import {
  delay,
  handleFatalError,
  NOW,
  parseServerDateString,
} from "@/core/util.ts";
import { useMapStore } from "@/store/mapStore.ts";
import { Queue } from "@/core/Queue.ts";
import { BuildingType } from "@/types/enum/BuildingType.ts";
import { useAuthStore } from "@/store/authStore.ts";
import { useEventsStore } from "@/store/eventsStore.ts";
import { EventType } from "@/types/enum/EventType.ts";

export const useBuildingsStore = defineStore("buildings", () => {
  const mapStore = useMapStore();
  const authStore = useAuthStore();
  const eventsStore = useEventsStore();
  const buildings = ref<Array<BuildingEntity>>([]);
  const startVillage = ref<Optional<BuildingEntity>>();
  const activeBuilding = ref<Optional<BuildingEntity>>();
  const loadBuildingsQueue = new Queue(500, 3);
  const breweryBeerProductionPerHour = ref<number>(-1);
  const breweryBeerStorage = ref<number>(-1);
  const villageLevel1BeerStorage = ref<number>(-1);
  const amountOfOwnVillages = ref<number>(-1);

  const maxBeerStorage = computed(() => {
    return villageLevel1BeerStorage.value * amountOfOwnVillages.value;
  });

  async function loadStartVillage(): Promise<void> {
    try {
      startVillage.value = await BuildingGateway.instance.getStartVillage();
      console.info("Start village: ", startVillage.value);
    } catch (error) {
      if (error instanceof Response && error.status === 404) {
        // Show the original map for two seconds and then
        // dispatch the start village action that grays out the map
        await delay(2000);
        await ACTION.dispatch(StartVillageAction);
        await loadStartVillage();
      } else {
        handleFatalError(error);
      }
    }
  }

  async function loadBuildings(): Promise<void> {
    await loadBuildingsQueue.add(async () => {
      try {
        const response = await BuildingGateway.instance.getBuildings(
          mapStore.currentMapRange,
        );
        buildings.value = response.buildings;
        breweryBeerProductionPerHour.value =
          response.breweryBeerProductionPerHour;
        breweryBeerStorage.value = response.breweryBeerStorage;
        villageLevel1BeerStorage.value = response.villageLevel1BeerStorage;
        amountOfOwnVillages.value = response.amountOfVillages;
      } catch (e) {
        handleFatalError(e);
      }
    });
  }

  function findFarmNextTo(
    x: number,
    y: number,
    userId: number,
  ): Optional<BuildingEntity> {
    return buildings.value.find((building) => {
      return (
        building.type === BuildingType.FARM &&
        Math.abs(building.x - x) <= 1 &&
        Math.abs(building.y - y) <= 1 &&
        building.user.id === userId
      );
    });
  }

  function calculateBeerToCollect(building: BuildingEntity): number {
    if (
      building.type !== BuildingType.BREWERY ||
      building.user.id !== authStore.user?.id
    ) {
      return 0;
    }

    // TODO: Actually that fails because we might not have the correct event in the store...

    const event = eventsStore.findLatestEventByPositionAndType(
      building.x,
      building.y,
      EventType.BEER_COLLECTED,
    );

    const beerCollectedAt = parseServerDateString(event?.createdAt);
    const timePassed = NOW.value - beerCollectedAt.getTime();
    const hoursPassed = timePassed / 1000 / 60 / 60;
    const beerToCollect = Math.floor(
      hoursPassed * breweryBeerProductionPerHour.value,
    );

    return Math.min(beerToCollect, breweryBeerStorage.value);
  }

  return {
    maxBeerStorage,
    breweryBeerProductionPerHour,
    calculateBeerToCollect,
    loadBuildings,
    loadStartVillage,
    buildings,
    startVillage,
    activeBuilding,
    findFarmNextTo,
  };
});
