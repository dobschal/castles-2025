import { BuildingEntity } from "@/types/model/BuildingEntity.ts";

export interface BuildingDto extends BuildingEntity {
  isOwnBuilding: boolean;
}
