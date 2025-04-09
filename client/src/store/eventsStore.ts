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
    const tileSize = mapStore.mapTileSize;
    const tileSizeHalf = tileSize / 2;

    function toDtoWithStyle(e: EventEntity): EventDto {
      const event = e as EventDto;
      const x = event.x * tileSize - tileSizeHalf;
      const y = event.y * tileSize - tileSizeHalf;

      event.style = {
        width: tileSize + "px",
        height: tileSize + "px",
        left: x + "px",
        top: y + "px",
        zIndex: 99 - event.x + event.y,
      };

      return event;
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
      response.forEach((event) => events.value.unshift(toDtoWithStyle(event)));
    } else {
      lastLoadX1 = mapStore.currentMapRange.x1;
      lastLoadX2 = mapStore.currentMapRange.x2;
      lastLoadY1 = mapStore.currentMapRange.y1;
      lastLoadY2 = mapStore.currentMapRange.y2;
      events.value = (
        await EventGateway.instance.getEvents(mapStore.currentMapRange)
      ).map(toDtoWithStyle);
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
