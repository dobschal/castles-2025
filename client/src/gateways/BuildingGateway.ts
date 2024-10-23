import { Gateway } from "@/core/Gateway.ts";
import { BuildingEntity } from "@/types/model/BuildingEntity.ts";
import { PointDto } from "@/types/dto/PointDto.ts";
import { TwoPointDto } from "@/types/dto/TwoPointDto.ts";

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

  async getBuildings(data: TwoPointDto): Promise<BuildingEntity[]> {
    return this.request<BuildingEntity[]>(
      "GET",
      "/v1/buildings?" + this.objectToQueryString(data),
    );
  }
}
