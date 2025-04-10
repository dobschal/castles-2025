import { computed, Ref } from "vue";
import { Optional } from "@/types/core/Optional.ts";
import { RenderLayers } from "@/types/core/RenderLayers.ts";
import { BuildingDto } from "@/types/dto/BuildingDto.ts";
import { ensure } from "@/core/util.ts";
import { useMapStore } from "@/store/mapStore.ts";

interface AvatarRenderer {
  register: (building: BuildingDto) => void;
}

export const useAvatarRenderer = function (
  context: Ref<Optional<CanvasRenderingContext2D>>,
  layers: Ref<RenderLayers>,
): AvatarRenderer {
  const mapStore = useMapStore();
  const avatarImages = new Map<number, HTMLImageElement>();
  const avatarSize = computed(() => mapStore.mapTileSize * 0.25);
  const fontSize = computed(() => avatarSize.value / 2);

  return {
    register,
  };

  function register(building: BuildingDto): void {
    layers.value[1].push(() => _render(building));
  }

  function _render(building: BuildingDto): void {
    if (!building.user.avatarId) {
      building.user.avatarId = 1;
    }

    const ctx = ensure(context.value);

    // To show the image correctly, we need to rotate the canvas back and forth
    // Move the canvas to the center of the building, rotate it, and draw the image
    ctx.save();
    ctx.translate(
      building.x * mapStore.mapTileSize + mapStore.mapTileSize / 4,
      building.y * mapStore.mapTileSize - mapStore.mapTileSize,
    );
    ctx.rotate((45 * Math.PI) / 180);
    _drawTextBox(building);
    _drawImage(building);
    ctx.restore();
  }

  function _drawTextBox(building: BuildingDto): void {
    const ctx = ensure(context.value);
    ctx.font = `${fontSize.value}px MedievalSharp, sans-serif`;
    const textMeasure = ctx.measureText(building.user.username); // TextMetrics object
    ctx.shadowColor = "black";
    ctx.shadowBlur = 15;
    ctx.fillStyle = "rgba(0,0,0,0.3)";
    ctx.fillRect(
      avatarSize.value / 2,
      avatarSize.value * 0.1,
      textMeasure.width + avatarSize.value * 1.1,
      avatarSize.value * 0.8,
    );
    ctx.fillStyle = "white";
    ctx.fillText(
      building.user.username,
      avatarSize.value + avatarSize.value * 0.25,
      avatarSize.value - (avatarSize.value - fontSize.value) * 0.66,
      mapStore.mapTileSize,
    );
  }

  function _drawImage(building: BuildingDto): void {
    if (!building.user.avatarId) {
      return;
    }

    let avatarImage = avatarImages.get(building.user.avatarId);

    if (!avatarImage) {
      avatarImage = new Image();
      avatarImage.src =
        "/avatars/avatar-" + (building.user.avatarId ?? 0) + ".png";
      avatarImages.set(building.user.avatarId, avatarImage);
    }

    const ctx = ensure(context.value);
    ctx.beginPath();
    ctx.arc(
      avatarSize.value / 2,
      avatarSize.value / 2,
      avatarSize.value / 2,
      0,
      Math.PI * 2,
    );
    ctx.clip();
    ctx.drawImage(avatarImage, 0, 0, avatarSize.value, avatarSize.value);
  }
};
