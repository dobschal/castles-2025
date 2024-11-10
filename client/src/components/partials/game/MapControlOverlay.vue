<template>
  <div class="map-control-overlay">
    <template v-if="ownVillages.length > 1">
      <CButton
        v-for="(village, index) in ownVillages"
        @click="showVillage(village)"
        class="small home"
      >
        <img src="@/assets/home.svg" alt="Home" />
        <span class="index">{{ index + 1 }}</span>
      </CButton>
    </template>
    <CButton v-else @click="showStartPosition" class="small home">
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
import { onMounted, ref } from "vue";
import { BuildingGateway } from "@/gateways/BuildingGateway.ts";
import { BuildingEntity } from "@/types/model/BuildingEntity.ts";
import { useAuthStore } from "@/store/authStore.ts";
import { BuildingType } from "@/types/enum/BuildingType.ts";

const buildingsStore = useBuildingsStore();
const mapStore = useMapStore();
const authStore = useAuthStore();

const ownVillages = ref<Array<BuildingEntity>>([]);

onMounted(() => {
  loadBuildings();
});

function showVillage(village: BuildingEntity): void {
  mapStore.goToPosition(village);
}

async function loadBuildings(): Promise<void> {
  if (!authStore.user) return;
  const response = await BuildingGateway.instance.getBuildingsByUser(
    authStore.user?.id ?? 0,
  );
  ownVillages.value = response.filter((building) =>
    [BuildingType.VILLAGE, BuildingType.CITY].includes(building.type),
  );
}

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
  user-select: none;

  :deep(button) {
    &.home {
      padding: 0.25rem 1rem;
      position: relative;

      .index {
        position: absolute;
        top: 0;
        left: 0;
        width: 100%;
        text-align: center;
        line-height: 2.2rem;
        font-size: 0.75rem;
      }

      img {
        width: 1.5rem;
      }
    }
  }
}
</style>
