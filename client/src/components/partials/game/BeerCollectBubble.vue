<template>
  <div class="bubble" @click="collectBeer" @mousedown.stop @touchstart.stop>
    <img src="@/assets/beer.png" alt="Beer" />
  </div>
</template>

<script lang="ts" setup>
import { BuildingEntity } from "@/types/model/BuildingEntity.ts";
import { useBuildingsStore } from "@/store/buildingsStore.ts";
import { BuildingGateway } from "@/gateways/BuildingGateway.ts";
import { handleFatalError } from "@/core/util.ts";
import { TOAST } from "@/events.ts";

const buildingsStore = useBuildingsStore();
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
</script>

<style lang="scss" scoped>
@keyframes bounce {
  0% {
    transform: rotate(45deg) translateY(0);
  }
  50% {
    transform: rotate(45deg) translateY(-10px);
  }
  100% {
    transform: rotate(45deg) translateY(0);
  }
}

.bubble {
  position: absolute;
  top: -20px;
  left: 0;
  width: 40px;
  height: 40px;
  z-index: 1;
  border-radius: 50%;
  background: rgba(0, 0, 0, 0.8);
  transform: rotate(45deg);
  display: flex;
  margin-left: calc(100% - 20px);
  margin-top: -0%;
  animation: bounce 1s infinite;

  &::after {
    content: "";
    position: absolute;
    top: 119%;
    left: 50%;
    width: 0;
    height: 0;
    border: 10px solid transparent;
    border-top-color: rgba(0, 0, 0, 0.8);
    transform: translate(-50%, -50%);
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
}
</style>
