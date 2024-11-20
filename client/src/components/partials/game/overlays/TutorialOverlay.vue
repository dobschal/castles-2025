<template>
  <CButton
    v-if="hasTutorial"
    @click="openTutorialAction"
    class="small tutorial-button"
  >
    <img src="../../../../assets/scroll-min.png" alt="Tutorial" />
  </CButton>
</template>

<script setup lang="ts">
import CButton from "@/components/partials/general/CButton.vue";
import { useTutorialStore } from "@/store/tutorialStore.ts";
import { computed } from "vue";

const tutorialStore = useTutorialStore();

const hasTutorial = computed(() => {
  return !!tutorialStore.tutorial;
});

async function openTutorialAction(): Promise<void> {
  await tutorialStore.loadAndShowTutorial();
}
</script>

<style lang="scss" scoped>
.tutorial-button {
  position: fixed;
  top: 5rem;
  right: 1rem;
  z-index: 99;

  img {
    width: 3rem;
    margin: -0.5rem -0.5rem;
  }
}
</style>
