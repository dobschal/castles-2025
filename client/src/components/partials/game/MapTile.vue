<template>
  <div
    class="map-tile"
    @mousedown="onMouseDown"
    @mouseenter="onMouseEnter"
    @mouseleave="onMouseLeave"
    @mouseup="onMouseUp"
    :class="[mapTile.state]"
  >
    <div class="image-wrapper">
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
    <div v-if="isMouseOver" class="position-text">
      {{ mapTile.x }} / {{ mapTile.y }}
    </div>
    <BuildingTile
      v-if="building"
      :building="building"
      :class="[mapTile.state]"
    />
    <UnitTile v-if="unit" :unit="unit" :class="[mapTile.state]" />
  </div>
</template>

<script setup lang="ts">
import { MapTileType } from "@/types/enum/MapTileType.ts";
import mountainTile from "@/assets/tiles/mountain.png";
import waterTile from "@/assets/tiles/water.png";
import forestTile from "@/assets/tiles/forest.png";
import plainTile from "@/assets/tiles/plain.png";
import { MAP_TILE_CLICKED } from "@/events.ts";
import { MapTileDto } from "@/types/dto/MapTileDto.ts";
import BuildingTile from "@/components/partials/game/BuildingTile.vue";
import { computed, ref } from "vue";
import { useBuildingsStore } from "@/store/buildingsStore.ts";
import { BuildingEntity } from "@/types/model/BuildingEntity.ts";
import { Optional } from "@/types/core/Optional.ts";
import { useUnitsStore } from "@/store/unitsStore.ts";
import UnitTile from "@/components/partials/game/UnitTile.vue";

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

// TODO: Need touch support too

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
.map-tile {
  position: absolute;
  user-select: none;

  // TODO: The image filter are CPU intensive, --> use pre generated images instead

  &.FORBIDDEN {
    .image-wrapper {
      img {
        filter: sepia(0.75) brightness(0.75);
      }
    }
  }

  &.ACCEPTABLE {
    cursor: pointer;

    &:hover {
      .image-wrapper {
        img {
          filter: brightness(1.4);
        }
      }
    }

    .image-wrapper {
      img {
        filter: brightness(1.1);
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

  .position-text {
    position: absolute;
    bottom: 0;
    right: 0;
    background-color: rgba(255, 255, 255, 0.5);
    padding: 0.5rem;
  }
}
</style>
