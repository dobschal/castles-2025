import { BuildingEntity } from "@/types/model/BuildingEntity.ts";
import { Optional } from "@/types/core/Optional.ts";
import { useMapStore } from "@/store/mapStore.ts";
import { BuildingType } from "@/types/enum/BuildingType.ts";
import { BuildingDto } from "@/types/dto/BuildingDto.ts";
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

import castleLevel1Owned from "@/assets/tiles/castle-level-1-red-roof-min.png";
import castleLevel1Enemy from "@/assets/tiles/castle-level-1-beige-roof-min.png";
import castleLevel1Forbidden from "@/assets/tiles/castle-level-1-disabled-min.png";

import castleLevel2Owned from "@/assets/tiles/castle-level-2-red-roof.png";
import castleLevel2Enemy from "@/assets/tiles/castle-level-2-beige-roof.png";
import castleLevel2Forbidden from "@/assets/tiles/castle-level-2-disabled.png";
import castleLevel2OwnedForeground from "@/assets/tiles/castle-level-2-red-roof-foreground.png";
import castleLevel2EnemyForeground from "@/assets/tiles/castle-level-2-beige-roof-foreground.png";
import castleLevel2ForbiddenForeground from "@/assets/tiles/castle-level-2-disabled-foreground.png";
import { Ref } from "vue";
import { RenderLayers } from "@/types/core/RenderLayers.ts";
import { loadImage } from "@/core/util.ts";

// TODO: Animate Beerbubbles --> probably a UI element?

// TODO: Position on load is wrong...

interface BuildingRenderer {
  register(building: BuildingDto): void;
}

export const useBuildingRenderer = function (
  context: Ref<Optional<CanvasRenderingContext2D>>,
  layers: Ref<RenderLayers>,
  fps: Ref<number>,
  frame: Ref<number>,
): BuildingRenderer {
  const mapStore = useMapStore();

  const villageOwnedImage = loadImage(villageOwned);
  const villageEnemyImage = loadImage(villageEnemy);
  const villageForbiddenImage = loadImage(villageForbidden);
  const villageForegroundImage = loadImage(villageForeground);
  const villageForbiddenForegroundImage = loadImage(villageForbiddenForeground);

  const marketOwnedImage = loadImage(marketOwned);
  const marketEnemyImage = loadImage(marketEnemy);
  const marketForbiddenImage = loadImage(marketForbidden);

  const cityOwnedImage = loadImage(cityOwned);
  const cityEnemyImage = loadImage(cityEnemy);
  const cityForbiddenImage = loadImage(cityForbidden);
  const cityOwnedForegroundImage = loadImage(cityOwnedForeground);
  const cityEnemyForegroundImage = loadImage(cityEnemyForeground);
  const cityForbiddenForegroundImage = loadImage(cityForbiddenForeground);

  const farmOwnedImage = loadImage(farmOwned);
  const farmEnemyImage = loadImage(farmEnemy);
  const farmForbiddenImage = loadImage(farmForbidden);

  const breweryOwnedImage = loadImage(breweryOwned);
  const breweryEnemyImage = loadImage(breweryEnemy);
  const breweryForbiddenImage = loadImage(breweryForbidden);
  const breweryForegroundImage = loadImage(breweryForeground);
  const breweryForbiddenForegroundImage = loadImage(breweryForbiddenForeground);

  const castleLevel1OwnedImage = loadImage(castleLevel1Owned);
  const castleLevel1EnemyImage = loadImage(castleLevel1Enemy);
  const castleLevel1ForbiddenImage = loadImage(castleLevel1Forbidden);

  const castleLevel2OwnedImage = loadImage(castleLevel2Owned);
  const castleLevel2EnemyImage = loadImage(castleLevel2Enemy);
  const castleLevel2ForbiddenImage = loadImage(castleLevel2Forbidden);
  const castleLevel2OwnedForegroundImage = loadImage(
    castleLevel2OwnedForeground,
  );
  const castleLevel2EnemyForegroundImage = loadImage(
    castleLevel2EnemyForeground,
  );
  const castleLevel2ForbiddenForegroundImage = loadImage(
    castleLevel2ForbiddenForeground,
  );

  return {
    register,
  };

  // public methods

  function register(building: BuildingDto): void {
    layers.value[1].push(() => _render(1, building));
    layers.value[4].push(() => _render(3, building));
  }

  // endregion

  // region private methods

  function _render(layer: number, building: BuildingDto): void {
    const image = _getImageByType(
      building.type,
      layer,
      building.isOwnBuilding,
      building.mapTile?.state === MapTileState.FORBIDDEN,
      building.level,
    );
    _drawImage(building, image);
  }

  function _getImageByType(
    type: BuildingType,
    layer: number,
    isOwnBuilding: boolean,
    isForbidden: boolean,
    level: number,
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
        case BuildingType.CASTLE:
          if (level === 1) {
            if (isForbidden) return castleLevel1ForbiddenImage;

            if (isOwnBuilding) return castleLevel1OwnedImage;

            return castleLevel1EnemyImage;
          } else if (level === 2) {
            if (isForbidden) return castleLevel2ForbiddenImage;

            if (isOwnBuilding) return castleLevel2OwnedImage;

            return castleLevel2EnemyImage;
          }
      }
    } else if (layer === 3) {
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
        case BuildingType.CASTLE:
          if (level === 2) {
            if (isForbidden) return castleLevel2ForbiddenForegroundImage;

            if (isOwnBuilding) return castleLevel2OwnedForegroundImage;

            return castleLevel2EnemyForegroundImage;
          }
      }
    }
  }

  function _drawImage(
    building: BuildingEntity,
    image: Optional<HTMLImageElement>,
  ): void {
    if (!image) {
      return;
    }

    // The images have a padding to all side and need to overlap
    // a bit to look good
    const realSize = mapStore.mapTileSize * 1.4;

    context.value?.drawImage(
      image,
      building.x * mapStore.mapTileSize - realSize / 2,
      building.y * mapStore.mapTileSize - realSize / 2,
      realSize,
      realSize,
    );
  }

  // endregion
};
