<template>
  <div class="overlay-wrapper">
    <div v-if="component" class="overlay">
      <component :is="component" @close-action="close" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ACTION } from "@/events.ts";
import { type Component, shallowRef, ShallowRef } from "vue";
import { Optional } from "@/types/core/Optional.ts";

const component: ShallowRef<Optional<Component>> = shallowRef();
let promise: Promise<void> | undefined;
let resolve: (() => void) | undefined;

ACTION.on((c: Component) => {
  component.value = c;
  promise = new Promise((r) => {
    resolve = r;
  });

  return promise;
});

function close(): void {
  component.value = undefined;
  resolve?.();
}
</script>

<style lang="scss" scoped>
.overlay-wrapper {
  position: absolute;
  bottom: 2rem;
  left: 0;
  width: 100%;
  z-index: 100;

  .overlay {
    width: 100%;
    max-width: 400px;
    margin: 0 auto;
    background: #703304;
    color: white;
    text-align: center;
    padding: 1rem;
    font-size: 1.2rem;
    box-shadow: 0.5rem 0.5rem 0.1rem 0 rgba(0, 0, 0, 0.5);
    border: solid 3px white;
  }
}
</style>
