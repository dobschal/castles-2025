import { createApp } from "vue";
import App from "@/components/App.vue";
import router from "./core/router";
import { createPinia } from "pinia";
import piniaPluginPersistedState from "pinia-plugin-persistedstate";
import { i18n } from "@/core/i18n";

const app = createApp(App);

const pinia = createPinia();
pinia.use(piniaPluginPersistedState);

app.use(router);
app.use(pinia);
app.use(i18n);

app.mount("#app");
