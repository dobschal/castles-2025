import { MapGateway } from "@/gateways/MapGateway.ts";
import { useMapStore } from "@/store/mapStore.ts";
import { computed, ComputedRef, Ref, ref } from "vue";
import { LOADED_MAP_TILES } from "@/events.ts";
import { TwoPointDto } from "@/types/dto/TwoPointDto.ts";

export interface MapLogic {
  load: () => Promise<void>;
  centerPosition: ComputedRef<{ x: number; y: number }>;
  windowWidth: Ref<number>;
  windowHeight: Ref<number>;
  offsetX: Ref<number>;
  offsetY: Ref<number>;
  mapTileSize: Ref<number>;
}

export function useMapLogic(): MapLogic {
  const mapStore = useMapStore();
  const mapTileSize = ref(100);
  const windowWidth = ref(window.innerWidth);
  const windowHeight = ref(window.innerHeight);
  const offsetX = ref(0);
  const offsetY = ref(0);

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

  async function load(): Promise<void> {
    const amountOfTiles = Math.max(amountOfTilesX.value, amountOfTilesY.value);
    const range: TwoPointDto = {
      x1: Math.floor(centerPosition.value.x - amountOfTiles / 1.5),
      y1: Math.floor(centerPosition.value.y - amountOfTiles / 1.5),
      x2: Math.ceil(centerPosition.value.x + amountOfTiles / 1.5),
      y2: Math.ceil(centerPosition.value.y + amountOfTiles / 1.5),
    };
    mapStore.mapTiles = await MapGateway.instance.getMapTiles(range);
    LOADED_MAP_TILES.dispatch(range);
  }

  return {
    load,
    centerPosition,
    windowWidth,
    windowHeight,
    offsetX,
    offsetY,
    mapTileSize,
  };
}
