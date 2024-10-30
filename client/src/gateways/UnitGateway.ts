import { Gateway } from "@/core/Gateway.ts";
import { UnitEntity } from "@/types/model/UnitEntity.ts";
import { TwoPointDto } from "@/types/dto/TwoPointDto.ts";
import { UnitsResponse } from "@/types/response/UnitsReponse.ts";

export class UnitGateway extends Gateway {
  static get instance(): UnitGateway {
    return new UnitGateway();
  }

  async getUnits(data: TwoPointDto): Promise<UnitsResponse> {
    return this.request<UnitsResponse>(
      "GET",
      "/v1/units?" + this.objectToQueryString(data),
    );
  }

  async createUnit(data: Omit<UnitEntity, "id" | "user">): Promise<void> {
    return this.request<void>("POST", "/v1/units", data);
  }

  async moveUnit(x: number, y: number, unitId: number): Promise<UnitEntity> {
    return this.request<UnitEntity>("POST", `/v1/units/move`, { x, y, unitId });
  }
}
