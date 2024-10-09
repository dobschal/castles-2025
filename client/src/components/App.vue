<template>
  <component :is="$route.meta.layout ?? 'div'">
    <router-view />
  </component>
</template>

<script lang="ts" setup>
fetch("http://localhost:3000/api/v1/version")
  .then((res) => res.text())
  .then((data) => console.log(data));
</script>

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
