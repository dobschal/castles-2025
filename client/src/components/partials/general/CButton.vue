<template>
  <button type="button">
    <slot />
  </button>
</template>

<style lang="scss" scoped>
button {
  display: flex;
  gap: 1rem;
  color: white;
  background: rgb(117, 59, 22);
  box-shadow: 4px 4px 0 rgb(224, 154, 74);
  border: none;
  padding: 1rem 2rem;
  font-size: 1.2rem;
  cursor: pointer;
  line-height: 1rem;
  transition: box-shadow 0.3s ease-out;
  margin-bottom: 1rem;

  &:active {
    box-shadow: 0 0 0 rgb(224, 154, 74);
  }

  &:disabled {
    background: grey;
    box-shadow: 4px 4px 0 darkgrey;
    cursor: not-allowed;
  }

  &.small {
    padding: 0.5rem 1.5rem;
    font-size: 1rem;
  }
}
</style>
