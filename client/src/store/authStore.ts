import { defineStore } from "pinia";
import { computed, ref } from "vue";
import { UserEntity } from "@/types/model/UserEntity.ts";
import { Optional } from "@/types/core/Optional.ts";
import { handleFatalError } from "@/core/util.ts";

let UserGateway;
import("@/gateways/UserGateway.ts").then((module) => {
  UserGateway = module.UserGateway;
});

export const useAuthStore = defineStore(
  "auth",
  () => {
    const token = ref("");
    const user = ref<Optional<UserEntity>>();
    const showEventsOnMap = ref(true);
    const audioPaused = ref(false);

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
        Math.floor((expiration - Date.now()) / 1000 / 60 / 60 / 24),
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
      if (!UserGateway) {
        setTimeout(() => loadUser(), 100);

        return;
      }

      try {
        user.value = await UserGateway.instance.getUser();
      } catch (error) {
        handleFatalError(error);
      }
    }

    return {
      showEventsOnMap,
      token,
      hasToken,
      roles,
      isTokenExpired,
      user,
      loadUser,
      audioPaused,
    };
  },

  /**
   *    To keep authorisation data, like the JWT alive,
   *    persist this specific store.
   */
  { persist: true },
);
