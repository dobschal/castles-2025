import { computed, Ref } from "vue";
import { Optional } from "@/types/core/Optional.ts";
import { RenderLayers } from "@/types/core/RenderLayers.ts";
import { BuildingDto } from "@/types/dto/BuildingDto.ts";
import { ensure, loadImage } from "@/core/util.ts";
import { useMapStore } from "@/store/mapStore.ts";
import beer from "@/assets/beer-min.png";

interface BeerBubbleRenderer {
  register: (building: BuildingDto) => void;
}

export const useBeerBubbleRenderer = function (
  context: Ref<Optional<CanvasRenderingContext2D>>,
  layers: Ref<RenderLayers>,
  fps: Ref<number>,
  frame: Ref<number>,
): BeerBubbleRenderer {
  const beerImage = loadImage(beer);
  const mapStore = useMapStore();

  const animationSize = computed(() => Math.round(mapStore.mapTileSize * 0.1));
  const size = computed(() => Math.round(mapStore.mapTileSize * 0.33));

  return {
    register,
  };

  function register(building: BuildingDto): void {
    if (!building.collectableBeer) {
      return;
    }

    layers.value[4].push(() => _render(building));
  }

  function _render(building: BuildingDto): void {
    const ctx = ensure(context.value);
    const offset = Math.round(
      -animationSize.value * Math.sin((frame.value / fps.value) * Math.PI * 2),
    );
    ctx.save();
    ctx.translate(
      building.x * mapStore.mapTileSize + mapStore.mapTileSize / 3,
      building.y * mapStore.mapTileSize - mapStore.mapTileSize * 0.58,
    );
    ctx.rotate((45 * Math.PI) / 180);
    ctx.shadowColor = "black";
    ctx.shadowBlur = 15;
    ctx.drawImage(beerImage, 0, offset, size.value, size.value);
    ctx.restore();
  }
};
