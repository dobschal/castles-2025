import { RouteRecordRaw } from "vue-router";
import MainLayout from "@/components/layouts/MainLayout.vue";
import { useAuthStore } from "@/store/authStore.ts";
import LoginLayout from "@/components/layouts/LoginLayout.vue";
import GameLayout from "@/components/layouts/GameLayout.vue";

/**
 * Any RouteRecordRaw object defined and exported here will be automatically
 * added to the vue router. Check twice that no paths are used twice by
 * different routes/pages.
 */

export type BeforeEnterReturnType = boolean | { path: string } | void;

export const title = "Castles";

export const LoginPageRoute: RouteRecordRaw = {
  path: "/login",
  name: "LoginPage",
  component: () => import("@/components/pages/Login.vue"),
  meta: { title, layout: LoginLayout },
  beforeEnter(): BeforeEnterReturnType {
    const authStore = useAuthStore();

    if (authStore.hasToken) {
      return { path: "/" };
    }
  },
};

export const RegistrationPageRoute: RouteRecordRaw = {
  path: "/registration",
  name: "RegistrationPage",
  component: () => import("@/components/pages/Registration.vue"),
  meta: { title, layout: LoginLayout },
  beforeEnter(): BeforeEnterReturnType {
    const authStore = useAuthStore();

    if (authStore.hasToken) {
      return { path: "/" };
    }
  },
};

export const WikiPageRoute: RouteRecordRaw = {
  path: "/wiki",
  name: "WikiPage",
  component: () => import("@/components/pages/Wiki.vue"),
  meta: { title, layout: MainLayout },
};

export const UserListPageRoute: RouteRecordRaw = {
  path: "/users",
  name: "UserListPage",
  component: () => import("@/components/pages/UserListPage.vue"),
  meta: { title, layout: MainLayout, roles: ["user"] },
};

export const UserProfilePageRoute: RouteRecordRaw = {
  path: "/user-profile",
  name: "UserProfilePage",
  component: () => import("@/components/pages/UserProfilePage.vue"),
  meta: { title, layout: MainLayout, roles: ["user"] },
};

export const MainPageRoute: RouteRecordRaw = {
  path: "/",
  name: "GamePage",
  component: () => import("@/components/pages/Game.vue"),
  meta: { title, layout: GameLayout, roles: ["user"] },
};
