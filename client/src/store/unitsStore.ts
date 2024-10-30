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
  const activeMoveUnit = ref<Optional<UnitEntity>>();
  const loadUnitsQueue = new Queue(500, 3);
  const workerMovesPerHour = ref(-1);
  const spearmanMovesPerHour = ref(-1);
  const swordsmanMovesPerHour = ref(-1);
  const horsemanMovesPerHour = ref(-1);

  async function loadUnits(): Promise<void> {
    await loadUnitsQueue.add(async () => {
      try {
        const response = await UnitGateway.instance.getUnits(
          mapStore.currentMapRange,
        );
        units.value = response.units;
        workerMovesPerHour.value = response.workerMovesPerHour;
        spearmanMovesPerHour.value = response.spearmanMovesPerHour;
        swordsmanMovesPerHour.value = response.swordsmanMovesPerHour;
        horsemanMovesPerHour.value = response.horsemanMovesPerHour;
      } catch (e) {
        handleFatalError(e);
      }
    });
  }

  return {
    loadUnits,
    units,
    activeUnit,
    activeMoveUnit,
    workerMovesPerHour,
    spearmanMovesPerHour,
    swordsmanMovesPerHour,
    horsemanMovesPerHour,
  };
});
