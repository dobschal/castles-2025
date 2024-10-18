import { onMounted } from "vue";
import { BuildingGateway } from "@/gateways/BuildingGateway.ts";
import { ACTION } from "@/events.ts";
import StartVillageAction from "@/components/partials/game/actions/StartVillageAction.vue";
import { useMapStore } from "@/store/mapStore.ts";
import { handleFatalError } from "@/core/util.ts";

export function useGame(): void {
  const mapStore = useMapStore();

  onMounted(async () => {
    await getStartVillage();
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
}
