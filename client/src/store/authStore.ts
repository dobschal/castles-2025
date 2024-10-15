import { defineStore } from "pinia";
import { computed, ref } from "vue";

export const useAuthStore = defineStore(
  "auth",
  () => {
    const token = ref("");
    const hasToken = computed(() => token.value !== "");
    const roles = computed<Array<string>>(() => {
      if (token.value === "") {
        return [];
      }

      const payload = token.value.split(".")[1];
      const decoded = atob(payload);
      const parsed = JSON.parse(decoded);

      return parsed.groups;
    });

    return {
      token,
      hasToken,
      roles,
    };
  },

  /**
   *    To keep authorisation data, like the JWT alive,
   *    persist this specific store.
   */
  { persist: true },
);
