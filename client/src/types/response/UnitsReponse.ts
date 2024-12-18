import { UnitEntity } from "@/types/model/UnitEntity.ts";

export interface UnitsResponse {
  units: Array<UnitEntity>;
  workerMovesPerHour: number;
  spearmanMovesPerHour: number;
  swordsmanMovesPerHour: number;
  horsemanMovesPerHour: number;
  dragonMovesPerHour: number;
  archerMovesPerHour: number;
  unitsCount: number;
  unitsLimit: number;
}
