import { defineStore } from "pinia";
import { EventEntity } from "@/types/model/EventEntity.ts";
import { ref } from "vue";
import { EventGateway } from "@/gateways/EventGateway.ts";
import { useMapStore } from "@/store/mapStore.ts";
import { EventType } from "@/types/enum/EventType.ts";
import { Optional } from "@/types/core/Optional.ts";
import { useAuthStore } from "@/store/authStore.ts";
import { TOAST } from "@/events.ts";

export const useEventsStore = defineStore("event", () => {
  const mapStore = useMapStore();
  const events = ref<Array<EventEntity>>([]);
  const authStore = useAuthStore();

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
        showToastForNewEvent(eventEntity);
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

  function showToastForNewEvent(eventEntity: EventEntity): void {
    if (
      eventEntity.type === EventType.LOST_UNIT &&
      eventEntity.user1.id === authStore.user?.id
    ) {
      {
        TOAST.dispatch({
          type: "danger",
          messageKey: "events.lostUnit",
        });
      }
    } else if (
      eventEntity.type === EventType.LOST_UNIT &&
      eventEntity.user2?.id === authStore.user?.id
    ) {
      TOAST.dispatch({
        type: "success",
        messageKey: "events.wonFight",
      });
    } else if (
      eventEntity.type === EventType.BUILDING_CONQUERED &&
      eventEntity.user1.id === authStore.user?.id
    ) {
      TOAST.dispatch({
        type: "success",
        messageKey: "events.buildingConquered",
      });
    } else if (
      eventEntity.type === EventType.BUILDING_CONQUERED &&
      eventEntity.user1.id !== authStore.user?.id
    ) {
      TOAST.dispatch({
        type: "danger",
        messageKey: "events.buildingConquered",
      });
    } else if (
      eventEntity.type === EventType.BUILDING_DESTROYED &&
      eventEntity.user1.id === authStore.user?.id
    ) {
      TOAST.dispatch({
        type: "success",
        messageKey: "events.buildingDestroyed",
      });
    } else if (
      eventEntity.type === EventType.BUILDING_DESTROYED &&
      eventEntity.user1.id !== authStore.user?.id
    ) {
      TOAST.dispatch({
        type: "danger",
        messageKey: "events.buildingDestroyed",
      });
    }
  }

  return {
    events,
    loadEvents,
    findLatestEventByPositionAndType,
  };
});
