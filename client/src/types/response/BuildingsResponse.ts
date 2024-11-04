import { BuildingEntity } from "@/types/model/BuildingEntity.ts";

export interface BuildingsResponse {
  buildings: Array<BuildingEntity>;
  breweryBeerProductionPerHour: number;
  breweryBeerStorage: number;
  villageLevel1BeerStorage: number;
  amountOfVillages: number;
}
