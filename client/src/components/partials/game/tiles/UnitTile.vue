<template>
  <div class="unit-tile">
    <img
      v-if="unit.type === UnitType.WORKER && isOwnUnit"
      src="../../../../assets/tiles/worker-red-hat.png"
      alt="Unit"
    />
    <img
      v-else-if="unit.type === UnitType.WORKER && !isOwnUnit"
      src="../../../../assets/tiles/worker-beige-hat.png"
      alt="Unit"
    />
  </div>
</template>

<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted } from "vue";
import { ACTION, MAP_TILE_CLICKED } from "@/events.ts";
import { MapTileDto } from "@/types/dto/MapTileDto.ts";
import { UnitEntity } from "@/types/model/UnitEntity.ts";
import { useUnitsStore } from "@/store/unitsStore.ts";
import UnitAction from "@/components/partials/game/actions/UnitAction.vue";
import { useBuildingsStore } from "@/store/buildingsStore.ts";
import { useAuthStore } from "@/store/authStore.ts";
import { UnitType } from "@/types/enum/UnitType.ts";

const buildingsStore = useBuildingsStore();
const unitsStore = useUnitsStore();
const authStore = useAuthStore();
const props = defineProps<{
  unit: UnitEntity;
}>();
const buildingOnPosition = computed(() => {
  return buildingsStore.buildings.find((building) => {
    return building.x === props.unit.x && building.y === props.unit.y;
  });
});
const isOwnUnit = computed(() => {
  return props.unit.user.id === authStore.user?.id;
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
    !isOwnUnit.value ||
    buildingOnPosition.value // If there is a building on that field --> open the building action first
  ) {
    return;
  }

  unitsStore.activeUnit = props.unit;
  ACTION.dispatch(UnitAction);
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
  }
}
</style>
