import { defineStore } from "pinia";
import { ref } from "vue";
import { MapTileEntity } from "@/types/model/MapTileEntity.ts";

export const useMapStore = defineStore(
  "map",
  () => {
    const mapTiles = ref<Array<MapTileEntity>>([]);

    return {
      mapTiles,
    };
  },
  { persist: true },
);
