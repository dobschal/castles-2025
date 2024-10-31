<template>
  <p>
    <span v-if="canBeCompleted">✅ </span><span v-else>👉 </span>
    <span v-html="t('tutorialAction.' + tutorialStore.tutorial?.type)"></span>
  </p>
  <CButton
    v-if="canBeCompleted"
    class="small"
    @click="complete"
    :disabled="isLoading"
  >
    {{ t("tutorialAction.complete") }}
  </CButton>
  <CButton v-else class="small" @click="close" :disabled="isLoading">
    OK
  </CButton>
</template>

<script lang="ts" setup>
import { useI18n } from "vue-i18n";
import CButton from "@/components/partials/general/CButton.vue";
import { useTutorialStore } from "@/store/tutorialStore.ts";
import { computed, onBeforeUnmount, onMounted, ref } from "vue";
import { TutorialStatus } from "@/types/enum/TutorialStatus.ts";
import { TutorialGateway } from "@/gateways/TutorialGateway.ts";
import { handleFatalError } from "@/core/util.ts";
import { MAP_TILE_CLICKED } from "@/events.ts";

const { t } = useI18n();
const emit = defineEmits(["close-action"]);
const tutorialStore = useTutorialStore();
const isLoading = ref(false);

const canBeCompleted = computed(() => {
  return tutorialStore.tutorial?.status === TutorialStatus.CAN_BE_COMPLETED;
});

onMounted(() => {
  MAP_TILE_CLICKED.on(close);
});

onBeforeUnmount(() => {
  MAP_TILE_CLICKED.off(close);
});

function close(): void {
  emit("close-action");
}

async function complete(): Promise<void> {
  try {
    if (!tutorialStore.tutorial?.id) return;
    isLoading.value = true;
    await TutorialGateway.instance.completeTutorial(tutorialStore.tutorial?.id);
    close();
    await tutorialStore.loadAndShowTutorial();
  } catch (error) {
    handleFatalError(error);
  } finally {
    isLoading.value = false;
  }
}
</script>
