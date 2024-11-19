<template>
  <div class="beer-display" :class="size ?? 'md'">
    <span>
      {{ beer }}
      <br v-if="limit" />
      <small v-if="limit">{{ limit }}</small>
    </span>
    <img src="../../../../assets/beer-min.png" alt="Beer" />
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
  font-size: 1rem;
  gap: 0.5rem;

  &.lg {
    font-size: 1.3rem;
    height: 1.5rem;
  }

  img {
    height: 100%;
  }

  span {
    line-height: 0.6;
    display: block;
    padding-top: 0.2rem;
    letter-spacing: -0.05rem;
  }

  small {
    font-size: 0.7rem;
    opacity: 0.5;
  }
}
</style>
