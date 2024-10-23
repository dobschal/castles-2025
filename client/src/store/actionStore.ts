import { defineStore } from "pinia";
import { ref } from "vue";

export const useActionStore = defineStore("action", () => {
  const isActionActive = ref<boolean>(false);

  return {
    isActionActive,
  };
});
