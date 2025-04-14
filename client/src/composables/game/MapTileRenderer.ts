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
import { RenderLayers } from "@/types/core/RenderLayers.ts";
import { Ref } from "vue";
import { ensure, loadImage } from "@/core/util.ts";

interface MapTileRenderer {
  register: (mapTile: MapTileDto) => void;
}

export const useMapTileRenderer = function (
  context: Ref<Optional<CanvasRenderingContext2D>>,
  layers: Ref<RenderLayers>,
  fps: Ref<number>,
): MapTileRenderer {
  // region member variables

  const mapStore = useMapStore();
  const forstTileImage = loadImage(forestTile);
  const forstTileImageForbidden = loadImage(forestTileForbidden);
  const mountainTileImage = loadImage(mountainTile);
  const mountainTileImageForbidden = loadImage(mountainTileForbidden);
  const waterTileImage = loadImage(waterTile);
  const waterTileImageForbidden = loadImage(waterTileForbidden);
  const plainTileImage = loadImage(plainTile);
  const plainTileImageForbidden = loadImage(plainTileForbidden);
  const forestTileTopLayerImage = loadImage(forestTileTopLayer);
  const forestTileTopLayerImageForbidden = loadImage(
    forstTileTopLayerForbidden,
  );

  // endregion

  // public methods

  return {
    register,
  };

  function register(mapTile: MapTileDto): void {
    mapTile.renderFrame++;
    layers.value[0].push(() => _render(0, mapTile));

    if (mapTile.type === MapTileType.FOREST) {
      layers.value[4].push(() => _render(4, mapTile));
    }
  }

  // endregion

  // region private methods

  function _render(layer: number, mapTile: MapTileDto): void {
    const image = _getImageByType(
      mapTile.type,
      layer,
      mapTile.state === MapTileState.FORBIDDEN,
    );
    _drawImage(mapTile, image);
  }

  function _drawImage(
    mapTile: MapTileDto,
    image: Optional<HTMLImageElement>,
  ): void {
    if (!image) {
      return;
    }

    // The images have a padding to all side and need to overlap
    // a bit to look good
    const animationFrames = fps.value / 3;
    const realSize = mapStore.mapTileSize * 1.4;
    let animationOffset = 0;

    const ctx = ensure(context.value);

    if (mapTile.renderFrame < animationFrames) {
      animationOffset =
        (realSize / 2) * (1 - mapTile.renderFrame / animationFrames);
      ctx.globalAlpha = mapTile.renderFrame / animationFrames;
    }

    ctx.drawImage(
      image,
      mapTile.x * mapStore.mapTileSize - realSize / 2 - animationOffset,
      mapTile.y * mapStore.mapTileSize - realSize / 2 + animationOffset,
      realSize,
      realSize,
    );
    ctx.globalAlpha = 1;
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

  // endregion
};
