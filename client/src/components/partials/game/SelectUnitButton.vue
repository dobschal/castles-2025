<template>
  <CButton v-if="unit" class="small with-icon" @click="openUnit">
    <CText path="villageAction.selectUnit"></CText>
  </CButton>
</template>
<script setup lang="ts">
import CButton from "@/components/partials/general/CButton.vue";
import CText from "@/components/partials/general/CText.vue";
import { UnitEntity } from "@/types/model/UnitEntity.ts";
import { ACTION } from "@/events.ts";
import { useUnitsStore } from "@/store/unitsStore.ts";
import UnitAction from "@/components/partials/game/actions/UnitAction.vue";

const emit = defineEmits(["close"]);
const unitsStore = useUnitsStore();
const props = defineProps<{
  unit?: UnitEntity;
}>();

function openUnit(): void {
  emit("close");
  unitsStore.activeUnit = props.unit;
  ACTION.dispatch(UnitAction);
}
</script>
