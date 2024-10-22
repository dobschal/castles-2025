import { defineStore } from "pinia";
import { computed, ref } from "vue";
import { UserEntity } from "@/types/model/UserEntity.ts";
import { Optional } from "@/types/core/Optional.ts";

let UserGateway;
import("@/gateways/UserGateway.ts").then((module) => {
  UserGateway = module.UserGateway;
});

export const useAuthStore = defineStore(
  "auth",
  () => {
    const token = ref("");
    const user = ref<Optional<UserEntity>>();

    const hasToken = computed(() => token.value !== "");

    const isTokenExpired = computed(() => {
      if (token.value === "") {
        return true;
      }

      const payload = token.value.split(".")[1];
      const decoded = atob(payload);
      const parsed = JSON.parse(decoded);
      const expiration = parsed.exp * 1000;
      console.info(
        "Session expires in ",
        (expiration - Date.now()) / 1000 / 60 / 60 / 24,
        " days",
      );

      return Date.now() > expiration;
    });

    const roles = computed<Array<string>>(() => {
      if (token.value === "") {
        return [];
      }

      const payload = token.value.split(".")[1];
      const decoded = atob(payload);
      const parsed = JSON.parse(decoded);

      return parsed.groups;
    });

    async function loadUser(): Promise<void> {
      user.value = await UserGateway.instance.getUser();
    }

    return {
      token,
      hasToken,
      roles,
      isTokenExpired,
      user,
      loadUser,
    };
  },

  /**
   *    To keep authorisation data, like the JWT alive,
   *    persist this specific store.
   */
  { persist: true },
);
