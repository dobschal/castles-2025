<template>
  <div class="beer-display" :class="size ?? 'md'">
    <span>{{ beer }}</span>
    <small v-if="limit"> / {{ limit }}</small>
    <img src="@/assets/beer-min.png" alt="Beer" />
  </div>
</template>

<script lang="ts" setup>
defineProps<{
  beer: number;
  limit?: number;
  size?: "md" | "lg";
}>();
</script>

<style lang="scss" scoped>
.beer-display {
  height: 1rem;
  display: flex;
  align-items: center;
  gap: 0.25rem;
  font-size: 1rem;

  &.lg {
    font-size: 1.3rem;
    height: 1.5rem;
  }

  img {
    height: 100%;
  }

  small {
    font-size: 0.8rem;
    transform: translateY(0.1rem);
  }
}
</style>
