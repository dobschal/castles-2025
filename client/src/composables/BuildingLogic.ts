import { onBeforeUnmount, onMounted } from "vue";
import { BuildingGateway } from "@/gateways/BuildingGateway.ts";
import { ACTION, LOADED_MAP_TILES } from "@/events.ts";
import StartVillageAction from "@/components/partials/game/actions/StartVillageAction.vue";
import { useMapStore } from "@/store/mapStore.ts";
import { handleFatalError } from "@/core/util.ts";
import { TwoPointDto } from "@/types/dto/TwoPointDto.ts";
import { useBuildingsStore } from "@/store/buildingsStore.ts";

export function useBuildingLogic(): void {
  const mapStore = useMapStore();
  const buildingsStore = useBuildingsStore();

  onMounted(async () => {
    await getStartVillage();
    LOADED_MAP_TILES.on(loadBuildings);
  });

  onBeforeUnmount(() => {
    LOADED_MAP_TILES.off(loadBuildings);
  });

  async function getStartVillage(): Promise<void> {
    try {
      mapStore.startVillage = await BuildingGateway.instance.getStartVillage();
      console.info("Start village: ", mapStore.startVillage);
    } catch (error) {
      if (error instanceof Response && error.status === 404) {
        await ACTION.dispatch(StartVillageAction);
        await getStartVillage();
      } else {
        handleFatalError(error);
      }
    }
  }

  async function loadBuildings(range: TwoPointDto): Promise<void> {
    buildingsStore.buildings =
      await BuildingGateway.instance.getBuildings(range);
  }
}
