<template>
  <div class="map-wrapper">
    <MapLayer></MapLayer>
  </div>
</template>

<script setup lang="ts">
import MapLayer from "@/components/partials/game/MapLayer.vue";
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
