<template>
  <div class="event-tile">
    <img
      v-show="!laterEvent && event.type === EventType.UNIT_MOVED"
      src="@/assets/arrow.svg"
      alt="Arrow"
      class="move"
      :class="arrowDirection"
    />
    <img
      v-show="
        !laterEvent &&
        (event.type === EventType.LOST_UNIT ||
          event.type === EventType.BUILDING_CONQUERED)
      "
      class="fight"
      src="@/assets/fight.svg"
      alt="Fight"
    />
  </div>
</template>

<script lang="ts" setup>
import { EventEntity } from "@/types/model/EventEntity.ts";
import { EventType } from "@/types/enum/EventType.ts";
import { computed } from "vue";
import { Optional } from "@/types/core/Optional.ts";
import { useEventsStore } from "@/store/eventsStore.ts";
import { Direction } from "@/types/enum/Direction.ts";

const eventsStore = useEventsStore();
const props = defineProps<{
  event: EventEntity;
}>();

const laterEvent = computed<Optional<EventEntity>>(() => {
  if (props.event.type !== EventType.UNIT_MOVED) {
    return;
  }

  return eventsStore.events.find((event) => {
    return (
      event.id > props.event.id &&
      event.type === EventType.UNIT_MOVED &&
      event.unit?.id === props.event.unit?.id
    );
  });
});

const previousEvent = computed<Optional<EventEntity>>(() => {
  if (props.event.type !== EventType.UNIT_MOVED) {
    return;
  }

  return eventsStore.events.find((event) => {
    return (
      event.id < props.event.id &&
      (event.type === EventType.UNIT_MOVED ||
        event.type === EventType.UNIT_CREATED) &&
      event.unit?.id === props.event.unit?.id
    );
  });
});

const arrowDirection = computed<Optional<Direction>>(() => {
  if (!previousEvent.value) {
    return;
  }

  const xDiff = props.event.x - previousEvent.value.x;
  const yDiff = props.event.y - previousEvent.value.y;

  if (xDiff === 0 && yDiff === 0) {
    return;
  }

  if (xDiff === 0) {
    return yDiff > 0 ? Direction.SOUTH : Direction.NORTH;
  }

  if (yDiff === 0) {
    return xDiff > 0 ? Direction.EAST : Direction.WEST;
  }

  return xDiff > 0
    ? yDiff > 0
      ? Direction.SOUTH_EAST
      : Direction.NORTH_EAST
    : yDiff > 0
      ? Direction.SOUTH_WEST
      : Direction.NORTH_WEST;
});
</script>

<style lang="scss" scoped>
.event-tile {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 10000 !important;
  pointer-events: none;

  img {
    &.fight {
      width: 50%;
      height: 50%;
      opacity: 1;
      transform: rotate(45deg);
    }

    &.move {
      width: 75%;
      height: 75%;
      opacity: 0.66;

      &.SOUTH {
        transform: translateY(-100%) rotate(45deg);
      }

      &.SOUTH_WEST {
        transform: translateY(-110%) translateX(110%) rotate(110deg);
      }

      &.WEST {
        transform: translateX(100%) rotate(135deg);
      }

      &.NORTH_WEST {
        transform: translateY(50%) translateX(50%) rotate(-180deg);
      }

      &.NORTH {
        transform: translateY(50%) rotate(-135deg);
      }

      &.NORTH_EAST {
        transform: translateY(50%) translateX(-50%) rotate(-90deg);
      }

      &.EAST {
        transform: translateX(-50%) rotate(-45deg);
      }

      &.SOUTH_EAST {
        transform: translateX(-50%) translateY(-50%) rotate(0deg);
      }
    }
  }
}
</style>
