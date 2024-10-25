<template>
  <p>
    {{
      t("breweryAction.breweryOf", {
        playerName: buildingsStore.activeBuilding?.user.username
      })
    }}
  </p>
  <CButton class="small" @click="close">
    {{ t("general.close") }}
  </CButton>
</template>

<script lang="ts" setup>
import { onBeforeUnmount, onMounted } from "vue";
import { useMapStore } from "@/store/mapStore.ts";
import { useBuildingsStore } from "@/store/buildingsStore.ts";
import { handleFatalError } from "@/core/util.ts";
import CButton from "@/components/partials/general/CButton.vue";
import { useI18n } from "vue-i18n";
import { MAP_TILE_CLICKED } from "@/events.ts";

const mapStore = useMapStore();
const buildingsStore = useBuildingsStore();
const emit = defineEmits(["close-action"]);
const { t } = useI18n();

onMounted(() => {
  if (!buildingsStore.activeBuilding) {
    return handleFatalError(new Error("No active building set"));
  }

  // When clicking on the map, close the action overlay
  MAP_TILE_CLICKED.on(close);

  mapStore.mapControlsDisabled = true;
  mapStore.mapTileSize = 200;
  mapStore.goToPosition({
    x: buildingsStore.activeBuilding.x,
    y: buildingsStore.activeBuilding.y
  });
});

onBeforeUnmount(() => {
  mapStore.mapControlsDisabled = false;
  mapStore.mapTileSize = 100;

  MAP_TILE_CLICKED.off(close);

  if (buildingsStore.activeBuilding) {
    mapStore.goToPosition({
      x: buildingsStore.activeBuilding.x,
      y: buildingsStore.activeBuilding.y
    });
    buildingsStore.activeBuilding = undefined;
  }
});

function close(): void {
  emit("close-action");
}
</script>

<style lang="scss" scoped></style>
