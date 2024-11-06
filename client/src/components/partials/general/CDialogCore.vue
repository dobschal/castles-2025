<template>
  <div v-if="open" class="dialog-wrapper">
    <div class="dialog">
      <p>{{ t(questionKey) }}</p>
      <div class="button-group">
        <CButton @click="close">{{ t(noButtonKey) }}</CButton>
        <CButton @click="confirm">{{ t(yesButtonKey) }}</CButton>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import CButton from "@/components/partials/general/CButton.vue";
import { onMounted, ref } from "vue";
import { useI18n } from "vue-i18n";
import { DIALOG } from "@/events.ts";
import { DialogDto } from "@/types/dto/DialogDto.ts";

const open = ref(false);
const { t } = useI18n();
const noButtonKey = ref("");
const yesButtonKey = ref("");
const questionKey = ref("");
const onYes = ref<() => void>(() => {});

function close(): void {
  open.value = false;
}

function confirm(): void {
  open.value = false;
  onYes.value();
}

onMounted(() => {
  DIALOG.on((data: DialogDto) => {
    open.value = true;
    questionKey.value = data.questionKey;
    noButtonKey.value = data.noButtonKey ?? "general.no";
    yesButtonKey.value = data.yesButtonKey ?? "general.yes";
    onYes.value = data.onYes ?? (() => {});
  });
});
</script>

<style lang="scss" scoped>
.dialog-wrapper {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100vh;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 999;

  .dialog {
    background: antiquewhite;
    box-shadow: 0.5rem 0.5rem 0.1rem 0 rgba(0, 0, 0, 0.5);
    border: solid 3px rgb(117, 59, 22);
    color: black;
    padding: 1rem 1rem 0 1rem;
    width: calc(100% - 2rem);
    max-width: 400px;

    .button-group {
      display: flex;
      flex-direction: row;
      flex-wrap: nowrap;
      justify-content: center;
      gap: 1rem;

      :deep(button) {
        width: 100%;
      }
    }
  }
}
</style>
