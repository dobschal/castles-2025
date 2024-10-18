<template>
  <nav>
    <h1>Castles</h1>
    <small>
      {{ versionStore.clientVersion }} /
      {{ versionStore.serverVersion }}
    </small>
  </nav>
  <section>
    <slot />
  </section>
</template>

<script setup lang="ts">
import { useVersionStore } from "@/store/versionStore.ts";

const versionStore = useVersionStore();
</script>

<style lang="scss" scoped>
nav {
  background: rgba(117, 59, 22, 0.9);
  color: white;
  text-align: center;
  width: 100%;
  height: 4rem;
  padding: 0.5rem 0;

  h1 {
    font-size: 2rem;
    line-height: 2rem;
    margin: 0;
  }

  small {
    font-size: 0.8rem;
    line-height: 0.8rem;
    opacity: 0.5;
  }
}

section {
  display: flex;
  width: 100%;
  min-height: calc(100vh - 4rem);
}
</style>
