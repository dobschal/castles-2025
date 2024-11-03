<template>
  <div class="map-control-overlay">
    <CButton @click="showStartPosition" class="small home">
      <img src="@/assets/home.svg" alt="Home" />
    </CButton>
    <CButton @click="mapStore.zoomIn" class="small">+</CButton>
    <CButton @click="mapStore.zoomOut" class="small">-</CButton>
  </div>
</template>

<script setup lang="ts">
import CButton from "@/components/partials/general/CButton.vue";
import { useMapStore } from "@/store/mapStore.ts";
import { useBuildingsStore } from "@/store/buildingsStore.ts";

const buildingsStore = useBuildingsStore();
const mapStore = useMapStore();

function showStartPosition(): void {
  mapStore.goToPosition(buildingsStore.startVillage ?? { x: 0, y: 0 });
}
</script>

<style lang="scss" scoped>
.map-control-overlay {
  position: fixed;
  bottom: 1rem;
  right: 1rem;
  z-index: 98;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;

  :deep(button) {
    &.home {
      padding: 0.25rem 1rem;

      img {
        width: 1.5rem;
      }
    }
  }
}
</style>
