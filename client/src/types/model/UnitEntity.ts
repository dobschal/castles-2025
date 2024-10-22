import { UnitType } from "@/types/enum/UnitType.ts";
import { UserEntity } from "@/types/model/UserEntity.ts";

export interface UnitEntity {
  id: number;
  x: number;
  y: number;
  type: UnitType;
  user: UserEntity;
}
