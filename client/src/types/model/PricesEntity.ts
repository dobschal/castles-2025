import { UnitType } from "@/types/enum/UnitType.ts";
import { BuildingType } from "@/types/enum/BuildingType.ts";

export interface PricesEntity {
  unitCreationPrices: Record<UnitType, number>;
  unitMovePrices: Record<UnitType, number>;
  buildingPrices: Record<BuildingType, number>;
  buildingLevelUpPrices: Record<BuildingType, number>; // only from level 1 to 2 currently
  sellBeerPrice: number;
}
