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
    <div
      :class="{ disabled: buildingsStore.totalGoldStorage === 0 }"
      @click="openUnitsAndBuildingsPage"
    >
      <UnitCountDisplay
        :units-count="unitsStore.unitsCount"
        :units-limit="unitsStore.unitsLimit"
        size="lg"
      />
    </div>
  </div>
</template>

<script lang="ts" setup>
import { useAuthStore } from "@/store/authStore.ts";
import BeerDisplay from "@/components/partials/game/displays/BeerDisplay.vue";
import { useBuildingsStore } from "@/store/buildingsStore.ts";
import GoldDisplay from "@/components/partials/game/displays/GoldDisplay.vue";
import UnitCountDisplay from "@/components/partials/game/displays/UnitCountDisplay.vue";
import { useUnitsStore } from "@/store/unitsStore.ts";
import router from "@/core/router.ts";

const authStore = useAuthStore();
const buildingsStore = useBuildingsStore();
const unitsStore = useUnitsStore();

function openUnitsAndBuildingsPage(): void {
  router.push(`/units-and-buildings?user_id=${authStore.user?.id}`);
}
</script>

<style lang="scss" scoped>
.stats-overlay {
  position: absolute;
  top: 4.5rem;
  left: 0.5rem;
  z-index: 90;
  display: flex;
  gap: 0.5rem;

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
