import { UserEntity } from "@/types/model/UserEntity.ts";
import { UnitEntity } from "@/types/model/UnitEntity.ts";
import { BuildingEntity } from "@/types/model/BuildingEntity.ts";
import { EventType } from "@/types/enum/EventType.ts";

export interface EventEntity {
  id: number;
  x: number;
  y: number;
  user1: UserEntity;
  user2?: UserEntity;
  unit?: UnitEntity;
  building?: BuildingEntity;
  createdAt: string;
  type: EventType;
}
