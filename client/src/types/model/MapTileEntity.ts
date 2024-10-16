import { MapTileType } from "@/types/enum/MapTileType.ts";

export interface MapTileEntity {
  id: number;
  x: number;
  y: number;
  type: MapTileType;
  createdAt: string;
}
