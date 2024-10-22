import { Gateway } from "@/gateways/Gateway.ts";
import { UnitEntity } from "@/types/model/UnitEntity.ts";
import { TwoPointDto } from "@/types/dto/TwoPointDto.ts";

export class UnitGateway extends Gateway {
  static get instance(): UnitGateway {
    return new UnitGateway();
  }

  async getUnits(data: TwoPointDto): Promise<UnitEntity[]> {
    return this.request<UnitEntity[]>(
      "GET",
      "/v1/units?" + this.objectToQueryString(data),
    );
  }

  async createUnit(data: Omit<UnitEntity, "id" | "user">): Promise<void> {
    return this.request<void>("POST", "/v1/units", data);
  }
}
