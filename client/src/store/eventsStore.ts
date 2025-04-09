import { defineStore } from "pinia";
import { EventEntity } from "@/types/model/EventEntity.ts";
import { ref } from "vue";
import { EventGateway } from "@/gateways/EventGateway.ts";
import { useMapStore } from "@/store/mapStore.ts";
import { EventType } from "@/types/enum/EventType.ts";
import { Optional } from "@/types/core/Optional.ts";
import { EventDto } from "@/types/dto/EventDto.ts";

export const useEventsStore = defineStore("event", () => {
  const mapStore = useMapStore();
  const events = ref<Array<EventDto>>([]);
  const ownActionHappened = ref(false);
  let lastLoadX1 = 0;
  let lastLoadX2 = 0;
  let lastLoadY1 = 0;
  let lastLoadY2 = 0;

  async function loadEvents(): Promise<void> {
    function toDto(e: EventEntity): EventDto {
      return e as EventDto;
    }

    if (
      lastLoadX1 === mapStore.currentMapRange.x1 &&
      lastLoadX2 === mapStore.currentMapRange.x2 &&
      lastLoadY1 === mapStore.currentMapRange.y1 &&
      lastLoadY2 === mapStore.currentMapRange.y2
    ) {
      const response = await EventGateway.instance.getEvents(
        mapStore.currentMapRange,
        events.value?.[0]?.id,
      );
      response.forEach((event) => events.value.unshift(toDto(event)));
    } else {
      lastLoadX1 = mapStore.currentMapRange.x1;
      lastLoadX2 = mapStore.currentMapRange.x2;
      lastLoadY1 = mapStore.currentMapRange.y1;
      lastLoadY2 = mapStore.currentMapRange.y2;
      events.value = (
        await EventGateway.instance.getEvents(mapStore.currentMapRange)
      ).map(toDto);
    }
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
    ownActionHappened,
    events,
    loadEvents,
    findLatestEventByPositionAndType,
  };
});
