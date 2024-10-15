<template>
  <nav>
    <h1>Castles</h1>
  </nav>
  <section>
    <slot />
  </section>
</template>

<script setup lang="ts"></script>

<style lang="scss" scoped>
nav {
  background: rgb(117, 59, 22);
  color: white;
  text-align: center;
  width: 100%;
  height: 4rem;

  h1 {
    font-size: 2rem;
    line-height: 4rem;
    margin: 0;
  }
}

section {
  display: flex;
  width: 100%;
  min-height: calc(100vh - 4rem);
}
</style>
