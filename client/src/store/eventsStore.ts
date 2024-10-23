import { defineStore } from "pinia";
import { EventEntity } from "@/types/model/EventEntity.ts";
import { ref } from "vue";
import { EventGateway } from "@/gateways/EventGateway.ts";
import { useMapStore } from "@/store/mapStore.ts";

export const useEventsStore = defineStore("event", () => {
  const mapStore = useMapStore();
  const events = ref<Array<EventEntity>>([]);

  async function loadEvents(): Promise<void> {
    const response = await EventGateway.instance.getEvents(
      mapStore.currentMapRange,
      events.value[0]?.id ?? -1,
    );
    for (const eventEntity of response.reverse()) {
      const eventExists = events.value.some(
        (existingEvent) => existingEvent.id === eventEntity.id,
      );

      if (!eventExists) {
        events.value.unshift(eventEntity);
        console.info("Event happened: ", eventEntity);
      }
    }
  }

  return {
    events,
    loadEvents,
  };
});
