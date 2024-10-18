import { defineStore } from "pinia";
import { ref } from "vue";
import { MapTileDto } from "@/types/dto/MapTileDto.ts";
import { BuildingEntity } from "@/types/model/BuildingEntity.ts";
import { Optional } from "@/types/core/Optional.ts";

export const useMapStore = defineStore(
  "map",
  () => {
    const mapTiles = ref<Array<MapTileDto>>([]);
    const startVillage = ref<Optional<BuildingEntity>>();

    return {
      mapTiles,
      startVillage,
    };
  },
  { persist: true },
);
