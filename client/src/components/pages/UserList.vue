<template>
  <Page>
    <h2>Players of Castles <small>of Beer and Dragons</small></h2>
    <p>{{ t("users.description") }}</p>
    <ul class="user-list">
      <li
        v-for="user in users"
        :key="user.username"
        @click="showPlayersProfile(user)"
      >
        <span class="avatar" :style="getAvatarStyle(user)"></span>
        <span class="username">
          <span v-if="authStore.user?.username === user.username">👉</span>
          {{ user.username }}
        </span>
        <span class="stats"> {{ user.points }} points </span>
      </li>
    </ul>
  </Page>
</template>

<script setup lang="ts">
import Page from "@/components/partials/general/CPage.vue";
import { onMounted, ref } from "vue";
import { UserGateway } from "@/gateways/UserGateway.ts";
import { UserRankingEntity } from "@/types/model/UserRankingEntity.ts";
import { useI18n } from "vue-i18n";
import router from "@/core/router.ts";
import { useAuthStore } from "@/store/authStore.ts";

const authStore = useAuthStore();
const users = ref<Array<UserRankingEntity>>([]);
const { t } = useI18n();

onMounted(async () => {
  users.value = (await UserGateway.instance.getUserRanking()).filter(
    (user) => user.username !== "barbarian",
  );
  users.value.sort((a, b) => b.points - a.points);
});

function showPlayersProfile(user: UserRankingEntity): void {
  router.push(`/user-profile?id=${user.id}`);
}

function getAvatarStyle(user: UserRankingEntity): Record<string, string> {
  return {
    backgroundImage: `url(/avatars/avatar-${user.avatarImageId}.png`,
  };
}
</script>

<style lang="scss" scoped>
.user-list {
  list-style-type: none;
  padding: 0;

  li {
    display: flex;
    align-items: center;
    padding: 1rem 0;
    border-bottom: 1px solid #ccc;
    cursor: pointer;

    &:hover {
      background-color: rgba(255, 255, 255, 0.33);
    }

    .avatar {
      width: 3rem;
      height: 3rem;
      border-radius: 50%;
      background-color: #ccc;
      margin-right: 1rem;
      background-size: cover;
      box-shadow: 0.1rem 0.1rem 0.1rem 0 rgba(0, 0, 0, 0.5);
      flex: 0 0 3rem;
    }

    .username {
      font-weight: bold;
    }

    .stats {
      margin-left: auto;
    }
  }
}
</style>
