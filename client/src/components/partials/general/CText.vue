<template>
  {{ t(path) }}
</template>

<script lang="ts" setup>
import { useI18n } from "vue-i18n";

defineProps<{
  path: string;
}>();

const { t } = useI18n();
</script>
