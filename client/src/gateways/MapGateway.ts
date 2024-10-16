import { Gateway } from "@/gateways/Gateway.ts";
import { TwoPointDto } from "@/types/dto/TwoPointDto.ts";
import { MapTileEntity } from "@/types/model/MapTileEntity.ts";

export class MapGateway extends Gateway {
  static get instance(): MapGateway {
    return new MapGateway();
  }

  async getMapTiles(data: TwoPointDto): Promise<Array<MapTileEntity>> {
    return this.request<Array<MapTileEntity>>(
      "GET",
      "/v1/map-tiles?" + this.objectToQueryString(data),
    );
  }
}
