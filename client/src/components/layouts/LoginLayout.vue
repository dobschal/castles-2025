<template>
  <nav>
    <h1>
      <img src="@/assets/logo_white.svg" alt="Castles" />
      Castles
    </h1>
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
  padding: 0 1rem;
  display: flex;
  flex-direction: row;
  justify-content: space-between;
  align-items: center;

  h1 {
    font-size: 2rem;
    line-height: 4rem;
    margin: 0;

    img {
      height: 2rem;
      margin-right: 0rem;
      transform: translateY(0.35rem);
    }
  }

  button.burger {
    background: transparent;
    border: none;
    cursor: pointer;
  }
}

section {
  display: flex;
  width: 100%;
  height: calc(100vh - 4rem);
  overflow-y: auto;
  background: url("@/assets/background-min.png") no-repeat center center fixed;
  background-size: cover;
}
</style>
