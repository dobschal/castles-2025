import { Gateway } from "@/core/Gateway.ts";
import { TwoPointDto } from "@/types/dto/TwoPointDto.ts";
import { EventEntity } from "@/types/model/EventEntity.ts";
import { Optional } from "@/types/core/Optional.ts";

export class EventGateway extends Gateway {
  static get instance(): EventGateway {
    return new EventGateway();
  }

  async getEvents(
    data: TwoPointDto,
    lastEventId: Optional<number> = undefined,
  ): Promise<Array<EventEntity>> {
    const url =
      "/v1/events?" +
      this.objectToQueryString(data) +
      (lastEventId ? `&last_event_id=${lastEventId}` : "");

    return this.request<Array<EventEntity>>("GET", url);
  }
}
