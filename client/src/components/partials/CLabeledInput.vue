<template>
  <div>
    <label :for="componentId">
      <slot />
    </label>
    <input
      :autofocus="autofocus"
      :type="type"
      :id="componentId"
      :value="modelValue"
      @change="valueUpdated"
    />
  </div>
</template>

<script lang="ts" setup>
import { getComponentId } from "@/core/util.ts";
import { ref } from "vue";

withDefaults(
  defineProps<{
    modelValue: string;
    type?: "text" | "number" | "password";
    placeholder?: string;
    autofocus?: boolean;
  }>(),
  {
    type: "text",
    placeholder: "...",
    autofocus: false,
  },
);

const componentId = ref(getComponentId());
const emit = defineEmits(["update:modelValue"]);

function valueUpdated(event: Event): void {
  emit("update:modelValue", (event.target as HTMLInputElement).value);
}
</script>

<style lang="scss" scoped>
div {
  display: flex;
  flex-direction: column;
  margin-bottom: 1rem;
}

label {
  font-size: 1rem;
}

input {
  padding: 0.5rem;
  font-size: 1rem;
  line-height: 1rem;
  border: 4px solid rgb(117, 59, 22);
  outline: none;
}
</style>
