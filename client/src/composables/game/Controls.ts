import { MAP_TILE_CLICKED } from "@/events.ts";
import { onBeforeUnmount, onMounted, Ref, ref } from "vue";
import { isTouchDevice } from "@/core/util.ts";
import { useMapStore } from "@/store/mapStore.ts";
import { Optional } from "@/types/core/Optional.ts";

// TODO: Apply click on touch too

export const useControls = function (
  canvas: Ref<Optional<HTMLCanvasElement>>,
): void {
  const mapStore = useMapStore();
  const isDragging = ref(false);
  const mouseDownTime = ref(0);
  const touchDownTime = ref(0);
  const lastTouch = ref({ x: 0, y: 0 });

  function onMapMouseDown(event: MouseEvent): void {
    if (mapStore.mapControlsDisabled || event.button !== 0) {
      return;
    }

    isDragging.value = true;
    mouseDownTime.value = Date.now();
  }

  function onMapMouseMove(event: MouseEvent): void {
    if (!isDragging.value) {
      return;
    }

    mapStore.offsetX += event.movementX;
    mapStore.offsetY += event.movementY;
  }

  function onMapMouseUp(event: MouseEvent): void {
    isDragging.value = false;
    mapStore.updateCenterPosition();
    const isClick = Date.now() - mouseDownTime.value < 200;

    if (isClick) {
      const mapTile = mapStore.findTileFromScreenPosition(
        event.clientX,
        event.clientY,
      );

      if (!mapTile) {
        return;
      }

      MAP_TILE_CLICKED.dispatch(mapTile);
    }
  }

  function onTouchStart(event: TouchEvent): void {
    if (mapStore.mapControlsDisabled) {
      return;
    }

    isDragging.value = true;
    touchDownTime.value = Date.now();
    lastTouch.value.x = event.touches[0].clientX;
    lastTouch.value.y = event.touches[0].clientY;
  }

  function onTouchMove(event: TouchEvent): void {
    if (!isDragging.value) {
      return;
    }

    mapStore.offsetX += event.touches[0].clientX - lastTouch.value.x;
    mapStore.offsetY += event.touches[0].clientY - lastTouch.value.y;

    lastTouch.value.x = event.touches[0].clientX;
    lastTouch.value.y = event.touches[0].clientY;
  }

  function onToucheEnd(): void {
    isDragging.value = false;
    mapStore.updateCenterPosition();
  }

  onMounted(() => {
    if (!isTouchDevice()) {
      canvas.value!.addEventListener("mousedown", onMapMouseDown);
      canvas.value!.addEventListener("mousemove", onMapMouseMove);
      canvas.value!.addEventListener("mouseup", onMapMouseUp);
    } else {
      canvas.value!.addEventListener("touchstart", onTouchStart);
      canvas.value!.addEventListener("touchmove", onTouchMove);
      canvas.value!.addEventListener("touchend", onToucheEnd);
    }
  });

  onBeforeUnmount(() => {
    canvas.value!.removeEventListener("mousedown", onMapMouseDown);
    canvas.value!.removeEventListener("mousemove", onMapMouseMove);
    canvas.value!.removeEventListener("mouseup", onMapMouseUp);
    canvas.value!.removeEventListener("touchstart", onTouchStart);
    canvas.value!.removeEventListener("touchmove", onTouchMove);
    canvas.value!.removeEventListener("touchend", onToucheEnd);
  });
};
