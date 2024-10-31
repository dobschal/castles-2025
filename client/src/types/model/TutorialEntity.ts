import { TutorialType } from "@/types/enum/TutorialType.ts";
import { TutorialStatus } from "@/types/enum/TutorialStatus.ts";
import { UserEntity } from "@/types/model/UserEntity.ts";

export interface TutorialEntity {
  id: number;
  type: TutorialType;
  status: TutorialStatus;
  user: UserEntity;
  createdAt: string;
}
