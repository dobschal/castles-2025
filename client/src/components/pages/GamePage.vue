<template>
  <div class="map-wrapper">
    <MapLayer></MapLayer>
  </div>
  <ActionOverlay />
</template>

<script setup lang="ts">
import MapLayer from "@/components/partials/game/MapLayer.vue";
import { useBuildingLogic } from "@/composables/BuildingLogic.ts";
import ActionOverlay from "@/components/partials/game/ActionOverlay.vue";

useBuildingLogic();
</script>

<style lang="scss" scoped>
.map-wrapper {
  position: relative;
  width: 100%;
  height: calc(100vh - 4rem);
  overflow: hidden;
  background: black;
}
</style>
