<template>
  <component :is="$route.meta.layout ?? 'div'">
    <router-view />
  </component>
</template>

<script lang="ts" setup></script>

<style lang="css">
.v-enter-active {
  transition:
    opacity 0.3s ease-in-out,
    transform 0.3s ease-in;
}

.v-enter-from {
  opacity: 0;
  transform: translateY(2rem);
}
</style>
