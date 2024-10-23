import { defineStore } from "pinia";
import { handleFatalError } from "@/core/util.ts";
import { UnitGateway } from "@/gateways/UnitGateway.ts";
import { ref } from "vue";
import { UnitEntity } from "@/types/model/UnitEntity.ts";
import { useMapStore } from "@/store/mapStore.ts";
import { Optional } from "@/types/core/Optional.ts";

export const useUnitsStore = defineStore("units", () => {
  const units = ref<Array<UnitEntity>>([]);
  const isLoadingUnits = ref(false);
  const mapStore = useMapStore();
  const activeUnit = ref<Optional<UnitEntity>>();

  async function loadUnits(): Promise<void> {
    if (isLoadingUnits.value) return;
    try {
      isLoadingUnits.value = true;
      units.value = await UnitGateway.instance.getUnits(
        mapStore.currentMapRange,
      );
    } catch (e) {
      handleFatalError(e);
    } finally {
      isLoadingUnits.value = false;
    }
  }

  return {
    loadUnits,
    units,
    activeUnit,
  };
});