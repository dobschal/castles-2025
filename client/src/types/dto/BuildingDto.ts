import { BuildingEntity } from "@/types/model/BuildingEntity.ts";
import { MapTileDto } from "@/types/dto/MapTileDto.ts";

export interface BuildingDto extends BuildingEntity {
  isOwnBuilding: boolean;
  mapTile?: MapTileDto;
}
