<template>
  <component :is="$route.meta.layout ?? 'div'">
    <router-view />
  </component>
</template>

<script lang="ts" setup>
import { API_URL } from "@/constants.ts";

console.info("API URL: ", API_URL);
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
