import { MapTileEntity } from "@/types/model/MapTileEntity.ts";
import { MapTileState } from "@/types/enum/MapTileState.ts";

export interface MapTileDto extends MapTileEntity {
  state: MapTileState;
}
