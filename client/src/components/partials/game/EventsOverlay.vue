<template>
  <div v-if="isOpen" class="events-overlay">
    <div
      class="entry"
      v-for="event in events"
      :key="event.id"
      @click="goToEventPosition(event)"
    >
      <small>{{ event.duration }}: </small><br />
      <span>{{ t(event.translationKey, event.params) }}</span>
    </div>
    <div v-if="events.length === 0 || !events">
      <span>{{ t("events.noEvents") }}</span>
    </div>
  </div>
  <CButton @click="toggle" class="small toggle-button">
    {{ isOpen ? t("general.close") : t("events.openOverlay") }}
  </CButton>
</template>

<script lang="ts" setup>
import { useEventsStore } from "@/store/eventsStore.ts";
import { useI18n } from "vue-i18n";
import { computed, ref } from "vue";
import { useAuthStore } from "@/store/authStore.ts";
import CButton from "@/components/partials/general/CButton.vue";
import { useMapStore } from "@/store/mapStore.ts";

const authStore = useAuthStore();
const eventsStore = useEventsStore();
const mapStore = useMapStore();
const { t } = useI18n();
const isOpen = ref(false);

interface CustomEvent {
  id: number;
  translationKey: string;
  params: {
    x: number;
    y: number;
    playerName: string;
  };
  duration: string;
}

const events = computed<Array<CustomEvent>>(() => {
  return eventsStore.events.map((event) => {
    const isOwnEvent = event.user1.id === authStore.user?.id;

    return {
      id: event.id,
      translationKey: `events.${isOwnEvent ? "own" : "other"}.${event.type}`,
      params: {
        x: event.x,
        y: event.y,
        playerName: event.user1.username,
      },
      duration: calculateDuration(
        new Date(),
        new Date(Date.parse(event.createdAt)),
      ),
    };
  });
});

function goToEventPosition(event: CustomEvent): void {
  mapStore.goToPosition(event.params);
}

function calculateDuration(date1: Date, date2: Date): string {
  const diff = date1.getTime() - date2.getTime();
  const minutes = Math.floor((diff / 1000 / 60) % 60);
  const hours = Math.floor(diff / 1000 / 60 / 60);

  if (minutes === 0 && hours === 0) {
    return t("general.lessThanMinute");
  }

  if (hours === 0) {
    return t("general.ago", [`${minutes}m`]);
  }

  return t("general.ago", [`${hours}h ${minutes}m`]);
}

function toggle(): void {
  isOpen.value = !isOpen.value;
}
</script>

<style lang="scss" scoped>
.toggle-button {
  position: fixed;
  bottom: 1rem;
  left: 1rem;
  z-index: 99;
}

.events-overlay {
  position: fixed;
  bottom: 5rem;
  left: 1rem;
  z-index: 99;
  background: antiquewhite;
  box-shadow: 0.5rem 0.5rem 0.1rem 0 rgba(0, 0, 0, 0.5);
  border: solid 3px rgb(117, 59, 22);
  padding: 1rem;
  color: black;
  line-height: 1rem;
  width: calc(100% - 2rem);
  max-width: 400px;
  max-height: 200px;
  overflow-y: auto;

  .close-button {
    margin-left: auto;
    padding: 0.5rem;
  }

  .entry {
    margin-bottom: 0.5rem;

    &:hover,
    &:active {
      cursor: pointer;
      background: rgba(0, 0, 0, 0.1);
    }
  }
}
</style>
