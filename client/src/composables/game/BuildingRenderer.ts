import { BuildingEntity } from "@/types/model/BuildingEntity.ts";
import { Optional } from "@/types/core/Optional.ts";
import { useMapStore } from "@/store/mapStore.ts";
import { BuildingType } from "@/types/enum/BuildingType.ts";
import { BuildingDto } from "@/types/dto/BuildingDto.ts";
import { MapTileDto } from "@/types/dto/MapTileDto.ts";
import { MapTileState } from "@/types/enum/MapTileState.ts";

import villageOwned from "@/assets/tiles/village-red-roof-min.png";
import villageEnemy from "@/assets/tiles/village-beige-roof-min.png";
import villageForbidden from "@/assets/tiles/village-disabled-min.png";
import villageForeground from "@/assets/tiles/village-top-layer-min.png";
import villageForbiddenForeground from "@/assets/tiles/village-top-layer-disabled-min.png";

import marketOwned from "@/assets/tiles/market-red.png";
import marketEnemy from "@/assets/tiles/market-beige.png";
import marketForbidden from "@/assets/tiles/market-disabled.png";

import cityOwned from "@/assets/tiles/city-red.png";
import cityEnemy from "@/assets/tiles/city-beige.png";
import cityForbidden from "@/assets/tiles/city-disabled.png";
import cityOwnedForeground from "@/assets/tiles/city-red-forground.png";
import cityEnemyForeground from "@/assets/tiles/city-beige-forground.png";
import cityForbiddenForeground from "@/assets/tiles/city-disabled-forground.png";

import farmOwned from "@/assets/tiles/farm-red-roof-min.png";
import farmEnemy from "@/assets/tiles/farm-beige-roof-min.png";
import farmForbidden from "@/assets/tiles/farm-disabled-min.png";

import breweryOwned from "@/assets/tiles/brewery-red-roof-min.png";
import breweryEnemy from "@/assets/tiles/brewery-beige-roof-min.png";
import breweryForbidden from "@/assets/tiles/brewery-disabled-min.png";
import breweryForeground from "@/assets/tiles/brewery-top-layer-min.png";
import breweryForbiddenForeground from "@/assets/tiles/brewery-top-layer-disabled-min.png";

// TODO: Animate Beerbubbles

// TODO: Add castles

interface BuildingRenderer {
  render(
    layer: number,
    canvasContext: CanvasRenderingContext2D,
    building: BuildingDto,
    mapTile: MapTileDto,
  ): void;
}

export const useBuildingRenderer = function (): BuildingRenderer {
  const mapStore = useMapStore();

  const villageOwnedImage = _loadImage(villageOwned);
  const villageEnemyImage = _loadImage(villageEnemy);
  const villageForbiddenImage = _loadImage(villageForbidden);
  const villageForegroundImage = _loadImage(villageForeground);
  const villageForbiddenForegroundImage = _loadImage(
    villageForbiddenForeground,
  );

  const marketOwnedImage = _loadImage(marketOwned);
  const marketEnemyImage = _loadImage(marketEnemy);
  const marketForbiddenImage = _loadImage(marketForbidden);

  const cityOwnedImage = _loadImage(cityOwned);
  const cityEnemyImage = _loadImage(cityEnemy);
  const cityForbiddenImage = _loadImage(cityForbidden);
  const cityOwnedForegroundImage = _loadImage(cityOwnedForeground);
  const cityEnemyForegroundImage = _loadImage(cityEnemyForeground);
  const cityForbiddenForegroundImage = _loadImage(cityForbiddenForeground);

  const farmOwnedImage = _loadImage(farmOwned);
  const farmEnemyImage = _loadImage(farmEnemy);
  const farmForbiddenImage = _loadImage(farmForbidden);

  const breweryOwnedImage = _loadImage(breweryOwned);
  const breweryEnemyImage = _loadImage(breweryEnemy);
  const breweryForbiddenImage = _loadImage(breweryForbidden);
  const breweryForegroundImage = _loadImage(breweryForeground);
  const breweryForbiddenForegroundImage = _loadImage(
    breweryForbiddenForeground,
  );

  return {
    render,
  };

  // public methods

  function render(
    layer: number,
    canvasContext: CanvasRenderingContext2D,
    building: BuildingDto,
    mapTile: MapTileDto,
  ): void {
    const image = _getImageByType(
      building.type,
      layer,
      building.isOwnBuilding,
      mapTile.state === MapTileState.FORBIDDEN,
    );
    _drawImage(canvasContext, building, image);
  }

  // endregion

  // region private methods

  function _getImageByType(
    type: BuildingType,
    layer: number,
    isOwnBuilding: boolean,
    isForbidden: boolean,
  ): Optional<HTMLImageElement> {
    if (layer === 1) {
      switch (type) {
        case BuildingType.VILLAGE:
          if (isForbidden) return villageForbiddenImage;

          if (isOwnBuilding) return villageOwnedImage;

          return villageEnemyImage;
        case BuildingType.MARKET:
          if (isForbidden) return marketForbiddenImage;

          if (isOwnBuilding) return marketOwnedImage;

          return marketEnemyImage;
        case BuildingType.CITY:
          if (isForbidden) return cityForbiddenImage;

          if (isOwnBuilding) return cityOwnedImage;

          return cityEnemyImage;
        case BuildingType.FARM:
          if (isForbidden) return farmForbiddenImage;

          if (isOwnBuilding) return farmOwnedImage;

          return farmEnemyImage;
        case BuildingType.BREWERY:
          if (isForbidden) return breweryForbiddenImage;

          if (isOwnBuilding) return breweryOwnedImage;

          return breweryEnemyImage;
      }
    } else {
      switch (type) {
        case BuildingType.VILLAGE:
          if (isForbidden) return villageForbiddenForegroundImage;

          return villageForegroundImage;
        case BuildingType.MARKET:
          return undefined;
        case BuildingType.CITY:
          if (isForbidden) return cityForbiddenForegroundImage;

          if (isOwnBuilding) return cityOwnedForegroundImage;

          return cityEnemyForegroundImage;
        case BuildingType.FARM:
          return undefined;
        case BuildingType.BREWERY:
          if (isForbidden) return breweryForbiddenForegroundImage;

          return breweryForegroundImage;
      }
    }
  }

  function _drawImage(
    canvasContext: CanvasRenderingContext2D,
    building: BuildingEntity,
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
      building.x * mapStore.mapTileSize - realSize / 2,
      building.y * mapStore.mapTileSize - realSize / 2,
      realSize,
      realSize,
    );
  }

  function _loadImage(source: string): HTMLImageElement {
    const image = new Image();
    image.src = source;

    return image;
  }

  // endregion
};
