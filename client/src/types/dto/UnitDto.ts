import { UnitEntity } from "@/types/model/UnitEntity.ts";
import { MapTileDto } from "@/types/dto/MapTileDto.ts";

export interface UnitDto extends UnitEntity {
  mapTile?: MapTileDto;
  isOwnUnit: boolean;
}
