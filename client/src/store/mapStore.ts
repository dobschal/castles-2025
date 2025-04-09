import { defineStore } from "pinia";
import { computed, ref } from "vue";
import { MapTileDto } from "@/types/dto/MapTileDto.ts";
import { TwoPointDto } from "@/types/dto/TwoPointDto.ts";
import { MapGateway } from "@/gateways/MapGateway.ts";
import { handleFatalError } from "@/core/util.ts";
import { PointDto } from "@/types/dto/PointDto.ts";
import { Queue } from "@/core/Queue.ts";

export const useMapStore = defineStore("map", () => {
  const mapTiles = ref<Array<MapTileDto>>([]);
  const mapTileSize = ref(100);
  const windowWidth = ref(window.innerWidth);
  const windowHeight = ref(window.innerHeight);
  const offsetX = ref(-1);
  const offsetY = ref(-1);
  const mapControlsDisabled = ref(false);
  const loadMapQueue = new Queue(200, 3);
  const allowedZoomMapTileSizes = [
    10, 25, 50, 75, 100, 150, 200, 250, 300, 350, 400, 450, 500, 1000,
  ];
  const centerPosition = ref<PointDto>({
    x: -1,
    y: -1,
  });

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

  const currentMapRange = computed<TwoPointDto>(() => {
    const amountOfTiles = Math.max(amountOfTilesX.value, amountOfTilesY.value);

    return {
      x1: Math.floor(centerPosition.value.x - amountOfTiles / 1.5),
      y1: Math.floor(centerPosition.value.y - amountOfTiles / 1.5),
      x2: Math.ceil(centerPosition.value.x + amountOfTiles / 1.5),
      y2: Math.ceil(centerPosition.value.y + amountOfTiles / 1.5),
    };
  });

  // The center position is used to load the right amount of
  // tiles around the player. This is actually hard to calculate
  // because the map is rotated 45deg... if it is working, leave it as it is
  function updateCenterPosition(): void {
    const screenX = (windowWidth.value - offsetX.value * 2) / 2;
    const screenY = (windowHeight.value - 64 - offsetY.value * 2) / 2; // 64px is the height of the top bar

    const rotatedScreenX = screenX * cos - screenY * sin;
    const rotatedScreenY = screenX * sin + screenY * cos;

    const x = Math.round(rotatedScreenX / mapTileSize.value);
    const y = Math.round(rotatedScreenY / mapTileSize.value);

    centerPosition.value = { x, y };
  }

  function findMaxZoomInMapTileSize(): number {
    for (const allowedMapTileSize of allowedZoomMapTileSizes) {
      if (
        allowedMapTileSize > windowWidth.value / 4 ||
        allowedMapTileSize === 300
      ) {
        return allowedMapTileSize;
      }
    }

    return 300;
  }

  function getOffset(
    windowWidth: number,
    windowHeight: number,
    x: number,
    y: number,
  ): {
    offsetX: number;
    offsetY: number;
  } {
    // Convert grid coordinates (x, y) back to screen coordinates
    const rotatedScreenX = x * mapTileSize.value;
    const rotatedScreenY = y * mapTileSize.value;

    // Reverse the rotation matrix
    const screenX = rotatedScreenX * cos + rotatedScreenY * sin;
    const screenY = -rotatedScreenX * sin + rotatedScreenY * cos;

    // Calculate offsetX and offsetY
    const offsetX = (windowWidth - screenX * 2) / 2;
    const offsetY = (windowHeight - 64 - screenY * 2) / 2;

    return {
      offsetX: Math.round(offsetX),
      offsetY: Math.round(offsetY),
    };
  }

  function goToPosition({ x, y }: PointDto): void {
    const offset = getOffset(windowWidth.value, windowHeight.value, x, y);
    offsetX.value = offset.offsetX;
    offsetY.value = offset.offsetY;
    updateCenterPosition();
  }

  async function loadMap(): Promise<void> {
    await loadMapQueue.add(async () => {
      try {
        const response = await MapGateway.instance.getMapTiles(
          currentMapRange.value,
        );
        const mapTileIds = new Set(mapTiles.value.map((tile) => tile.id));
        const newMapTileIds = new Set(response.map((tile) => tile.id));

        const tileSize = mapTileSize.value;
        const tileSizeHalf = tileSize / 2;

        // Add all map tiles that are in response and not in the current map
        // Filter the ones that are in the current map and not in the response
        mapTiles.value = [
          ...response.filter((newTile) => !mapTileIds.has(newTile.id)),
          ...mapTiles.value.filter((tile) => newMapTileIds.has(tile.id)),
        ].map((mapTile) => {
          const x = mapTile.x * tileSize - tileSizeHalf;
          const y = mapTile.y * tileSize - tileSizeHalf;

          mapTile.style = {
            width: tileSize + "px",
            height: tileSize + "px",
            left: x + "px",
            top: y + "px",
            zIndex: 99 - mapTile.x + mapTile.y,
          };

          return mapTile;
        });
      } catch (e) {
        handleFatalError(e);
      }
    });
  }

  function isOnCurrentMap({ x, y }: PointDto): boolean {
    return (
      x >= currentMapRange.value.x1 &&
      x <= currentMapRange.value.x2 &&
      y >= currentMapRange.value.y1 &&
      y <= currentMapRange.value.y2
    );
  }

  function zoomIn(): void {
    const currentZoomIndex = allowedZoomMapTileSizes.indexOf(mapTileSize.value);

    if (currentZoomIndex === allowedZoomMapTileSizes.length - 1) {
      return;
    }

    const currentCenter = centerPosition.value;
    mapTileSize.value = allowedZoomMapTileSizes[currentZoomIndex + 1];
    goToPosition(currentCenter);
  }

  function zoomOut(): void {
    const currentZoomIndex = allowedZoomMapTileSizes.indexOf(mapTileSize.value);

    if (currentZoomIndex === 0) {
      return;
    }

    const currentCenter = centerPosition.value;
    mapTileSize.value = allowedZoomMapTileSizes[currentZoomIndex - 1];
    goToPosition(currentCenter);
  }

  function adjustMapTileSizeToScreen(): void {
    const minTilesX = 4;
    const maxTilesX = 20;

    if (amountOfTilesX.value < minTilesX) {
      zoomOut();
    } else if (amountOfTilesX.value >= maxTilesX) {
      zoomIn();
    }
  }

  return {
    updateCenterPosition,
    findMaxZoomInMapTileSize,
    adjustMapTileSizeToScreen,
    zoomIn,
    zoomOut,
    mapControlsDisabled,
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
    isOnCurrentMap,
  };
});
