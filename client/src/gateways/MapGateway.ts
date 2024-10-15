import { Gateway } from "@/gateways/Gateway.ts";
import { TwoPointDto } from "@/types/dto/TwoPointDto.ts";
import { MapTile } from "@/types/model/MapTile.ts";

export class MapGateway extends Gateway {
  static get instance(): MapGateway {
    return new MapGateway();
  }

  async getMapTiles(data: TwoPointDto): Promise<Array<MapTile>> {
    return this.request<Array<MapTile>>(
      "GET",
      "/v1/map-tiles?" + this.objectToQueryString(data),
    );
  }
}
