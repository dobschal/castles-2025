<template>
  <img class="unit-tile" src="@/assets/tiles/worker.png" alt="Unit" />
</template>

<script setup lang="ts"></script>

<style lang="scss" scoped>
.unit-tile {
  position: absolute;
  width: 140%;
  height: 140%;
  margin-left: -35%;
  margin-top: -20%;
  pointer-events: none;
  z-index: 3;
}
</style>
