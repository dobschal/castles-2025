<template>
  <div class="stats-overlay">
    <div>
      <BeerDisplay
        size="lg"
        :beer="authStore.user?.beer ?? 0"
        :limit="buildingsStore.maxBeerStorage"
      />
    </div>
    <div :class="{ disabled: buildingsStore.totalGoldStorage === 0 }">
      <GoldDisplay
        :gold="authStore.user?.gold ?? 0"
        :limit="buildingsStore.totalGoldStorage"
        size="lg"
      />
    </div>
  </div>
</template>

<script lang="ts" setup>
import { useAuthStore } from "@/store/authStore.ts";
import BeerDisplay from "@/components/partials/game/BeerDisplay.vue";
import { useBuildingsStore } from "@/store/buildingsStore.ts";
import GoldDisplay from "@/components/partials/game/GoldDisplay.vue";

const authStore = useAuthStore();
const buildingsStore = useBuildingsStore();
</script>

<style lang="scss" scoped>
.stats-overlay {
  position: absolute;
  top: 5rem;
  left: 1rem;
  z-index: 90;
  display: flex;
  gap: 1rem;

  & > * {
    padding: 10px;
    color: black;
    background: antiquewhite;
    box-shadow: 0.5rem 0.5rem 0.1rem 0 rgba(0, 0, 0, 0.5);
    border: solid 3px rgb(117, 59, 22);

    &.disabled {
      opacity: 0.5;
    }
  }
}
</style>
