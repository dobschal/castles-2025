<template>
  <div>
    <p>Village of {{ buildingsStore.activeBuilding?.user.username }}</p>
    <CButton @click="close">Close</CButton>
  </div>
</template>

<script lang="ts" setup>
import { onBeforeUnmount, onMounted } from "vue";
import { useMapStore } from "@/store/mapStore.ts";
import { useBuildingsStore } from "@/store/buildingsStore.ts";
import { handleFatalError } from "@/core/util.ts";
import CButton from "@/components/partials/general/CButton.vue";

const mapStore = useMapStore();
const buildingsStore = useBuildingsStore();
const emit = defineEmits(["close-action"]);

onMounted(() => {
  if (!buildingsStore.activeBuilding) {
    return handleFatalError(new Error("No active building set"));
  }

  mapStore.mapControlsDisabled = true;
  mapStore.mapTileSize = 200;
  mapStore.goToPosition({
    x: buildingsStore.activeBuilding.x,
    y: buildingsStore.activeBuilding.y,
  });
});

onBeforeUnmount(() => {
  mapStore.mapControlsDisabled = false;
  mapStore.mapTileSize = 100;

  if (buildingsStore.activeBuilding) {
    mapStore.goToPosition({
      x: buildingsStore.activeBuilding.x,
      y: buildingsStore.activeBuilding.y,
    });
    buildingsStore.activeBuilding = undefined;
  }
});

function close(): void {
  emit("close-action");
}
</script>
