import { defineStore } from "pinia";
import { ref } from "vue";
import { MapTile } from "@/types/model/MapTile.ts";

export const useMapStore = defineStore(
  "map",
  () => {
    const mapTiles = ref<Array<MapTile>>([]);

    return {
      mapTiles,
    };
  },
  { persist: true },
);
