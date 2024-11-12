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

  async createCity(data: PointDto): Promise<BuildingEntity> {
    return this.request<BuildingEntity>("POST", "/v1/buildings/create-city", {
      ...data,
      type: BuildingType.CITY,
    });
  }

  async getBuildings(data: TwoPointDto): Promise<BuildingsResponse> {
    return this.request<BuildingsResponse>(
      "GET",
      "/v1/buildings?" + this.objectToQueryString(data),
    );
  }

  async getBuildingsByUser(userId: number): Promise<Array<BuildingEntity>> {
    return this.request<Array<BuildingEntity>>(
      "GET",
      "/v1/buildings/by-user?user_id=" + userId,
    );
  }

  async destroyBuilding(x: number, y: number): Promise<void> {
    return this.request<void>("DELETE", `/v1/buildings`, {
      x,
      y,
    });
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

  async sellBeer(amountOfBeer: number): Promise<void> {
    return this.request<void>("POST", "/v1/buildings/sell-beer", {
      amountOfBeer,
    });
  }

  async upgradeBuilding(buildingId: number): Promise<void> {
    return this.request<void>("POST", "/v1/buildings/level-up", {
      buildingId,
    });
  }
}
