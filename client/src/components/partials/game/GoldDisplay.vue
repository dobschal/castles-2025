<template>
  <div class="gold-display" :class="size ?? 'md'">
    <span>{{ gold }}</span>
    <small v-if="limit"> / {{ limit }}</small>
    <img src="@/assets/gold.png" alt="Gold" />
  </div>
</template>

<script lang="ts" setup>
defineProps<{
  gold: number;
  limit?: number;
  size?: "md" | "lg";
}>();
</script>

<style lang="scss" scoped>
.gold-display {
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
