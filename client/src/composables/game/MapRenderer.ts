import { onBeforeUnmount, onMounted, ref, Ref } from "vue";
import { useMapStore } from "@/store/mapStore.ts";
import { useMapTileRenderer } from "@/composables/game/MapTileRenderer.ts";
import { useBuildingsStore } from "@/store/buildingsStore.ts";
import { useBuildingRenderer } from "@/composables/game/BuildingRenderer.ts";
import { ACTION, MAP_TILE_CLICKED } from "@/events.ts";
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
import { atSamePosition, ensure } from "@/core/util.ts";
import { RenderLayers } from "@/types/core/RenderLayers.ts";
import { useAvatarRenderer } from "@/composables/game/AvatarRenderer.ts";
import { useUnitsStore } from "@/store/unitsStore.ts";
import { useUnitRenderer } from "@/composables/game/UnitRenderer.ts";
import UnitAction from "@/components/partials/game/actions/UnitAction.vue";

export const useMapRenderer = function (
  canvas: Ref<Optional<HTMLCanvasElement>>,
): void {
  // region variables

  const mapStore = useMapStore();
  const buildingsStore = useBuildingsStore();
  const unitsStore = useUnitsStore();

  const context = ref<CanvasRenderingContext2D>();
  const layers = ref<RenderLayers>([]);

  useControls(canvas);
  const buildingRenderer = useBuildingRenderer(context, layers);
  const mapTileRenderer = useMapTileRenderer(context, layers);
  const avatarRenderer = useAvatarRenderer(context, layers);
  const unitRenderer = useUnitRenderer(context, layers);

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

  function render(): void {
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

    window.requestAnimationFrame(render);
  }

  function onMapTileClicked(mapTile: MapTileDto): void {
    const building = buildingsStore.buildings.find(atSamePosition(mapTile));

    if (building) {
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
          ACTION.dispatch(BreweryAction);
          break;
        case BuildingType.CASTLE:
          ACTION.dispatch(CastleAction);
          break;
        case BuildingType.MARKET:
          ACTION.dispatch(MarketAction);
          break;
      }
    } else {
      const unit = unitsStore.units.find(atSamePosition(mapTile));

      if (unit) {
        unitsStore.activeUnit = unit;
        ACTION.dispatch(UnitAction);
      }
    }
  }

  // endregion

  // region hooks

  onMounted(() => {
    setup();
    render();
  });

  onBeforeUnmount(() => {
    canvas.value!.removeEventListener("resize", onWindowResize);
  });
};
