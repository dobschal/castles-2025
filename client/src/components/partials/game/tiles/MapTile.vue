<template>
  <div
    class="map-tile"
    @mousedown="onMouseDown"
    @mouseenter="onMouseEnter"
    @mouseleave="onMouseLeave"
    @mouseup="onMouseUp"
    :class="[mapTile.state]"
  >
    <div v-if="mapTile.state !== MapTileState.FORBIDDEN" class="image-wrapper">
      <img
        v-if="mapTile.type === MapTileType.FOREST"
        :src="forestTile"
        class="forest"
        alt="Forest"
      />
      <img
        v-else-if="mapTile.type === MapTileType.PLAIN"
        :src="plainTile"
        class="plain"
        alt="Plain"
      />
      <img
        v-else-if="mapTile.type === MapTileType.WATER"
        :src="waterTile"
        class="water"
        alt="Water"
      />
      <img
        v-else-if="mapTile.type === MapTileType.MOUNTAIN"
        :src="mountainTile"
        class="mountain"
        alt="Mountain"
      />
    </div>
    <div v-else class="image-wrapper">
      <img
        v-if="mapTile.type === MapTileType.FOREST"
        src="@/assets/tiles/forest-disabled.png"
        class="forest"
        alt="Forest"
      />
      <img
        v-else-if="mapTile.type === MapTileType.PLAIN"
        src="@/assets/tiles/plain-disabled.png"
        class="plain"
        alt="Plain"
      />
      <img
        v-else-if="mapTile.type === MapTileType.WATER"
        src="@/assets/tiles/water-disabled.png"
        class="water"
        alt="Water"
      />
      <img
        v-else-if="mapTile.type === MapTileType.MOUNTAIN"
        src="@/assets/tiles/mountain-disabled.png"
        class="mountain"
        alt="Mountain"
      />
    </div>
    <BuildingTile v-if="building" :building="building" :map-tile="mapTile" />
    <UnitTile v-if="unit" :unit="unit" :class="[mapTile.state]" />
    <div
      v-if="mapTile.state !== MapTileState.FORBIDDEN"
      class="image-top-layer-wrapper"
    >
      <img
        v-if="mapTile.type === MapTileType.FOREST"
        :src="forestTileTopLayer"
        class="forest-top-layer"
        alt="Forest"
      />
    </div>
    <div v-else class="image-top-layer-wrapper">
      <img
        v-if="mapTile.type === MapTileType.FOREST"
        src="@/assets/tiles/forest-top-layer-disabled.png"
        class="forest-top-layer"
        alt="Forest"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { MapTileType } from "@/types/enum/MapTileType.ts";
import mountainTile from "@/assets/tiles/mountain.png";
import waterTile from "@/assets/tiles/water.png";
import forestTile from "@/assets/tiles/forest.png";
import forestTileTopLayer from "@/assets/tiles/forest-top-layer.png";
import plainTile from "@/assets/tiles/plain.png";
import { MAP_TILE_CLICKED } from "@/events.ts";
import { MapTileDto } from "@/types/dto/MapTileDto.ts";
import BuildingTile from "@/components/partials/game/tiles/BuildingTile.vue";
import { computed, ref } from "vue";
import { useBuildingsStore } from "@/store/buildingsStore.ts";
import { BuildingEntity } from "@/types/model/BuildingEntity.ts";
import { Optional } from "@/types/core/Optional.ts";
import { useUnitsStore } from "@/store/unitsStore.ts";
import UnitTile from "@/components/partials/game/tiles/UnitTile.vue";
import { MapTileState } from "@/types/enum/MapTileState.ts";

const buildingsStore = useBuildingsStore();
const unitsStore = useUnitsStore();
const props = defineProps<{
  mapTile: MapTileDto;
}>();
let mouseDownTimestamp = 0;
const isMouseOver = ref(false);

const building = computed<Optional<BuildingEntity>>(() => {
  return buildingsStore.buildings.find((building) => {
    return building.x === props.mapTile.x && building.y === props.mapTile.y;
  });
});

const unit = computed(() => {
  return unitsStore.units.find((unit) => {
    return unit.x === props.mapTile.x && unit.y === props.mapTile.y;
  });
});

function onMouseDown(): void {
  mouseDownTimestamp = Date.now();
}

function onMouseUp(): void {
  if (Date.now() - mouseDownTimestamp > 200) {
    return;
  }

  MAP_TILE_CLICKED.dispatch(props.mapTile);
}

function onMouseEnter(): void {
  isMouseOver.value = true;
}

function onMouseLeave(): void {
  isMouseOver.value = false;
}
</script>

<style lang="scss" scoped>
@keyframes fade-in {
  from {
    opacity: 0.5;
    transform: translateY(+1rem) translateX(-1rem);
  }
  to {
    opacity: 1;
  }
}

.map-tile {
  position: absolute;
  user-select: none;
  animation: fade-in 0.5s;

  &.ACCEPTABLE {
    cursor: pointer;

    &:hover {
      .image-wrapper {
        img {
          filter: brightness(1.4);
        }
      }
    }
  }

  .image-wrapper {
    position: absolute;
    z-index: 0;

    img {
      display: block;
      pointer-events: none;

      &.water {
        margin-left: -30%;
        margin-top: -30%;
        width: 160%;
      }

      &.forest {
        width: 150%;
        margin-left: -25%;
        margin-top: -25%;
      }

      &.plain {
        width: 150%;
        margin-left: -30%;
        margin-top: -25%;
      }

      &.mountain {
        margin-left: -25%;
        margin-top: -30%;
        width: 150%;
      }
    }
  }

  .image-top-layer-wrapper {
    position: absolute;
    z-index: 4;

    img {
      display: block;
      pointer-events: none;

      &.forest-top-layer {
        width: 150%;
        margin-left: -25%;
        margin-top: -25%;
      }
    }
  }

  .position-text {
    position: absolute;
    bottom: 0;
    right: 0;
    background-color: rgba(255, 255, 255, 0.5);
    padding: 0.5rem;
    z-index: 10;
  }
}
</style>
