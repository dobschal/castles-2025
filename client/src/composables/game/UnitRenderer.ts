import { Ref } from "vue";
import { Optional } from "@/types/core/Optional.ts";
import { RenderLayers } from "@/types/core/RenderLayers.ts";
import { UnitEntity } from "@/types/model/UnitEntity.ts";
import { loadImage } from "@/core/util.ts";
import { UnitType } from "@/types/enum/UnitType.ts";
import { UnitDto } from "@/types/dto/UnitDto.ts";
import { MapTileState } from "@/types/enum/MapTileState.ts";
import { useMapStore } from "@/store/mapStore.ts";

import workerOwned from "@/assets/tiles/worker-red-hat-min.png";
import workerEnemy from "@/assets/tiles/worker-beige-hat-min.png";
import workerForbidden from "@/assets/tiles/worker-disabled-min.png";
import swordsmanOwned from "@/assets/tiles/swordsman-red-min.png";
import swordsmanEnemy from "@/assets/tiles/swordsman-beige-min.png";
import swordsmanForbidden from "@/assets/tiles/swordsman-disabled-min.png";
import horsemanOwned from "@/assets/tiles/hosreman-red-min.png";
import horsemanEnemy from "@/assets/tiles/hosreman-beige-min.png";
import horsemanForbidden from "@/assets/tiles/hosreman-disabled-min.png";
import spearmanOwned from "@/assets/tiles/spearman-red-min.png";
import spearmanEnemy from "@/assets/tiles/spearman-beige-min.png";
import spearmanForbidden from "@/assets/tiles/spearman-disabled-min.png";
import dragonOwned from "@/assets/tiles/dragon-red.png";
import dragonEnemy from "@/assets/tiles/dragon-beige.png";
import dragonForbidden from "@/assets/tiles/dragon-disabled.png";
import archerOwned from "@/assets/tiles/archer-red.png";
import archerEnemy from "@/assets/tiles/archer-beige.png";
import archerForbidden from "@/assets/tiles/archer-disabled.png";

// TODO: Render countdown for "next move in"

interface UnitRenderer {
  register(unit: UnitEntity): void;
}

export const useUnitRenderer = function (
  context: Ref<Optional<CanvasRenderingContext2D>>,
  layers: Ref<RenderLayers>,
): UnitRenderer {
  const mapStore = useMapStore();

  const workerOwnedImage = loadImage(workerOwned);
  const workerEnemyImage = loadImage(workerEnemy);
  const workerForbiddenImage = loadImage(workerForbidden);
  const swordsmanOwnedImage = loadImage(swordsmanOwned);
  const swordsmanEnemyImage = loadImage(swordsmanEnemy);
  const swordsmanForbiddenImage = loadImage(swordsmanForbidden);
  const horsemanOwnedImage = loadImage(horsemanOwned);
  const horsemanEnemyImage = loadImage(horsemanEnemy);
  const horsemanForbiddenImage = loadImage(horsemanForbidden);
  const spearmanOwnedImage = loadImage(spearmanOwned);
  const spearmanEnemyImage = loadImage(spearmanEnemy);
  const spearmanForbiddenImage = loadImage(spearmanForbidden);
  const dragonOwnedImage = loadImage(dragonOwned);
  const dragonEnemyImage = loadImage(dragonEnemy);
  const dragonForbiddenImage = loadImage(dragonForbidden);
  const archerOwnedImage = loadImage(archerOwned);
  const archerEnemyImage = loadImage(archerEnemy);
  const archerForbiddenImage = loadImage(archerForbidden);

  return {
    register,
  };

  function register(unit: UnitDto): void {
    const image = _getImageByType(unit);
    layers.value[2].push(() => _drawImage(unit, image));
  }

  function _getImageByType(unit: UnitDto): Optional<HTMLImageElement> {
    const isForbidden = unit.mapTile?.state === MapTileState.FORBIDDEN;
    switch (unit.type) {
      case UnitType.WORKER:
        if (isForbidden) return workerForbiddenImage;

        if (unit.isOwnUnit) return workerOwnedImage;

        return workerEnemyImage;
      case UnitType.SWORDSMAN:
        if (isForbidden) return swordsmanForbiddenImage;

        if (unit.isOwnUnit) return swordsmanOwnedImage;

        return swordsmanEnemyImage;
      case UnitType.HORSEMAN:
        if (isForbidden) return horsemanForbiddenImage;

        if (unit.isOwnUnit) return horsemanOwnedImage;

        return horsemanEnemyImage;
      case UnitType.SPEARMAN:
        if (isForbidden) return spearmanForbiddenImage;

        if (unit.isOwnUnit) return spearmanOwnedImage;

        return spearmanEnemyImage;
      case UnitType.DRAGON:
        if (isForbidden) return dragonForbiddenImage;

        if (unit.isOwnUnit) return dragonOwnedImage;

        return dragonEnemyImage;
      case UnitType.ARCHER:
        if (isForbidden) return archerForbiddenImage;

        if (unit.isOwnUnit) return archerOwnedImage;

        return archerEnemyImage;
    }
  }

  function _drawImage(unit: UnitDto, image: Optional<HTMLImageElement>): void {
    if (!image) {
      return;
    }

    // The images have a padding to all side and need to overlap
    // a bit to look good
    const realSize = mapStore.mapTileSize * 1.2;

    context.value?.drawImage(
      image,
      unit.x * mapStore.mapTileSize - realSize / 2,
      unit.y * mapStore.mapTileSize - realSize / 2,
      realSize,
      realSize,
    );
  }
};
