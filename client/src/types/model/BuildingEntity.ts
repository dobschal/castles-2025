import { BuildingType } from "@/types/enum/BuildingType.ts";
import { UserEntity } from "@/types/model/UserEntity.ts";

export interface BuildingEntity {
  id: number;
  x: number;
  y: number;
  type: BuildingType;
  user: UserEntity;
}
