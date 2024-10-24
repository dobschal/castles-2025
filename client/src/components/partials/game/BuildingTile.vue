<template>
  <div class="building-tile" :class="{ 'is-own-building': isOwnBuilding }">
    <p class="banner" :style="bannerStyle">{{ building.user.username }}</p>
    <img
      src="@/assets/tiles/village.png"
      class="building village"
      alt="Building"
    />
    <img
      src="@/assets/tiles/village-top-layer.png"
      class="building-top-layer village"
      alt="Building"
    />
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

const props = defineProps<{
  building: BuildingEntity;
}>();
const buildingsStore = useBuildingsStore();
const authStore = useAuthStore();
const mapStore = useMapStore();

const isOwnBuilding = computed(() => {
  return props.building.user.id === authStore.user?.id;
});

const bannerStyle = computed(() => {
  return {
    fontSize: Math.floor(mapStore.mapTileSize / 7) + "px",
  };
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
  ACTION.dispatch(VillageAction);
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

  &.FORBIDDEN {
    img {
      filter: sepia(0.75) brightness(0.75);
    }
  }

  &.ACCEPTABLE {
    img {
      filter: brightness(1.2);
    }
  }

  &:not(.is-own-building) {
    .banner {
      color: lightgray;
    }
  }

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
      margin-left: -15%;
      margin-top: -25%;
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
      margin-left: -13%;
      margin-top: -25%;
    }
  }
}
</style>
