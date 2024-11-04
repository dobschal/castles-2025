<template>
  <div
    class="bubble"
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

const buildingsStore = useBuildingsStore();
const mapStore = useMapStore();
const props = defineProps<{
  building: BuildingEntity;
}>();

async function collectBeer(): Promise<void> {
  try {
    const amountOfBeer = buildingsStore.calculateBeerToCollect(props.building);
    const { message } = await BuildingGateway.instance.collectBeer(
      props.building.id,
      amountOfBeer,
    );
    TOAST.dispatch({ type: "success", messageKey: message });
  } catch (error) {
    handleFatalError(error);
  }
}

const bubbleStyle = computed(() => {
  return {
    width: `${mapStore.mapTileSize / 2.5}px`,
    height: `${mapStore.mapTileSize / 2.5}px`,
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
    transform: rotate(45deg) translateY(-200%) translateX(110%);
  }
  50% {
    transform: rotate(45deg) translateY(calc(-200% - 10px)) translateX(110%);
  }
  100% {
    transform: rotate(45deg) translateY(-200%) translateX(110%);
  }
}

.bubble {
  position: absolute;
  top: 0;
  left: 0;
  z-index: 1;
  border-radius: 50%;
  background: rgba(0, 0, 0, 0.8);
  transform: rotate(45deg) translateY(-200%) translateX(110%);
  display: flex;
  animation: bounce 1s infinite;
  will-change: transform;

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
