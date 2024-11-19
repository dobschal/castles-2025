<template>
  <h2>Menu</h2>
  <ul>
    <li>
      <router-link to="/"> {{ t("general.toGame") }}</router-link>
    </li>
    <li>
      <router-link
        :to="UserProfilePageRoute.path + '?id=' + authStore.user?.id"
      >
        {{ t("userProfile.link") }}
      </router-link>
    </li>
    <li>
      <router-link :to="UserListPageRoute.path">
        {{ t("users.link") }}
      </router-link>
    </li>
    <li>
      <router-link :to="WikiPageRoute.path">
        {{ t("wiki.link") }}
      </router-link>
    </li>
    <li>
      <a
        href="https://whatsapp.com/channel/0029Vavmqsc77qVZVlSsTL0Y"
        target="_blank"
      >
        {{ t("login.whatsapp") }}
      </a>
    </li>
    <li>
      <span class="link" @click="logout">{{ t("general.logout") }}</span>
    </li>
    <li class="without">
      <small>
        <b>Version</b>: App {{ versionStore.clientVersion }} | Server:
        {{ versionStore.serverVersion }}
      </small>
    </li>
  </ul>
  <div class="footer"><img src="@/assets/logo_black.svg" alt="Logo" /></div>
</template>

<script setup lang="ts">
import router from "@/core/router.ts";
import { useI18n } from "vue-i18n";
import { useVersionStore } from "@/store/versionStore.ts";
import { useAuthStore } from "@/store/authStore.ts";
import {
  UserListPageRoute,
  UserProfilePageRoute,
  WikiPageRoute,
} from "@/routes.ts";

const authStore = useAuthStore();
const versionStore = useVersionStore();
const { t } = useI18n();
const emit = defineEmits(["close"]);

function logout(): void {
  authStore.token = "";
  emit("close");
  router.push("/login");
}
</script>

<style lang="scss" scoped>
ul {
  list-style-type: none;
  padding: 0;
  margin: 0;

  li {
    margin-bottom: 0.5rem;

    &:not(.without):before {
      content: "👉";
      margin-right: 0.5rem;
    }

    &.without {
      margin-top: 1rem;
      color: rgba(0, 0, 0, 0.5);
    }

    a:link,
    a:visited,
    .link {
      color: black;
      text-decoration: none;
      cursor: pointer;
      font-size: 1.2rem;
    }

    a:hover,
    a:active,
    .link:hover,
    .link:active {
      color: black;
      text-decoration: underline;
    }
  }
}

.footer {
  img {
    width: 24px;
  }
}
</style>
