<template>
  <div class="unit-tile">
    <template v-if="unit.type === UnitType.WORKER">
      <img
        v-if="isDisabled"
        src="../../../../assets/tiles/worker-disabled-min.png"
        alt="Unit"
      />
      <img
        v-else-if="isOwnUnit"
        src="../../../../assets/tiles/worker-red-hat-min.png"
        alt="Unit"
      />
      <img
        v-else-if="!isOwnUnit"
        src="../../../../assets/tiles/worker-beige-hat-min.png"
        alt="Unit"
      />
    </template>
    <template v-if="unit.type === UnitType.SWORDSMAN">
      <img
        v-if="isDisabled"
        src="../../../../assets/tiles/swordsman-disabled-min.png"
        class="swordsman"
        alt="Unit"
      />
      <img
        v-else-if="isOwnUnit"
        src="../../../../assets/tiles/swordsman-red-min.png"
        class="swordsman"
        alt="Unit"
      />
      <img
        v-else-if="!isOwnUnit"
        src="../../../../assets/tiles/swordsman-beige-min.png"
        class="swordsman"
        alt="Unit"
      />
    </template>
    <template v-if="unit.type === UnitType.HORSEMAN">
      <img
        v-if="isDisabled"
        src="../../../../assets/tiles/hosreman-disabled-min.png"
        class="hosreman"
        alt="Unit"
      />
      <img
        v-else-if="isOwnUnit"
        src="../../../../assets/tiles/hosreman-red-min.png"
        class="hosreman"
        alt="Unit"
      />
      <img
        v-else-if="!isOwnUnit"
        src="../../../../assets/tiles/hosreman-beige-min.png"
        class="hosreman"
        alt="Unit"
      />
    </template>
    <template v-if="unit.type === UnitType.SPEARMAN">
      <img
        v-if="isDisabled"
        src="../../../../assets/tiles/spearman-disabled-min.png"
        class="spearman"
        alt="Unit"
      />
      <img
        v-else-if="isOwnUnit"
        src="../../../../assets/tiles/spearman-red-min.png"
        class="spearman"
        alt="Unit"
      />
      <img
        v-else-if="!isOwnUnit"
        src="../../../../assets/tiles/spearman-beige-min.png"
        class="spearman"
        alt="Unit"
      />
    </template>
    <template v-if="unit.type === UnitType.DRAGON">
      <img
        v-if="isDisabled"
        src="../../../../assets/tiles/dragon-disabled.png"
        class="dragon"
        alt="Unit"
      />
      <img
        v-else-if="isOwnUnit"
        src="../../../../assets/tiles/dragon-red.png"
        class="dragon"
        alt="Unit"
      />
      <img
        v-else-if="!isOwnUnit"
        src="../../../../assets/tiles/dragon-beige.png"
        class="dragon"
        alt="Unit"
      />
    </template>
    <template v-if="unit.type === UnitType.ARCHER">
      <img
        v-if="isDisabled"
        src="../../../../assets/tiles/archer-disabled.png"
        class="archer"
        alt="Unit"
      />
      <img
        v-else-if="isOwnUnit"
        src="../../../../assets/tiles/archer-red.png"
        class="archer"
        alt="Unit"
      />
      <img
        v-else-if="!isOwnUnit"
        src="../../../../assets/tiles/archer-beige.png"
        class="archer"
        alt="Unit"
      />
    </template>
    <div class="countdown" :style="countdownStyle" v-if="nextMoveIn">
      {{ nextMoveIn }}
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted } from "vue";
import { ACTION, MAP_TILE_CLICKED } from "@/events.ts";
import { MapTileDto } from "@/types/dto/MapTileDto.ts";
import { UnitEntity } from "@/types/model/UnitEntity.ts";
import { useUnitsStore } from "@/store/unitsStore.ts";
import WorkerAction from "@/components/partials/game/actions/UnitAction.vue";
import { useBuildingsStore } from "@/store/buildingsStore.ts";
import { useAuthStore } from "@/store/authStore.ts";
import { UnitType } from "@/types/enum/UnitType.ts";
import { MapTileState } from "@/types/enum/MapTileState.ts";
import { useMapStore } from "@/store/mapStore.ts";

const buildingsStore = useBuildingsStore();
const unitsStore = useUnitsStore();
const authStore = useAuthStore();
const mapStore = useMapStore();
const props = defineProps<{
  unit: UnitEntity;
  mapTile: MapTileDto;
}>();

const nextMoveIn = computed(() => {
  return unitsStore.nextMoveIn(props.unit);
});

const isDisabled = computed(() => {
  return props.mapTile.state === MapTileState.FORBIDDEN;
});

const buildingOnPosition = computed(() => {
  return buildingsStore.buildings.find((building) => {
    return building.x === props.unit.x && building.y === props.unit.y;
  });
});

const isOwnUnit = computed(() => {
  return props.unit.user.id === authStore.user?.id;
});

const countdownStyle = computed<Record<string, string>>(() => {
  return {
    fontSize: `${Math.floor(mapStore.mapTileSize / 8)}px`,
  };
});

onMounted(() => {
  MAP_TILE_CLICKED.on(onMapTileClicked);
});

onBeforeUnmount(() => {
  MAP_TILE_CLICKED.off(onMapTileClicked);
});

function onMapTileClicked(mapTile: MapTileDto): void {
  if (
    props.unit.x !== mapTile.x ||
    props.unit.y !== mapTile.y ||
    buildingOnPosition.value // If there is a building on that field --> open the building action first
  ) {
    return;
  }

  unitsStore.activeUnit = props.unit;
  ACTION.dispatch(WorkerAction);
}
</script>

<style lang="scss" scoped>
.unit-tile {
  position: absolute;
  pointer-events: none;
  z-index: 3;

  img {
    width: 125%;
    height: 125%;
    margin-left: -21%;
    margin-top: -10%;

    &.swordsman {
      width: 150%;
      height: 150%;
      margin-left: 0%;
      margin-top: -25%;
    }

    &.spearman {
      width: 130%;
      height: 130%;
      margin-left: 0%;
      margin-top: -25%;
    }

    &.hosreman {
      width: 130%;
      height: 130%;
      margin-left: -5%;
      margin-top: -25%;
    }

    &.dragon {
      width: 175%;
      height: 175%;
      margin-left: -30%;
      margin-top: -45%;
    }

    &.archer {
      width: 150%;
      height: 150%;
      margin-left: -20%;
      margin-top: -25%;
    }
  }

  .countdown {
    background: rgba(0, 0, 0, 0.5);
    position: absolute;
    top: 0;
    left: 25%;
    display: flex;
    justify-content: center;
    align-items: center;
    color: white;
    text-shadow: 1px 1px 1px black;
    z-index: 50;
    transform: rotate(45deg);
  }
}
</style>
