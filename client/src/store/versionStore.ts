import { defineStore } from "pinia";
import { ref } from "vue";
import { version } from "../../package.json";

export const useVersionStore = defineStore("version", () => {
  const serverVersion = ref("");
  const clientVersion = ref(version);

  return { serverVersion, clientVersion };
});
