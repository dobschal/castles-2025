<template>
  <Page>
    <h2>~ {{ userRanking?.username }} ~</h2>
    <div class="avatar" :style="avatarStyle"></div>
    <p @click="showPlayerOnMap" class="clickable">
      > {{ t("userProfile.showOnMap") }}
    </p>
    <template v-if="isOwnUser">
      <p @click="toggleLanguage" class="clickable">
        >
        {{
          t("general.changeLanguage", [
            i18n.global.locale.value === "en" ? "Deutsch" : "English",
          ])
        }}
      </p>
      <p class="avatar-selector">
        <span>> {{ t("userProfile.selectAvatar") }}</span>
        <img
          v-for="i in amountOfAvatars"
          :key="i"
          :src="'/avatars/avatar-' + (i - 1) + '.png'"
          alt="Avatar"
          @click="setAvatarId(i - 1)"
        />
      </p>
    </template>
  </Page>
</template>

<script setup lang="ts">
import Page from "@/components/partials/general/CPage.vue";
import { UserRankingEntity } from "@/types/model/UserRankingEntity.ts";
import { useI18n } from "vue-i18n";
import { i18n, setLocale } from "@/core/i18n.ts";
import { useAuthStore } from "@/store/authStore.ts";
import { computed, onMounted, ref } from "vue";
import { UserGateway } from "@/gateways/UserGateway.ts";
import { Optional } from "@/types/core/Optional.ts";
import { useRoute } from "vue-router";
import router from "@/core/router.ts";
import { handleFatalError } from "@/core/util.ts";

const authStore = useAuthStore();
const { t } = useI18n();
const userRanking = ref<Optional<UserRankingEntity>>();
const route = useRoute();
const amountOfAvatars = 20; // depends on the amount of avatar images in the assets folder

const userId = computed(() => {
  return Number(route.query.id);
});

const isOwnUser = computed(() => {
  return authStore.user?.id === userId.value;
});

const avatarStyle = computed<Record<string, string>>(() => {
  return {
    backgroundImage: `url(/avatars/avatar-${userRanking.value?.avatarImageId}.png`,
  };
});

onMounted(() => load());

async function load(): Promise<void> {
  try {
    userRanking.value = await UserGateway.instance.getUserRankingById(
      userId.value,
    );
  } catch (e) {
    handleFatalError(e);
  }
}

function showPlayerOnMap(): void {
  router.push(`/?x=${userRanking.value?.x}&y=${userRanking.value?.y}`);
}

function toggleLanguage(): void {
  setLocale(i18n.global.locale.value === "en" ? "de" : "en");
}

async function setAvatarId(avatarId: number): Promise<void> {
  try {
    await UserGateway.instance.updateAvatarImageId(userId.value, avatarId);
    await load();
  } catch (e) {
    handleFatalError(e);
  }
}
</script>

<style lang="scss" scoped>
.clickable {
  cursor: pointer;
}

h2 {
  text-align: center;
  margin: 0;
}

.avatar {
  width: 100px;
  height: 100px;
  background-size: cover;
  background-position: center;
  border-radius: 50%;
  margin: 0 auto 2rem auto;
  box-shadow: 0 0 10px rgba(0, 0, 0, 0.5);
}

.avatar-selector {
  display: flex;
  justify-content: center;
  align-items: center;
  flex-wrap: wrap;
  gap: 1rem;

  span {
    display: block;
    width: 100%;
  }

  img {
    width: 100px;
    height: 100px;
    border-radius: 50%;
    cursor: pointer;

    &:hover {
      box-shadow: 0 0 10px rgba(0, 0, 0, 0.5);
    }
  }
}
</style>
