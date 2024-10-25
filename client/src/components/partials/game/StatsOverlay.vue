<template>
  <div class="stats-overlay">
    <BeerDisplay size="lg" :beer="authStore.user?.beer ?? 0" />
  </div>
</template>

<script lang="ts" setup>
import { useAuthStore } from "@/store/authStore.ts";
import BeerDisplay from "@/components/partials/game/BeerDisplay.vue";

const authStore = useAuthStore();
</script>

<style lang="scss" scoped>
.stats-overlay {
  position: absolute;
  top: 5rem;
  left: 1rem;
  padding: 10px;
  font-size: 20px;
  color: black;
  background: antiquewhite;
  box-shadow: 0.5rem 0.5rem 0.1rem 0 rgba(0, 0, 0, 0.5);
  border: solid 3px rgb(117, 59, 22);
  z-index: 90;
}
</style>
