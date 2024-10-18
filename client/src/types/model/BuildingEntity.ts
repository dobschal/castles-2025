import { BuildingType } from "@/types/enum/BuildingType.ts";

export interface BuildingEntity {
  x: number;
  y: number;
  type: BuildingType;
  user: unknown; // TODO: Define UserEntity
}
