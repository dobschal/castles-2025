<template>
  <Page>
    <h2>Units and Buildings of {{ userRanking?.username ?? "..." }}</h2>
    <p>
      {{
        t("unitsAndBuildings.description", {
          playerName: userRanking?.username ?? "...",
        })
      }}
    </p>
    <h3>
      {{ t("unitsAndBuildings.units") }}
    </h3>
    <ul>
      <li v-for="unit in units" :key="unit.id" @click="showOnMap(unit)">
        {{ t("unitType." + unit.type) }} ({{ unit.x }} / {{ unit.y }})
      </li>
    </ul>
    <h3>
      {{ t("unitsAndBuildings.buildings") }}
    </h3>
    <ul>
      <li
        v-for="building in buildings"
        :key="building.id"
        @click="showOnMap(building)"
      >
        {{ t("buildingType." + building.type) }} ({{ building.x }} /
        {{ building.y }})
      </li>
    </ul>
  </Page>
</template>

<script setup lang="ts">
import Page from "@/components/partials/general/CPage.vue";
import { onMounted, ref } from "vue";
import { useI18n } from "vue-i18n";
import { UnitGateway } from "@/gateways/UnitGateway.ts";
import { useRoute } from "vue-router";
import { UnitEntity } from "@/types/model/UnitEntity.ts";
import { BuildingGateway } from "@/gateways/BuildingGateway.ts";
import { BuildingEntity } from "@/types/model/BuildingEntity.ts";
import { PointDto } from "@/types/dto/PointDto.ts";
import router from "@/core/router.ts";
import { UserGateway } from "@/gateways/UserGateway.ts";
import { handleFatalError } from "@/core/util.ts";
import { Optional } from "@/types/core/Optional.ts";
import { UserRankingEntity } from "@/types/model/UserRankingEntity.ts";

const { t } = useI18n();
const route = useRoute();
const units = ref<Array<UnitEntity>>([]);
const buildings = ref<Array<BuildingEntity>>([]);
const userRanking = ref<Optional<UserRankingEntity>>();

onMounted(async () => {
  await loadUnits();
  await loadBuildings();
  await loadUser();
});

async function loadUser(): Promise<void> {
  try {
    userRanking.value = await UserGateway.instance.getUserRankingById(
      Number(route.query.user_id),
    );
  } catch (e) {
    handleFatalError(e);
  }
}

function showOnMap(point: PointDto): void {
  router.push(`/?x=${point.x}&y=${point.y}`);
}

async function loadUnits(): Promise<void> {
  units.value = await UnitGateway.instance.getUnitsByUser(
    Number(route.query.user_id),
  );
}

async function loadBuildings(): Promise<void> {
  buildings.value = await BuildingGateway.instance.getBuildingsByUser(
    Number(route.query.user_id),
  );
  buildings.value.sort((a, b) => a.type.localeCompare(b.type));
}
</script>

<style lang="scss" scoped>
h3 {
  margin: 0;
}

ul {
  list-style-type: none;
  padding: 0;
  margin: 0 0 2rem 0;

  li {
    display: flex;
    align-items: center;
    padding: 1rem 0;
    border-bottom: 1px solid #ccc;
    cursor: pointer;

    &:hover {
      background-color: rgba(255, 255, 255, 0.33);
    }
  }
}
</style>
