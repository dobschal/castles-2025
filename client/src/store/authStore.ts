import { defineStore } from "pinia";

export const useAuthStore = defineStore(
  "auth",
  () => {
    return {};
  },

  /**
   *    To keep authorisation data, like the JWT alive,
   *    persist this specific store.
   */
  { persist: true },
);
