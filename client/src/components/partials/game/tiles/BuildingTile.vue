<template>
  <div class="building-tile" :class="{ 'is-own-building': isOwnBuilding }">
    <p
      v-if="building.type === BuildingType.VILLAGE"
      class="banner"
      :style="bannerStyle"
    >
      {{ building.user.username }}
    </p>
    <template v-if="building.type === BuildingType.VILLAGE">
      <img
        v-if="!isDisabled && isOwnBuilding"
        src="../../../../assets/tiles/village-red-roof-min.png"
        class="building village"
        alt="Building"
      />
      <img
        v-else-if="!isDisabled && !isOwnBuilding"
        src="../../../../assets/tiles/village-beige-roof-min.png"
        class="building village"
        alt="Building"
      />
      <img
        v-else
        src="../../../../assets/tiles/village-disabled-min.png"
        class="building village"
        alt="Building"
      />
      <img
        v-if="!isDisabled"
        src="../../../../assets/tiles/village-top-layer-min.png"
        class="building-top-layer village"
        alt="Building"
      />
      <img
        v-else
        src="../../../../assets/tiles/village-top-layer-disabled-min.png"
        class="building-top-layer village"
        alt="Building"
      />
    </template>
    <template v-if="building.type === BuildingType.FARM">
      <img
        v-if="!isDisabled && isOwnBuilding"
        src="../../../../assets/tiles/farm-red-roof-min.png"
        class="building farm"
        alt="Building"
      />
      <img
        v-else-if="!isDisabled && !isOwnBuilding"
        src="../../../../assets/tiles/farm-beige-roof-min.png"
        class="building farm"
        alt="Building"
      />
      <img
        v-else
        src="../../../../assets/tiles/farm-disabled-min.png"
        class="building farm"
        alt="Building"
      />
      <img
        v-if="!isDisabled"
        src="../../../../assets/tiles/farm-top-layer-min.png"
        class="building-top-layer farm"
        alt="Building"
      />
      <img
        v-else
        src="../../../../assets/tiles/farm-top-layer-disabled-min.png"
        class="building-top-layer farm"
        alt="Building"
      />
    </template>
    <template v-if="building.type === BuildingType.BREWERY">
      <img
        v-if="!isDisabled && isOwnBuilding"
        src="../../../../assets/tiles/brewery-red-roof-min.png"
        class="building brewery"
        alt="Building"
      />
      <img
        v-else-if="!isDisabled && !isOwnBuilding"
        src="../../../../assets/tiles/brewery-beige-roof-min.png"
        class="building brewery"
        alt="Building"
      />
      <img
        v-else
        src="../../../../assets/tiles/brewery-disabled-min.png"
        class="building brewery"
        alt="Building"
      />
      <img
        v-if="!isDisabled"
        src="../../../../assets/tiles/brewery-top-layer-min.png"
        class="building-top-layer brewery"
        alt="Building"
      />
      <img
        v-else
        src="../../../../assets/tiles/brewery-top-layer-disabled-min.png"
        class="building-top-layer brewery"
        alt="Building"
      />
      <BeerCollectBubble v-if="beerToCollect > 0" :building="building" />
    </template>
    <template v-if="building.type === BuildingType.CASTLE">
      <img
        v-if="building.level === 1 && !isDisabled && isOwnBuilding"
        src="../../../../assets/tiles/castle-level-1-red-roof-min.png"
        class="building castle-level-1"
        alt="Building"
      />
      <img
        v-else-if="building.level === 1 && !isDisabled && !isOwnBuilding"
        src="../../../../assets/tiles/castle-level-1-beige-roof-min.png"
        class="building castle-level-1"
        alt="Building"
      />
      <img
        v-else-if="building.level === 1 && isDisabled"
        src="../../../../assets/tiles/castle-level-1-disabled-min.png"
        class="building castle-level-1"
        alt="Building"
      />
    </template>
  </div>
</template>

<script lang="ts" setup>
import { computed, onBeforeUnmount, onMounted } from "vue";
import { ACTION, MAP_TILE_CLICKED } from "@/events.ts";
import { MapTileDto } from "@/types/dto/MapTileDto.ts";
import { BuildingEntity } from "@/types/model/BuildingEntity.ts";
import VillageAction from "@/components/partials/game/actions/VillageAction.vue";
import { useBuildingsStore } from "@/store/buildingsStore.ts";
import { useAuthStore } from "@/store/authStore.ts";
import { useMapStore } from "@/store/mapStore.ts";
import { BuildingType } from "@/types/enum/BuildingType.ts";
import FarmAction from "@/components/partials/game/actions/FarmAction.vue";
import BreweryAction from "@/components/partials/game/actions/BreweryAction.vue";
import { MapTileState } from "@/types/enum/MapTileState.ts";
import BeerCollectBubble from "@/components/partials/game/BeerCollectBubble.vue";
import CastleAction from "@/components/partials/game/actions/CastleAction.vue";

const props = defineProps<{
  building: BuildingEntity;
  mapTile: MapTileDto;
}>();

const buildingsStore = useBuildingsStore();
const authStore = useAuthStore();
const mapStore = useMapStore();

const isDisabled = computed(() => {
  return props.mapTile.state === MapTileState.FORBIDDEN;
});

const isOwnBuilding = computed(() => {
  return props.building.user.id === authStore.user?.id;
});

const bannerStyle = computed(() => {
  return {
    fontSize: Math.floor(mapStore.mapTileSize / 7) + "px",
  };
});

const beerToCollect = computed(() => {
  return buildingsStore.calculateBeerToCollect(props.building);
});

onMounted(() => {
  MAP_TILE_CLICKED.on(onMapTileClicked);
});

onBeforeUnmount(() => {
  MAP_TILE_CLICKED.off(onMapTileClicked);
});

function onMapTileClicked(mapTile: MapTileDto): void {
  if (props.building.x !== mapTile.x || props.building.y !== mapTile.y) {
    return;
  }

  buildingsStore.activeBuilding = props.building;
  switch (props.building.type) {
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
  }
}
</script>

<style lang="scss" scoped>
.building-tile {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  user-select: none;

  .banner {
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    background-color: rgba(0, 0, 0, 0.5);
    color: white;
    text-align: center;
    padding: 5px;
    z-index: 2;
    font-size: 0.75rem;
    transform: rotate(45deg) translateX(23%) translateY(-160%);
    box-shadow: 5px 5px 15px 0 rgba(0, 0, 0, 0.8);
  }

  img.building {
    position: absolute;
    top: 0;
    left: 0;
    pointer-events: none;
    z-index: 1;

    &.village {
      width: 150%;
      margin-left: -18%;
      margin-top: -23%;
    }

    &.farm {
      width: 150%;
      margin-left: -25%;
      margin-top: -19%;
    }

    &.brewery {
      width: 150%;
      margin-left: -25%;
      margin-top: -25%;
    }

    &.castle-level-1 {
      width: 160%;
      margin-left: -20%;
      margin-top: -40%;
    }

    &.castle-level-2 {
      width: 150%;
      margin-left: -5%;
      margin-top: -35%;
    }

    &.castle-level-3 {
      width: 130%;
      margin-left: 0;
      margin-top: -30%;
    }
  }

  img.building-top-layer {
    position: absolute;
    top: 0;
    left: 0;
    pointer-events: none;
    z-index: 4;

    &.village {
      width: 150%;
      margin-left: -15%;
      margin-top: -24%;
    }

    &.farm {
      width: 150%;
      margin-left: -22%;
      margin-top: -19%;
    }

    &.brewery {
      width: 150%;
      margin-left: -25%;
      margin-top: -26%;
    }
  }
}
</style>
