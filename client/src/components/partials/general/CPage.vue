<template>
  <div>
    <slot />
  </div>
</template>

<style lang="scss" scoped>
div {
  margin: 1rem auto;
  display: flex;
  flex-direction: column;
  width: calc(100% - 2rem);
  max-width: 640px;
  color: black;
  padding: 1rem 1rem 0 1rem;
  font-size: 1.2rem;
  background: antiquewhite;
  box-shadow: 0.5rem 0.5rem 0.1rem 0 rgba(0, 0, 0, 0.5);
  border: solid 3px rgb(117, 59, 22);
  height: fit-content;
  touch-action: pan-y pan-x;

  * {
    touch-action: pan-y pan-x;
  }
}
</style>
