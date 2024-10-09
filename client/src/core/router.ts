import {
  createRouter,
  createWebHistory,
  RouteLocationNormalized,
  RouteRecordRaw,
} from "vue-router";
import * as routesModule from "@/routes";
import { MainPageRoute, title } from "@/routes";

// Load all Route config objects exported from routes.ts
export const routes = Object.values(routesModule)
  .filter((routeRecord) => {
    return (
      routeRecord &&
      typeof routeRecord === "object" &&
      "path" in routeRecord &&
      "component" in routeRecord &&
      MainPageRoute !== routeRecord
    );
  })
  .map((routeRecord) => routeRecord as RouteRecordRaw);

// The fallback route needs to be sorted last in the array
routes.push(MainPageRoute);

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes,
  scrollBehavior(to: RouteLocationNormalized) {
    if (to.hash) {
      return { selector: to.hash };
    } else {
      return { left: 0, top: 0 };
    }
  },
});

router.beforeEach(async (to: RouteLocationNormalized) => {
  document.title = to.meta.title ? `${to.meta.title}` : title;
});

export default router;
