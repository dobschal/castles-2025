<template>
  <div class="building-tile" :class="{ 'is-own-building': isOwnBuilding }">
    <p
      v-if="[BuildingType.VILLAGE, BuildingType.CITY].includes(building.type)"
      class="banner"
      :style="bannerStyle"
    >
      <img
        :src="'/avatars/avatar-' + (building.user.avatarId ?? 0) + '.png'"
        alt="Avatar"
        :style="bannerImageStyle"
      />
      {{ building.user.username }}
    </p>
    <template v-if="building.type === BuildingType.MARKET">
      <img
        v-if="!isDisabled && isOwnBuilding"
        src="../../../../assets/tiles/market-red.png"
        class="building market"
        alt="Building"
      />
      <img
        v-else-if="!isDisabled && !isOwnBuilding"
        src="../../../../assets/tiles/market-beige.png"
        class="building market"
        alt="Building"
      />
      <img
        v-else
        src="../../../../assets/tiles/market-disabled.png"
        class="building market"
        alt="Building"
      />
    </template>
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
    <template v-if="building.type === BuildingType.CITY">
      <img
        v-if="!isDisabled && isOwnBuilding"
        src="../../../../assets/tiles/city-red.png"
        class="building city"
        alt="Building"
      />
      <img
        v-else-if="!isDisabled && !isOwnBuilding"
        src="../../../../assets/tiles/city-beige.png"
        class="building city"
        alt="Building"
      />
      <img
        v-else
        src="../../../../assets/tiles/city-disabled.png"
        class="building city"
        alt="Building"
      />
      <img
        v-if="!isDisabled && isOwnBuilding"
        src="../../../../assets/tiles/city-red-forground.png"
        class="building-top-layer city"
        alt="Building"
      />
      <img
        v-else-if="!isDisabled && !isOwnBuilding"
        src="../../../../assets/tiles/city-beige-forground.png"
        class="building-top-layer city"
        alt="Building"
      />
      <img
        v-else
        src="../../../../assets/tiles/city-disabled-forground.png"
        class="building-top-layer city"
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
      <!--      <img-->
      <!--        v-if="!isDisabled"-->
      <!--        src="../../../../assets/tiles/farm-top-layer-min.png"-->
      <!--        class="building-top-layer farm"-->
      <!--        alt="Building"-->
      <!--      />-->
      <!--      <img-->
      <!--        v-else-->
      <!--        src="../../../../assets/tiles/farm-top-layer-disabled-min.png"-->
      <!--        class="building-top-layer farm"-->
      <!--        alt="Building"-->
      <!--      />-->
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
      <template v-if="building.level === 1">
        <img
          v-if="!isDisabled && isOwnBuilding"
          src="../../../../assets/tiles/castle-level-1-red-roof-min.png"
          class="building castle-level-1"
          alt="Building"
        />
        <img
          v-else-if="!isDisabled && !isOwnBuilding"
          src="../../../../assets/tiles/castle-level-1-beige-roof-min.png"
          class="building castle-level-1"
          alt="Building"
        />
        <img
          v-else-if="isDisabled"
          src="../../../../assets/tiles/castle-level-1-disabled-min.png"
          class="building castle-level-1"
          alt="Building"
        />
      </template>
      <template v-if="building.level === 2">
        <img
          v-if="!isDisabled && isOwnBuilding"
          src="../../../../assets/tiles/castle-level-2-red-roof.png"
          class="building castle-level-2"
          alt="Building"
        />
        <img
          v-else-if="!isDisabled && !isOwnBuilding"
          src="../../../../assets/tiles/castle-level-2-beige-roof.png"
          class="building castle-level-2"
          alt="Building"
        />
        <img
          v-else-if="isDisabled"
          src="../../../../assets/tiles/castle-level-2-disabled.png"
          class="building castle-level-2"
          alt="Building"
        />
        <img
          v-if="!isDisabled && isOwnBuilding"
          src="../../../../assets/tiles/castle-level-2-red-roof-foreground.png"
          class="building-top-layer castle-level-2"
          alt="Building"
        />
        <img
          v-else-if="!isDisabled && !isOwnBuilding"
          src="../../../../assets/tiles/castle-level-2-beige-roof-foreground.png"
          class="building-top-layer castle-level-2"
          alt="Building"
        />
        <img
          v-else-if="isDisabled"
          src="../../../../assets/tiles/castle-level-2-disabled-foreground.png"
          class="building-top-layer castle-level-2"
          alt="Building"
        />
      </template>
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
import CityAction from "@/components/partials/game/actions/CityAction.vue";
import MarketAction from "@/components/partials/game/actions/MarketAction.vue";

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

const bannerImageStyle = computed(() => {
  return {
    width: Math.ceil(mapStore.mapTileSize / 3) + "px",
    height: Math.ceil(mapStore.mapTileSize / 3) + "px",
    left: Math.ceil(-mapStore.mapTileSize / 4) + "px",
    borderWidth: Math.floor(mapStore.mapTileSize / 40) + "px",
  };
});

const bannerStyle = computed(() => {
  const padding = Math.floor(mapStore.mapTileSize / 20);

  return {
    padding: `${padding}px ${padding}px ${padding}px ${padding * 2.5}px`,
    fontSize: Math.floor(mapStore.mapTileSize / 10) + "px",
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
    left: 100%;
    width: fit-content;
    background-color: rgba(0, 0, 0, 0.5);
    color: white;
    text-align: center;
    padding: 5px;
    z-index: 2;
    font-size: 0.75rem;
    line-height: 1;
    transform: rotate(45deg) translate(-35%, -35%);
    transform-origin: 0 0;
    box-shadow: 5px 5px 15px 0 rgba(0, 0, 0, 0.8);
    pointer-events: none;

    img {
      position: absolute;
      top: 50%;
      left: 0;
      width: 1.5rem;
      height: 1.5rem;
      border-radius: 50%;
      border: 2px solid rgba(0, 0, 0, 0.5);
      transform: translateY(-50%);
    }
  }

  img.building {
    position: absolute;
    top: 0;
    left: 0;
    pointer-events: none;
    z-index: 1;

    &.market {
      width: 130%;
      margin-left: -20%;
      margin-top: -25%;
    }

    &.city {
      width: 150%;
      margin-left: -30%;
      margin-top: -29%;
    }

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
      width: 160%;
      margin-left: -23%;
      margin-top: -37%;
    }
  }

  img.building-top-layer {
    position: absolute;
    top: 0;
    left: 0;
    pointer-events: none;
    z-index: 4;

    &.city {
      width: 150%;
      margin-left: -30%;
      margin-top: -29%;
    }

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

    &.castle-level-2 {
      width: 160%;
      margin-left: -23%;
      margin-top: -37%;
    }
  }
}
</style>
