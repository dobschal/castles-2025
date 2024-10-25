<template>
  <p>👉 {{ t("unitAction.chooseAction") }}</p>
  <CButton v-if="unitsStore.activeUnit" class="small" @click="showMoveAction">
    {{ t("villageAction.moveUnit") }}
    <BeerDisplay
      :beer="pricesStore.getMovePrice(unitsStore.activeUnit?.type)"
    />
  </CButton>
  <CButton class="small" @click="close">
    {{ t("general.cancel") }}
  </CButton>
</template>

<script lang="ts" setup>
import { useI18n } from "vue-i18n";
import CButton from "@/components/partials/general/CButton.vue";
import { onBeforeUnmount } from "vue";
import { useUnitsStore } from "@/store/unitsStore.ts";
import { ACTION } from "@/events.ts";
import UnitMoveAction from "@/components/partials/game/actions/UnitMoveAction.vue";
import BeerDisplay from "@/components/partials/game/BeerDisplay.vue";
import { usePricesStore } from "@/store/pricesStore.ts";

const unitsStore = useUnitsStore();
const pricesStore = usePricesStore();

const { t } = useI18n();
const emit = defineEmits(["close-action"]);

onBeforeUnmount(() => {
  unitsStore.activeUnit = undefined;
});

function close(): void {
  emit("close-action");
}

function showMoveAction(): void {
  unitsStore.activeMoveUnit = unitsStore.activeUnit;
  close();
  ACTION.dispatch(UnitMoveAction);
}
</script>
