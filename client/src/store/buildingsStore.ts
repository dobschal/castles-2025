import { defineStore } from "pinia";
import { ref } from "vue";
import { BuildingEntity } from "@/types/model/BuildingEntity.ts";

export const useBuildingsStore = defineStore("buildings", () => {
  const buildings = ref<Array<BuildingEntity>>([]);

  return {
    buildings,
  };
});
