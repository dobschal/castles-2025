import { defineStore } from "pinia";
import { ref } from "vue";
import { BuildingEntity } from "@/types/model/BuildingEntity.ts";
import { Optional } from "@/types/core/Optional.ts";
import { BuildingGateway } from "@/gateways/BuildingGateway.ts";
import { ACTION } from "@/events.ts";
import StartVillageAction from "@/components/partials/game/actions/StartVillageAction.vue";
import { handleFatalError } from "@/core/util.ts";
import { useMapStore } from "@/store/mapStore.ts";
import { Queue } from "@/core/Queue.ts";
import { BuildingType } from "@/types/enum/BuildingType.ts";

export const useBuildingsStore = defineStore("buildings", () => {
  const mapStore = useMapStore();
  const buildings = ref<Array<BuildingEntity>>([]);
  const startVillage = ref<Optional<BuildingEntity>>();
  const activeBuilding = ref<Optional<BuildingEntity>>();
  const loadBuildingsQueue = new Queue(500, 3);

  async function loadStartVillage(): Promise<void> {
    try {
      startVillage.value = await BuildingGateway.instance.getStartVillage();
      console.info("Start village: ", startVillage.value);
    } catch (error) {
      if (error instanceof Response && error.status === 404) {
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
        buildings.value = await BuildingGateway.instance.getBuildings(
          mapStore.currentMapRange,
        );
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

  return {
    loadBuildings,
    loadStartVillage,
    buildings,
    startVillage,
    activeBuilding,
    findFarmNextTo,
  };
});
