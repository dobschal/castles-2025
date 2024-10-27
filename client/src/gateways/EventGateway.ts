import { Gateway } from "@/core/Gateway.ts";
import { TwoPointDto } from "@/types/dto/TwoPointDto.ts";
import { EventEntity } from "@/types/model/EventEntity.ts";

export class EventGateway extends Gateway {
  static get instance(): EventGateway {
    return new EventGateway();
  }

  async getEvents(data: TwoPointDto): Promise<Array<EventEntity>> {
    return this.request<Array<EventEntity>>(
      "GET",
      "/v1/events?" + this.objectToQueryString(data),
    );
  }
}
