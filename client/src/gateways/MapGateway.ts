import { Gateway } from "@/core/Gateway.ts";
import { TwoPointDto } from "@/types/dto/TwoPointDto.ts";
import { MapTileEntity } from "@/types/model/MapTileEntity.ts";
import { MapTileDto } from "@/types/dto/MapTileDto.ts";
import { MapTileState } from "@/types/enum/MapTileState.ts";

export class MapGateway extends Gateway {
  static get instance(): MapGateway {
    return new MapGateway();
  }

  async getMapTiles(data: TwoPointDto): Promise<Array<MapTileDto>> {
    const response = await this.request<Array<MapTileEntity>>(
      "GET",
      "/v1/map-tiles?" + this.objectToQueryString(data),
    );

    return response.map((mapTile) => ({
      ...mapTile,
      state: MapTileState.DEFAULT,
      renderFrame: 0,
    }));
  }
}
