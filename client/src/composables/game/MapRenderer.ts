import { onBeforeUnmount, onMounted, Ref } from "vue";
import { useMapStore } from "@/store/mapStore.ts";
import { useMapTileRenderer } from "@/composables/game/MapTileRenderer.ts";
import { useBuildingsStore } from "@/store/buildingsStore.ts";
import { useBuildingRenderer } from "@/composables/game/BuildingRenderer.ts";
import { MapTileType } from "@/types/enum/MapTileType.ts";
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

export const useMapRenderer = function (
  canvas: Ref<Optional<HTMLCanvasElement>>,
): void {
  // TODO: Make MapLayer a composable too...

  // region variables

  const mapStore = useMapStore();
  const mapTileRenderer = useMapTileRenderer();
  const buildingsStore = useBuildingsStore();
  const buildingRenderer = useBuildingRenderer();
  // const authStore = useAuthStore();
  // const eventsStore = useEventsStore();
  // const actionStore = useActionStore();
  let canvasContext: CanvasRenderingContext2D;
  useControls(canvas);

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
    const context = canvas.value?.getContext("2d");

    if (!context) {
      return console.error("FATAL: Canvas context not found");
    }

    canvasContext = context;
    window.addEventListener("resize", onWindowResize);
    onWindowResize();

    MAP_TILE_CLICKED.on(onMapTileClicked);
  }

  function render(): void {
    canvasContext.clearRect(0, 0, mapStore.windowWidth, mapStore.windowHeight);

    // For the isometric view, we need to rotate the canvas
    canvasContext.translate(mapStore.offsetX, mapStore.offsetY);
    canvasContext.rotate((-45 * Math.PI) / 180);

    const renderLayers: Array<Array<(...p: Array<unknown>) => void>> = [
      [],
      [],
      [],
      [],
      [],
    ];

    mapStore.mapTiles.forEach((mapTile) => {
      // Render the map tile
      renderLayers[0].push(() =>
        mapTileRenderer.render(0, canvasContext, mapTile),
      );

      if (mapTile.type === MapTileType.FOREST) {
        renderLayers[4].push(() =>
          mapTileRenderer.render(4, canvasContext, mapTile),
        );
      }

      // Render the building
      const building = buildingsStore.buildings.find(
        (b) => b.x === mapTile.x && b.y === mapTile.y,
      );

      if (building) {
        renderLayers[1].push(() =>
          buildingRenderer.render(1, canvasContext, building, mapTile),
        );
        renderLayers[3].push(() =>
          buildingRenderer.render(3, canvasContext, building, mapTile),
        );
      }
    });

    // Render the layers in the correct order
    renderLayers.forEach((layer) => {
      layer.forEach((renderFunction) => {
        renderFunction();
      });
    });

    // Reset the canvas transformation matrix (to avoid cumulative transformations)
    canvasContext.setTransform(1, 0, 0, 1, 0, 0);

    window.requestAnimationFrame(render);
  }

  function onMapTileClicked(mapTile: MapTileDto): void {
    const building = buildingsStore.buildings.find(
      (b) => b.x === mapTile.x && b.y === mapTile.y,
    );

    if (!building) {
      return;
    }

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
