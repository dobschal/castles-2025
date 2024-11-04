import { defineStore } from "pinia";
import { handleFatalError, NOW, parseServerDateString } from "@/core/util.ts";
import { UnitGateway } from "@/gateways/UnitGateway.ts";
import { ref } from "vue";
import { UnitEntity } from "@/types/model/UnitEntity.ts";
import { useMapStore } from "@/store/mapStore.ts";
import { Optional } from "@/types/core/Optional.ts";
import { Queue } from "@/core/Queue.ts";
import { EventType } from "@/types/enum/EventType.ts";
import { UnitType } from "@/types/enum/UnitType.ts";
import { useEventsStore } from "@/store/eventsStore.ts";

export const useUnitsStore = defineStore("units", () => {
  const units = ref<Array<UnitEntity>>([]);
  const mapStore = useMapStore();
  const eventsStore = useEventsStore();
  const activeUnit = ref<Optional<UnitEntity>>();
  const activeMoveUnit = ref<Optional<UnitEntity>>();
  const loadUnitsQueue = new Queue(500, 3);
  const workerMovesPerHour = ref(-1);
  const spearmanMovesPerHour = ref(-1);
  const swordsmanMovesPerHour = ref(-1);
  const horsemanMovesPerHour = ref(-1);

  async function loadUnits(): Promise<void> {
    await loadUnitsQueue.add(async () => {
      try {
        const response = await UnitGateway.instance.getUnits(
          mapStore.currentMapRange,
        );
        units.value = response.units;
        workerMovesPerHour.value = response.workerMovesPerHour;
        spearmanMovesPerHour.value = response.spearmanMovesPerHour;
        swordsmanMovesPerHour.value = response.swordsmanMovesPerHour;
        horsemanMovesPerHour.value = response.horsemanMovesPerHour;
      } catch (e) {
        handleFatalError(e);
      }
    });
  }

  function movesLastHour(unit: UnitEntity): number {
    return eventsStore.events.filter(
      (event) =>
        event.unit?.id === unit.id &&
        event.type === EventType.UNIT_MOVED &&
        Date.now() - parseServerDateString(event.createdAt).getTime() < 3600000,
    ).length;
  }

  function movesPerHourLimit(unit: UnitEntity): number {
    switch (unit.type) {
      case UnitType.WORKER:
        return workerMovesPerHour.value;
      case UnitType.SWORDSMAN:
        return swordsmanMovesPerHour.value;
      case UnitType.SPEARMAN:
        return spearmanMovesPerHour.value;
      case UnitType.HORSEMAN:
        return horsemanMovesPerHour.value;
      default:
        return 0;
    }
  }

  function nextMoveIn(unit: UnitEntity): string {
    const latestMoves = eventsStore.events.filter(
      (event) =>
        event.unit?.id === unit.id &&
        event.type === EventType.UNIT_MOVED &&
        Date.now() - parseServerDateString(event.createdAt).getTime() < 3600000,
    );
    const movesLimit = movesPerHourLimit(unit);
    const movesRemaining = movesLimit - latestMoves.length;

    if (movesRemaining > 0) return "";

    const moveTime = parseServerDateString(
      latestMoves[latestMoves.length - 1].createdAt,
    );
    const diffInSeconds = Math.floor(
      (NOW.value - moveTime.getTime() - 3600000) / -1000,
    );
    const actualSeconds = diffInSeconds % 60;
    const actualMinutes = Math.floor(diffInSeconds / 60) % 60;

    return `${actualMinutes}m ${actualSeconds}sec`;
  }

  return {
    nextMoveIn,
    movesPerHourLimit,
    movesLastHour,
    loadUnits,
    units,
    activeUnit,
    activeMoveUnit,
    workerMovesPerHour,
    spearmanMovesPerHour,
    swordsmanMovesPerHour,
    horsemanMovesPerHour,
  };
});
