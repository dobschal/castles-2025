import { RouteRecordRaw } from "vue-router";
import MainLayout from "@/components/layouts/MainLayout.vue";

/**
 * Any RouteRecordRaw object defined and exported here will be automatically
 * added to the vue router. Check twice that no paths are used twice by
 * different routes/pages.
 */

export type BeforeEnterReturnType = boolean | { path: string } | void;

export const title = "Castles";

export const MainPageRoute: RouteRecordRaw = {
  path: "/",
  name: "MainPage",
  component: () => import("@/components/pages/MainPage.vue"),
  meta: { title, layout: MainLayout },
  beforeEnter(): BeforeEnterReturnType {
    console.log("Yeah");
  },
};
