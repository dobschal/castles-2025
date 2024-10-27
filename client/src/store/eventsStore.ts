import { defineStore } from "pinia";
import { EventEntity } from "@/types/model/EventEntity.ts";
import { ref } from "vue";
import { EventGateway } from "@/gateways/EventGateway.ts";
import { useMapStore } from "@/store/mapStore.ts";

export const useEventsStore = defineStore("event", () => {
  const mapStore = useMapStore();
  const events = ref<Array<EventEntity>>([]);

  function removeEventsNotOnCurrentMap(): void {
    for (let i = events.value.length - 1; i >= 0; i--) {
      if (
        !mapStore.isOnCurrentMap({
          x: events.value[i].x,
          y: events.value[i].y,
        })
      ) {
        events.value.splice(i, 1);
      }
    }
  }

  async function loadEvents(): Promise<void> {
    removeEventsNotOnCurrentMap();
    let ignoreEventIds = events.value.map((event) => event.id).join(",");

    if (!ignoreEventIds) {
      ignoreEventIds = "-1";
    }

    const response = await EventGateway.instance.getEvents(
      mapStore.currentMapRange,
      ignoreEventIds,
    );
    for (const eventEntity of response.reverse()) {
      const eventExists = events.value.some(
        (existingEvent) => existingEvent.id === eventEntity.id,
      );

      if (!eventExists) {
        events.value.unshift(eventEntity);
      }
    }

    events.value.sort((a, b) => b.id - a.id);
  }

  return {
    events,
    loadEvents,
  };
});
