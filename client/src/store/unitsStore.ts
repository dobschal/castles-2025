import { defineStore } from "pinia";
import { handleFatalError } from "@/core/util.ts";
import { UnitGateway } from "@/gateways/UnitGateway.ts";
import { ref } from "vue";
import { UnitEntity } from "@/types/model/UnitEntity.ts";
import { useMapStore } from "@/store/mapStore.ts";
import { Optional } from "@/types/core/Optional.ts";
import { Queue } from "@/core/Queue.ts";

export const useUnitsStore = defineStore("units", () => {
  const units = ref<Array<UnitEntity>>([]);
  const mapStore = useMapStore();
  const activeUnit = ref<Optional<UnitEntity>>();
  const loadUnitsQueue = new Queue(300, 3);

  async function loadUnits(): Promise<void> {
    await loadUnitsQueue.add(async () => {
      try {
        units.value = await UnitGateway.instance.getUnits(
          mapStore.currentMapRange,
        );
      } catch (e) {
        handleFatalError(e);
      }
    });
  }

  return {
    loadUnits,
    units,
    activeUnit,
  };
});
