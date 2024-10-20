import { defineStore } from "pinia";
import { ref } from "vue";
import { BuildingEntity } from "@/types/model/BuildingEntity.ts";
import { Optional } from "@/types/core/Optional.ts";
import { BuildingGateway } from "@/gateways/BuildingGateway.ts";
import { ACTION } from "@/events.ts";
import StartVillageAction from "@/components/partials/game/actions/StartVillageAction.vue";
import { handleFatalError } from "@/core/util.ts";
import { useMapStore } from "@/store/mapStore.ts";

export const useBuildingsStore = defineStore("buildings", () => {
  const mapStore = useMapStore();
  const buildings = ref<Array<BuildingEntity>>([]);
  const startVillage = ref<Optional<BuildingEntity>>();
  const isLoadingBuildings = ref(false);

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
    if (isLoadingBuildings.value) return;
    try {
      isLoadingBuildings.value = true;
      buildings.value = await BuildingGateway.instance.getBuildings(
        mapStore.currentMapRange,
      );
    } catch (e) {
      handleFatalError(e);
    } finally {
      isLoadingBuildings.value = false;
    }
  }

  return {
    loadBuildings,
    loadStartVillage,
    buildings,
    startVillage,
  };
});
