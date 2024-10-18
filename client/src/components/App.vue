<template>
  <component :is="$route.meta.layout ?? 'div'">
    <router-view />
  </component>
  <CToastCore />
</template>

<script lang="ts" setup>
import { onMounted } from "vue";
import { VersionGateway } from "@/gateways/VersionGateway.ts";
import { useVersionStore } from "@/store/versionStore.ts";
import { SHOW_TOAST } from "@/events.ts";
import CToastCore from "@/components/partials/general/CToastCore.vue";

onMounted(() => {
  loadServerVersion();
});

async function loadServerVersion(): Promise<void> {
  try {
    useVersionStore().serverVersion =
      await VersionGateway.instance.getVersion();
  } catch (error) {
    console.error("Error on loading server version: ", error);
    SHOW_TOAST.dispatch({
      type: "danger",
      messageKey: "general.serverError",
    });
  }
}
</script>

<style lang="scss">
@font-face {
  font-family: "MedievalSharp";
  src: url("@/assets/fonts/MedievalSharp-Regular.ttf") format("truetype");
}

body {
  font-size: 16px;
  line-height: 1.5;
  font-family: "MedievalSharp", sans-serif;
  margin: 0;
  width: 100%;
  min-height: 100vh;
  background: antiquewhite;
}

* {
  font-family: "MedievalSharp", sans-serif;
  box-sizing: border-box;
}

hr {
  border: none;
  border-top: 4px solid rgb(117, 59, 22);
  margin: 0 0 1rem 0;
  box-shadow: 2px 2px 0 rgb(224, 154, 74);
}
</style>
