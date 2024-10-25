import { UnitType } from "@/types/enum/UnitType.ts";

export interface PricesEntity {
  unitCreationPrices: Record<UnitType, number>;
  unitMovePrices: Record<UnitType, number>;
}
