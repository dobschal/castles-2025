import { onBeforeUnmount, onMounted, ref, Ref } from "vue";
import { useMapStore } from "@/store/mapStore.ts";
import { useMapTileRenderer } from "@/composables/game/MapTileRenderer.ts";
import { useBuildingsStore } from "@/store/buildingsStore.ts";
import { useBuildingRenderer } from "@/composables/game/BuildingRenderer.ts";
import { ACTION, MAP_TILE_CLICKED, TOAST } from "@/events.ts";
import { MapTileDto } from "@/types/dto/MapTileDto.ts";
import { BuildingType } from "@/types/enum/BuildingType.ts";
import CityAction from "@/components/partials/game/actions/CityAction.vue";
import VillageAction from "@/components/partials/game/actions/VillageAction.vue";
import FarmAction from "@/components/partials/game/actions/FarmAction.vue";
import BreweryAction from "@/components/partials/game/actions/BreweryAction.vue";
import CastleAction from "@/components/partials/game/actions/CastleAction.vue";
import MarketAction from "@/components/partials/game/actions/MarketAction.vue";
import { useControls } from "@/composables/game/Controls.ts";
import { Optional } from "@/types/core/Optional.ts";
import { atSamePosition, ensure, handleFatalError } from "@/core/util.ts";
import { RenderLayers } from "@/types/core/RenderLayers.ts";
import { useAvatarRenderer } from "@/composables/game/AvatarRenderer.ts";
import { useUnitsStore } from "@/store/unitsStore.ts";
import { useUnitRenderer } from "@/composables/game/UnitRenderer.ts";
import UnitAction from "@/components/partials/game/actions/UnitAction.vue";
import { BuildingDto } from "@/types/dto/BuildingDto.ts";
import { useBeerBubbleRenderer } from "@/composables/game/BeerBubbleRenderer.ts";
import { BuildingGateway } from "@/gateways/BuildingGateway.ts";
import { useEventsStore } from "@/store/eventsStore.ts";

