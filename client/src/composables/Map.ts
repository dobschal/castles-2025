import { MapGateway } from "@/gateways/MapGateway.ts";
import { useMapStore } from "@/store/mapStore.ts";
import { computed, ComputedRef, Ref, ref } from "vue";

export interface Map {
  load: () => Promise<void>;
  centerPosition: ComputedRef<{ x: number; y: number }>;
  windowWidth: Ref<number>;
  windowHeight: Ref<number>;
  offsetX: Ref<number>;
  offsetY: Ref<number>;
  mapTileSize: Ref<number>;
}

export function useMap(): Map {
  const mapStore = useMapStore();
  const mapTileSize = ref(100);
  const mapTileDiagonal = ref(Math.sqrt(2 * Math.pow(mapTileSize.value, 2)));
  const windowWidth = ref(window.innerWidth);
  const windowHeight = ref(window.innerHeight);
  const offsetX = ref(0);
  const offsetY = ref(0);

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
    const centerX = Math.round(
      (-offsetX.value + windowWidth.value / 2) / mapTileDiagonal.value,
    );
    const centerY = Math.round(
      (-offsetY.value - windowHeight.value / 2) / mapTileDiagonal.value,
    );

    return {
      x: Math.round(centerX - centerY),
      y: Math.round(centerY + centerX),
    };
  });

  async function load(): Promise<void> {
    const amountOfTiles = Math.max(amountOfTilesX.value, amountOfTilesY.value);
    mapStore.mapTiles = await MapGateway.instance.getMapTiles({
      x1: Math.floor(centerPosition.value.x - amountOfTiles),
      y1: Math.floor(centerPosition.value.y - amountOfTiles),
      x2: Math.ceil(centerPosition.value.x + amountOfTiles),
      y2: Math.ceil(centerPosition.value.y + amountOfTiles),
    });
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
