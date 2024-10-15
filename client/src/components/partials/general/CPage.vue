<template>
  <div>
    <slot />
  </div>
</template>

<style lang="scss" scoped>
div {
  margin: auto;
  display: flex;
  flex-direction: column;
  width: 100%;
  max-width: 640px;
  padding: 0 2rem;
}
</style>
