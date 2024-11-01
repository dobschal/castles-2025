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
import { useActionStore } from "@/store/actionStore.ts";

const component: ShallowRef<Optional<Component>> = shallowRef();
let promise: Promise<void> | undefined;
let resolve: (() => void) | undefined;
const actionStore = useActionStore();

// Actually we could have this managed via store only, but an event
// is more flexible and can be used from anywhere. Also it handles
// promises. E.g. on the StartVillageAction we wait to be finished
// before we can do anything else.
ACTION.on((c: Component) => {
  if (component.value) {
    // avoid multiple actions at the same time
    return;
  }

  actionStore.isActionActive = true;
  component.value = c;
  promise = new Promise((r) => {
    resolve = r;
  });

  return promise;
});

function close(): void {
  actionStore.isActionActive = false;
  component.value = undefined;
  resolve?.();
}
</script>

<style lang="scss" scoped>
@keyframes drive-in {
  0% {
    transform: translateY(100%);
  }
  100% {
    transform: translateY(0);
  }
}

.overlay-wrapper {
  position: absolute;
  bottom: 1rem;
  left: 1rem;
  width: calc(100% - 2rem);
  z-index: 100;

  .overlay {
    width: 100%;
    max-width: 520px;
    margin: 0 auto;
    color: black;
    padding: 1rem 1rem 0 1rem;
    font-size: 1.2rem;
    background: antiquewhite;
    box-shadow: 0.5rem 0.5rem 0.1rem 0 rgba(0, 0, 0, 0.5);
    border: solid 3px rgb(117, 59, 22);
    display: flex;
    flex-direction: column;
    align-items: center;
    animation: drive-in 0.5s cubic-bezier(0.33, 0.96, 0.52, 1.13);

    :deep(button.with-icon) {
      width: 100%;
      max-width: 320px;
      text-align: left;

      div {
        margin-left: auto;
      }
    }
  }
}
</style>