export const useMapRenderer = function (
  canvas: Ref<Optional<HTMLCanvasElement>>,
): void {
  // region variables

  const mapStore = useMapStore();
  const buildingsStore = useBuildingsStore();
  const unitsStore = useUnitsStore();
  const eventsStore = useEventsStore();

  const context = ref<CanvasRenderingContext2D>();
  const layers = ref<RenderLayers>([]);
  const fps = ref<number>(60);
  const lastTime = ref<number>(0);
  const renderTime = ref<number>(0);
  const frame = ref<number>(0);
  const isMounted = ref<boolean>(false);
  const ALLOWED_FPS_DIFF = 5;

  let logFpsInterval: ReturnType<typeof setInterval>;

  useControls(canvas);
  const beerBubbleRenderer = useBeerBubbleRenderer(context, layers, fps, frame);
  const buildingRenderer = useBuildingRenderer(context, layers, fps, frame);
  const mapTileRenderer = useMapTileRenderer(context, layers, fps);
  const avatarRenderer = useAvatarRenderer(context, layers, fps, frame);
  const unitRenderer = useUnitRenderer(context, layers, fps, frame);

  // endregion

  // region methods

  function onWindowResize(): void {
    canvas.value!.width = window.innerWidth;
    canvas.value!.height = window.innerHeight - 64;
    mapStore.windowWidth = window.innerWidth;
    mapStore.windowHeight = window.innerHeight;
    mapStore.adjustMapTileSizeToScreen();
    mapStore.updateCenterPosition();
  }

  function setup(): void {
    context.value = ensure(canvas.value?.getContext("2d"));
    window.addEventListener("resize", onWindowResize);
    onWindowResize();

    MAP_TILE_CLICKED.on(onMapTileClicked);
  }

  function render(t: number): void {
    const t1 = Date.now();
    const currentFps = Math.round(1000 / (t - lastTime.value));

    if (Math.abs(currentFps - fps.value) > ALLOWED_FPS_DIFF) {
      fps.value = currentFps;
    }

    lastTime.value = t;
    context.value?.clearRect(0, 0, mapStore.windowWidth, mapStore.windowHeight);

    // For the isometric view, we need to rotate the canvas
    context.value?.translate(mapStore.offsetX, mapStore.offsetY);
    context.value?.rotate((-45 * Math.PI) / 180);

    layers.value = [[], [], [], [], [], []];

    mapStore.mapTiles.forEach((mapTile) => {
      mapTileRenderer.register(mapTile);
    });

    buildingsStore.buildings.forEach((building) => {
      if (!building.mapTile) {
        building.mapTile = mapStore.mapTiles.find(atSamePosition(building));
      }

      buildingRenderer.register(building);

      if (
        [BuildingType.CITY, BuildingType.CASTLE, BuildingType.VILLAGE].includes(
          building.type,
        )
      ) {
        avatarRenderer.register(building);
      }

      if (building.type === BuildingType.BREWERY) {
        beerBubbleRenderer.register(building);
      }
    });

    unitsStore.units.forEach((unit) => {
      if (!unit.mapTile) {
        unit.mapTile = mapStore.mapTiles.find(atSamePosition(unit));
      }

      unitRenderer.register(unit);
    });

    // Render the layers in the correct order
    layers.value.forEach((layer) => {
      layer.forEach((renderFunction) => {
        renderFunction();
      });
    });

    // Reset the canvas transformation matrix (to avoid cumulative transformations)
    context.value?.setTransform(1, 0, 0, 1, 0, 0);

    renderTime.value = Date.now() - t1;
    frame.value++;

    if (frame.value >= fps.value) {
      frame.value = 0;
    }

    if (!isMounted.value) {
      console.info("[MapRenderer] Unmounted, stopping render loop");

      return;
    }

    window.requestAnimationFrame(render);
  }

  function onMapTileClicked(mapTile: MapTileDto): void {
    const building = buildingsStore.buildings.find(atSamePosition(mapTile));

    if (building) {
      onBuildingClicked(building);
    } else {
      const unit = unitsStore.units.find(atSamePosition(mapTile));

      if (unit) {
        unitsStore.activeUnit = unit;
        ACTION.dispatch(UnitAction);
      }
    }
  }

  function onBuildingClicked(building: BuildingDto): void {
    buildingsStore.activeBuilding = building;
    switch (building.type) {
      case BuildingType.CITY:
        ACTION.dispatch(CityAction);
        break;
      case BuildingType.VILLAGE:
        ACTION.dispatch(VillageAction);
        break;
      case BuildingType.FARM:
        ACTION.dispatch(FarmAction);
        break;
      case BuildingType.BREWERY:
        if (building.collectableBeer > 0) {
          void collectBeer(building);

          return;
        }

        ACTION.dispatch(BreweryAction);
        break;
      case BuildingType.CASTLE:
        ACTION.dispatch(CastleAction);
        break;
      case BuildingType.MARKET:
        ACTION.dispatch(MarketAction);
        break;
    }
  }

  async function collectBeer(building: BuildingDto): Promise<void> {
    try {
      const { message } = await BuildingGateway.instance.collectBeer(
        building.id,
        building.collectableBeer,
      );
      building.collectableBeer = 0; // TODO: Not reflected in store...
      eventsStore.ownActionHappened = true;
      TOAST.dispatch({ type: "success", messageKey: message });
    } catch (error) {
      handleFatalError(error);
    }
  }

  // endregion

  // region hooks

  onMounted(() => {
    isMounted.value = true;
    setup();
    render(0);
    logFpsInterval = setInterval(() => {
      console.info(
        `[MapRenderer] FPS: ${fps.value}, Render time: ${Math.round(renderTime.value)}ms`,
      );
    }, 3000);
  });

  onBeforeUnmount(() => {
    canvas.value!.removeEventListener("resize", onWindowResize);
    isMounted.value = false;
    window.clearInterval(logFpsInterval);
  });
};
