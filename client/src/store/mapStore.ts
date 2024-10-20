import { defineStore } from "pinia";
import { computed, ref } from "vue";
import { MapTileDto } from "@/types/dto/MapTileDto.ts";
import { TwoPointDto } from "@/types/dto/TwoPointDto.ts";
import { MapGateway } from "@/gateways/MapGateway.ts";
import { handleFatalError } from "@/core/util.ts";
import { PointDto } from "@/types/dto/PointDto.ts";

export const useMapStore = defineStore(
  "map",
  () => {
    const mapTiles = ref<Array<MapTileDto>>([]);
    const mapTileSize = ref(100);
    const windowWidth = ref(window.innerWidth);
    const windowHeight = ref(window.innerHeight);
    const offsetX = ref(0);
    const offsetY = ref(0);
    const isLoadingMap = ref(false);

    // Used to calculate the rotation of the map
    const radians = 45 * (Math.PI / 180);
    const cos = Math.cos(radians);
    const sin = Math.sin(radians);

    // The amount of tiles in x and y direction is calculated
    // based on the window size and the tile size
    const amountOfTilesX = computed(() =>
      Math.ceil(windowWidth.value / mapTileSize.value),
    );
    const amountOfTilesY = computed(() =>
      Math.ceil(windowHeight.value / mapTileSize.value),
    );

    // The center position is used to load the right amount of
    // tiles around the player. This is actually hard to calculate
    // because the map is rotated 45deg... if it is working, leave it as it is
    const centerPosition = computed(() => {
      const screenX = (windowWidth.value - offsetX.value * 2) / 2;
      const screenY = (windowHeight.value - 64 - offsetY.value * 2) / 2; // 64px is the height of the top bar

      const rotatedScreenX = screenX * cos - screenY * sin;
      const rotatedScreenY = screenX * sin + screenY * cos;

      const x = Math.round(rotatedScreenX / mapTileSize.value);
      const y = Math.round(rotatedScreenY / mapTileSize.value);

      return { x, y };
    });

    const currentMapRange = computed<TwoPointDto>(() => {
      const amountOfTiles = Math.max(
        amountOfTilesX.value,
        amountOfTilesY.value,
      );

      return {
        x1: Math.floor(centerPosition.value.x - amountOfTiles / 1.5),
        y1: Math.floor(centerPosition.value.y - amountOfTiles / 1.5),
        x2: Math.ceil(centerPosition.value.x + amountOfTiles / 1.5),
        y2: Math.ceil(centerPosition.value.y + amountOfTiles / 1.5),
      };
    });

    function getOffset(
      windowWidth: number,
      windowHeight: number,
      x: number,
      y: number,
    ): {
      offsetX: number;
      offsetY: number;
    } {
      const screenX = x * mapTileSize.value;
      const screenY = y * mapTileSize.value;

      const rotatedScreenX = screenX * cos + screenY * sin;
      const rotatedScreenY = -screenX * sin + screenY * cos;

      // const centerX = (windowWidth - rotatedScreenX * 2) / 2;
      // const centerY = (windowHeight - 64 - rotatedScreenY * 2) / 2; // 64px is the height of the top bar

      const offsetX = (windowWidth / 2 - rotatedScreenX) / 2;
      const offsetY = (windowHeight / 2 - 64 - rotatedScreenY) / 2;

      return {
        offsetX: Math.round(offsetX),
        offsetY: Math.round(offsetY),
      };
    }

    function goToPosition({ x, y }: PointDto): void {
      const offset = getOffset(windowWidth.value, windowHeight.value, x, y);
      offsetX.value = offset.offsetX;
      offsetY.value = offset.offsetY;
      console.info("Go to position", x, y, offsetX.value, offsetY.value);
    }

    async function loadMap(): Promise<void> {
      if (isLoadingMap.value) return;
      try {
        isLoadingMap.value = true;
        mapTiles.value = await MapGateway.instance.getMapTiles(
          currentMapRange.value,
        );
      } catch (e) {
        handleFatalError(e);
      } finally {
        isLoadingMap.value = false;
      }
    }

    return {
      loadMap,
      goToPosition,
      mapTiles,
      currentMapRange,
      centerPosition,
      windowWidth,
      windowHeight,
      offsetX,
      offsetY,
      mapTileSize,
    };
  },
  { persist: true },
);
