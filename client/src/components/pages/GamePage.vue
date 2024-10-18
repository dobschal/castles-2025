<template>
  <div class="map-wrapper">
    <MapLayer></MapLayer>
  </div>
</template>

<script setup lang="ts">
import MapLayer from "@/components/partials/game/MapLayer.vue";

// TODO: Action Overlay with a message on bottom explaning the action, waiting for an event to fire to close it
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
