<template>
  <div class="building-tile" :class="{ 'is-own-building': isOwnBuilding }">
    <p class="banner">
      {{ building.user.username }}
    </p>
    <img src="@/assets/tiles/village.png" class="building" alt="Building" />
  </div>
</template>

<script lang="ts" setup>
import { computed, onBeforeUnmount, onMounted } from "vue";
import { ACTION, MAP_TILE_CLICKED } from "@/events.ts";
import { MapTileDto } from "@/types/dto/MapTileDto.ts";
import { BuildingEntity } from "@/types/model/BuildingEntity.ts";
import OpenVillageAction from "@/components/partials/game/actions/OpenVillageAction.vue";
import { useBuildingsStore } from "@/store/buildingsStore.ts";
import { useAuthStore } from "@/store/authStore.ts";

const props = defineProps<{
  building: BuildingEntity;
}>();
const buildingsStore = useBuildingsStore();
const authStore = useAuthStore();

const isOwnBuilding = computed(() => {
  return props.building.user.id === authStore.user?.id;
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
  ACTION.dispatch(OpenVillageAction);
}
</script>

<style lang="scss" scoped>
.building-tile {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: 1;
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

  &.is-own-building {
    .banner {
      background: rgb(119 47 0 / 68%);
    }
  }

  .banner {
    position: absolute;
    top: -28%;
    left: 48%;
    width: 100%;
    background-color: rgba(0, 0, 0, 0.5);
    color: white;
    text-align: center;
    padding: 5px;
    z-index: 2;
    font-size: 0.75rem;
    transform: rotate(45deg);
    box-shadow: 5px 5px 15px 0 rgba(0, 0, 0, 0.8);
  }

  img.building {
    width: 150%;
    margin-left: -25%;
    margin-top: -25%;
    pointer-events: none;
  }
}
</style>
