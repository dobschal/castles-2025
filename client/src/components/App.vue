<template>
  <component :is="$route.meta.layout ?? 'div'">
    <router-view />
  </component>
  <CToastCore />
  <CDialogCore />
</template>

<script lang="ts" setup>
import { onMounted } from "vue";
import { VersionGateway } from "@/gateways/VersionGateway.ts";
import { useVersionStore } from "@/store/versionStore.ts";
import CToastCore from "@/components/partials/general/CToastCore.vue";
import CDialogCore from "@/components/partials/general/CDialogCore.vue";
import { handleFatalError } from "@/core/util.ts";

onMounted(() => {
  loadServerVersion();
});

async function loadServerVersion(): Promise<void> {
  try {
    useVersionStore().serverVersion =
      await VersionGateway.instance.getVersion();
  } catch (error) {
    handleFatalError(error);
  }
}
</script>

<style lang="scss">
@font-face {
  font-family: "MedievalSharp";
  src: url("@/assets/fonts/MedievalSharp-Regular.ttf") format("truetype");
}

// Disable pull to refresh
html {
  overflow: hidden;
  overscroll-behavior: none;
}

body {
  font-size: 16px;
  line-height: 1.5;
  font-family: "MedievalSharp", sans-serif;
  margin: 0;
  width: 100%;
  min-height: 100vh;
  background: antiquewhite;
  touch-action: none;
}

* {
  font-family: "MedievalSharp", sans-serif;
  box-sizing: border-box;
  touch-action: none;
}

h1 {
  font-size: 2rem;
  margin: 0 0 1rem 0;
}

h2 {
  font-size: 1.5rem;
  margin: 0 0 1rem 0;
}

hr {
  border: none;
  border-top: 4px solid rgb(117, 59, 22);
  margin: 0 0 1rem 0;
  box-shadow: 2px 2px 0 rgb(224, 154, 74);
}

p {
  margin: 0 0 1rem 0;
}
</style>
