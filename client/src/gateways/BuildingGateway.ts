import { Gateway } from "@/core/Gateway.ts";
import { BuildingEntity } from "@/types/model/BuildingEntity.ts";
import { PointDto } from "@/types/dto/PointDto.ts";
import { TwoPointDto } from "@/types/dto/TwoPointDto.ts";
import { BuildingType } from "@/types/enum/BuildingType.ts";
import { BuildingsResponse } from "@/types/response/BuildingsResponse.ts";

export class BuildingGateway extends Gateway {
  static get instance(): BuildingGateway {
    return new BuildingGateway();
  }

  async getStartVillage(): Promise<BuildingEntity> {
    return this.request<BuildingEntity>("GET", "/v1/buildings/start-village");
  }

  async saveStartVillage(data: PointDto): Promise<void> {
    return this.request<void>("POST", "/v1/buildings/start-village", data);
  }

  async getBuildings(data: TwoPointDto): Promise<BuildingsResponse> {
    return this.request<BuildingsResponse>(
      "GET",
      "/v1/buildings?" + this.objectToQueryString(data),
    );
  }

  async createBuilding(
    data: PointDto,
    type: BuildingType,
  ): Promise<BuildingEntity> {
    return this.request<BuildingEntity>("POST", "/v1/buildings", {
      ...data,
      type,
    });
  }

  async collectBeer(
    buildingId: number,
    amountOfBeer: number,
  ): Promise<{ message: string }> {
    return this.request<{ message: string }>(
      "POST",
      `/v1/buildings/collect-beer`,
      {
        buildingId,
        amountOfBeer,
      },
    );
  }
}
