import { MapTileDto } from "@/types/dto/MapTileDto.ts";
import { useMapStore } from "@/store/mapStore.ts";
import mountainTile from "@/assets/tiles/mountain-min.png";
import mountainTileForbidden from "@/assets/tiles/mountain-disabled-min.png";
import waterTile from "@/assets/tiles/water-min.png";
import waterTileForbidden from "@/assets/tiles/water-disabled-min.png";
import forestTile from "@/assets/tiles/forest-min.png";
import forestTileForbidden from "@/assets/tiles/forest-disabled-min.png";
import forestTileTopLayer from "@/assets/tiles/forest-top-layer-min.png";
import forstTileTopLayerForbidden from "@/assets/tiles/forest-top-layer-disabled-min.png";
import plainTile from "@/assets/tiles/plain-min.png";
import plainTileForbidden from "@/assets/tiles/plain-disabled-min.png";
import { MapTileType } from "@/types/enum/MapTileType.ts";
import { Optional } from "@/types/core/Optional.ts";
import { MapTileState } from "@/types/enum/MapTileState.ts";

interface MapTileRenderer {
  render: (
    layer: number,
    canvasContext: CanvasRenderingContext2D,
    mapTile: MapTileDto,
  ) => void;
}

export const useMapTileRenderer = function (): MapTileRenderer {
  // region member variables

  const mapStore = useMapStore();
  const forstTileImage = _loadImage(forestTile);
  const forstTileImageForbidden = _loadImage(forestTileForbidden);
  const mountainTileImage = _loadImage(mountainTile);
  const mountainTileImageForbidden = _loadImage(mountainTileForbidden);
  const waterTileImage = _loadImage(waterTile);
  const waterTileImageForbidden = _loadImage(waterTileForbidden);
  const plainTileImage = _loadImage(plainTile);
  const plainTileImageForbidden = _loadImage(plainTileForbidden);
  const forestTileTopLayerImage = _loadImage(forestTileTopLayer);
  const forestTileTopLayerImageForbidden = _loadImage(
    forstTileTopLayerForbidden,
  );

  // endregion

  // public methods

  return {
    render,
  };

  function render(
    layer: number,
    canvasContext: CanvasRenderingContext2D,
    mapTile: MapTileDto,
  ): void {
    const image = _getImageByType(
      mapTile.type,
      layer,
      mapTile.state === MapTileState.FORBIDDEN,
    );
    _drawImage(canvasContext, mapTile, image);
  }

  // endregion

  // region private methods

  function _drawImage(
    canvasContext: CanvasRenderingContext2D,
    mapTile: MapTileDto,
    image: Optional<HTMLImageElement>,
  ): void {
    if (!image) {
      return;
    }

    // The images have a padding to all side and need to overlap
    // a bit to look good
    const realSize = mapStore.mapTileSize * 1.4;

    canvasContext.drawImage(
      image,
      mapTile.x * mapStore.mapTileSize - realSize / 2,
      mapTile.y * mapStore.mapTileSize - realSize / 2,
      realSize,
      realSize,
    );
  }

  function _getImageByType(
    type: MapTileType,
    layer: number,
    isForbidden: boolean,
  ): HTMLImageElement {
    if (layer === 0) {
      switch (type) {
        case MapTileType.MOUNTAIN:
          return isForbidden ? mountainTileImageForbidden : mountainTileImage;
        case MapTileType.WATER:
          return isForbidden ? waterTileImageForbidden : waterTileImage;
        case MapTileType.FOREST:
          return isForbidden ? forstTileImageForbidden : forstTileImage;
        case MapTileType.PLAIN:
          return isForbidden ? plainTileImageForbidden : plainTileImage;
      }
    } else {
      return isForbidden
        ? forestTileTopLayerImageForbidden
        : forestTileTopLayerImage;
    }
  }

  function _loadImage(source: string): HTMLImageElement {
    const image = new Image();
    image.src = source;

    return image;
  }

  // endregion
};
