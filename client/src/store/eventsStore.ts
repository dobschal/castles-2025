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

  async function loadEvents(): Promise<void> {
    events.value = await EventGateway.instance.getEvents(
      mapStore.currentMapRange,
    );
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
