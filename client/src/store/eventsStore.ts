import { defineStore } from "pinia";
import { EventEntity } from "@/types/model/EventEntity.ts";
import { ref } from "vue";
import { EventGateway } from "@/gateways/EventGateway.ts";
import { useMapStore } from "@/store/mapStore.ts";
import { EventType } from "@/types/enum/EventType.ts";
import { Optional } from "@/types/core/Optional.ts";

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

    const response = await EventGateway.instance.getEvents(
      mapStore.currentMapRange,
    );
    const existingEventIds = new Set(events.value.map((event) => event.id));
    for (const eventEntity of response.reverse()) {
      if (!existingEventIds.has(eventEntity.id)) {
        events.value.unshift(eventEntity);
      }
    }

    events.value.sort((a, b) => b.id - a.id);
  }

  function findLatestEventByPositionAndType(
    x: number,
    y: number,
    type: EventType,
  ): Optional<EventEntity> {
    return events.value.find(
      (event) => event.x === x && event.y === y && event.type === type,
    );
  }

  return {
    events,
    loadEvents,
    findLatestEventByPositionAndType,
  };
});
