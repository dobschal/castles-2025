import { EventEntity } from "@/types/model/EventEntity.ts";

export interface EventDto extends EventEntity {
  style: Record<string, string | number>;
}
