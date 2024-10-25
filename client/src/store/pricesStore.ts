import { defineStore } from "pinia";
import { PricesEntity } from "@/types/model/PricesEntity.ts";
import { Optional } from "@/types/core/Optional.ts";
import { ref } from "vue";
import { Queue } from "@/core/Queue.ts";
import { handleFatalError } from "@/core/util.ts";
import { PriceGateway } from "@/gateways/PriceGateway.ts";
import { UnitType } from "@/types/enum/UnitType.ts";
import { BuildingType } from "@/types/enum/BuildingType.ts";

export const usePricesStore = defineStore("prices", () => {
  const prices = ref<Optional<PricesEntity>>();
  const loadPricesQueue = new Queue(300, 3);

  async function loadPrices(): Promise<void> {
    await loadPricesQueue.add(async () => {
      try {
        prices.value = await new PriceGateway().getPrices();
      } catch (error) {
        handleFatalError(error);
      }
    });
  }

  function getCreationPrice(unitType: UnitType): number {
    const price = prices.value?.unitCreationPrices[unitType];

    if (!price) {
      throw new Error(`No price found for unit type ${unitType}`);
    }

    return price;
  }

  function getMovePrice(unitType: UnitType): number {
    const price = prices.value?.unitMovePrices[unitType];

    if (!price) {
      throw new Error(`No price found for unit type ${unitType}`);
    }

    return price;
  }

  function getBuildPrice(buildingType: BuildingType): number {
    const price = prices.value?.buildingPrices[buildingType];

    if (!price) {
      throw new Error(`No price found for building type ${buildingType}`);
    }

    return price;
  }

  return {
    prices,
    loadPrices,
    getCreationPrice,
    getMovePrice,
    getBuildPrice,
  };
});
