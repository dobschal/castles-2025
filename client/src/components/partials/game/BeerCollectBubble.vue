<template>
  <div
    class="bubble"
    :class="{ disabled: isDisabled }"
    :style="bubbleStyle"
    @click="collectBeer"
    @mousedown.stop
    @touchstart.stop
  >
    <img src="@/assets/beer-min.png" alt="Beer" />
    <div class="arrow" :style="arrowStyle"></div>
  </div>
</template>

<script lang="ts" setup>
import { BuildingEntity } from "@/types/model/BuildingEntity.ts";
import { useBuildingsStore } from "@/store/buildingsStore.ts";
import { BuildingGateway } from "@/gateways/BuildingGateway.ts";
import { handleFatalError } from "@/core/util.ts";
import { TOAST } from "@/events.ts";
import { useMapStore } from "@/store/mapStore.ts";
import { computed } from "vue";
import { useEventsStore } from "@/store/eventsStore.ts";

const buildingsStore = useBuildingsStore();
const mapStore = useMapStore();
const eventsStore = useEventsStore();
const props = defineProps<{
  building: BuildingEntity;
}>();

const isDisabled = computed(() => {
  return (
    buildingsStore.findFarmNextTo(
      props.building.x,
      props.building.y,
      props.building.user.id,
    ) === undefined
  );
});

async function collectBeer(): Promise<void> {
  try {
    const amountOfBeer = buildingsStore.calculateBeerToCollect(props.building);
    const { message } = await BuildingGateway.instance.collectBeer(
      props.building.id,
      amountOfBeer,
    );
    eventsStore.ownActionHappened = true;
    TOAST.dispatch({ type: "success", messageKey: message });
  } catch (error) {
    handleFatalError(error);
  }
}

const bubbleStyle = computed(() => {
  return {
    width: `${mapStore.mapTileSize / 2.5}px`,
    height: `${mapStore.mapTileSize / 2.5}px`,
    top: `-${mapStore.mapTileSize / 2.5}px`,
  };
});

const arrowStyle = computed(() => {
  return {
    borderWidth: `${mapStore.mapTileSize / 10}px`,
    bottom: `${mapStore.mapTileSize / 40}px`,
  };
});
</script>

<style lang="scss" scoped>
@keyframes bounce {
  0% {
    transform: rotate(45deg);
  }
  50% {
    transform: rotate(45deg) translateY(10px);
  }
  100% {
    transform: rotate(45deg);
  }
}

.bubble {
  position: absolute;
  top: 0;
  left: 100%;
  z-index: 1;
  border-radius: 50%;
  background: rgba(0, 0, 0, 0.8);
  transform: rotate(45deg);
  display: flex;
  animation: bounce 1s infinite;
  will-change: transform;

  &.disabled {
    opacity: 0.5;
  }

  &:hover {
    animation: none;
    cursor: pointer;
  }

  img {
    width: 60%;
    display: block;
    margin: auto;
    transform: translateX(-1px);
  }

  .arrow {
    position: absolute;
    bottom: 0;
    left: 50%;
    width: 0;
    height: 0;
    border: 10px solid transparent;
    border-top-color: rgba(0, 0, 0, 0.8);
    transform: translate(-50%, 100%);
  }
}
</style>
