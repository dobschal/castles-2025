<template>
  <div class="toast-container" v-if="toasts.length > 0">
    <transition-group name="toast">
      <div
        v-for="toast of toasts"
        :key="toast.id"
        class="toast"
        :class="toast.type"
        @mousemove="onMouseMove(toast)"
        @click="getRemoveToastFunction(toast.id)()"
      >
        <div class="my-auto">
          {{
            toast.message ??
            t(toast.messageKey ?? "N/A", toast.messageParams ?? [])
          }}
        </div>
        <div
          class="timeout-indicator"
          :style="{
            width: 100 - timeoutPercentage(toast) + '%',
            left: timeoutPercentage(toast) / 2 + '%',
          }"
        ></div>
      </div>
    </transition-group>
  </div>
</template>

<script setup lang="ts">
import { ToastConfig } from "@/types/core/ToastConfig.ts";
import { onBeforeUnmount, onMounted, ref } from "vue";
import { useI18n } from "vue-i18n";
import { TOAST } from "@/events.ts";

interface ToastModel extends ToastConfig {
  id: number;
  timeout: ReturnType<typeof setTimeout>;
  timestamp: number;
}

const { t } = useI18n();
const displayDuration = 5000;
let toastCount = 0;
const toasts = ref<Array<ToastModel>>([]);
const now = ref(Date.now());
let interval;

TOAST.on((config: ToastConfig) => {
  const id = toastCount++;
  toasts.value.push({
    timestamp: Date.now(),
    id,
    timeout: setTimeout(getRemoveToastFunction(id), displayDuration),
    ...config,
  });
});

onMounted(() => {
  interval = setInterval(() => {
    now.value = Date.now();
  }, 40);
});

onBeforeUnmount(() => {
  clearInterval(interval);
});

function timeoutPercentage(toast: ToastModel): number {
  return Math.max(
    0,
    Math.floor(((now.value - toast.timestamp) / displayDuration) * 100),
  );
}

function getRemoveToastFunction(id: number) {
  return () => {
    const index = toasts.value.findIndex((t) => t.id === id);

    if (index === -1) return;
    toasts.value.splice(index, 1);
  };
}

// On hover, we increase the display time of the toast again and again
function onMouseMove(toast: ToastModel): void {
  clearInterval(toast.timeout);
  toast.timestamp = Date.now();
  toast.timeout = setTimeout(getRemoveToastFunction(toast.id), displayDuration);
}
</script>

<style lang="scss" scoped>
.toast-container {
  position: fixed;
  top: 4rem;
  right: 0;
  max-width: 320px;
  width: 66%;
  padding: 1rem;
  z-index: 999;

  .toast {
    box-shadow: 0.5rem 0.5rem 0.1rem 0 rgba(0, 0, 0, 0.5);
    position: relative;
    font-size: 0.875rem;
    padding: 0.5rem 1rem;
    color: #ffffff;
    margin-bottom: 0.5rem;
    display: flex;
    flex-direction: row;
    gap: 0.5rem;
    cursor: pointer;

    svg {
      display: block;
      margin: auto 0;
      width: 1.75rem;
      flex-shrink: 0;
    }

    .timeout-indicator {
      width: 100%;
      position: absolute;
      bottom: 0;
      left: 0;
      border-bottom: solid 3px rgba(255, 255, 255, 0.66);
    }

    &.info {
      background-color: #4b5563;
    }

    &.danger {
      background-color: #c05151;
    }

    &.success {
      background-color: #82ad00;
    }

    &.warning {
      background-color: #fbbf24;
      color: #4b5563;
    }
  }
}

.toast-enter-active,
.toast-leave-active {
  transition:
    opacity 0.3s ease-in-out,
    transform 0.5s cubic-bezier(0.17, 0.67, 0.4, 1.35);
}

.toast-enter-from,
.toast-leave-to {
  opacity: 0;
  transform: translateX(320px);
}
</style>
