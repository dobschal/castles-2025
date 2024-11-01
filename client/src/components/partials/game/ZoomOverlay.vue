<template>
  <div class="zoom-overlay">
    <CButton @click="mapStore.zoomIn" class="small">+</CButton>
    <CButton @click="mapStore.zoomOut" class="small">-</CButton>
  </div>
</template>

<style lang="scss" scoped>
.zoom-overlay {
  position: fixed;
  bottom: 1rem;
  right: 1rem;
  z-index: 98;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
}
</style>

<script setup lang="ts">
import CButton from "@/components/partials/general/CButton.vue";
import { useMapStore } from "@/store/mapStore.ts";

const mapStore = useMapStore();
</script>
