<template>
  <nav>
    <h1>
      <img src="@/assets/logo_white.svg" alt="Castles" />
      Castles
    </h1>
    <button class="burger" @click="onBurgerButtonClick">
      <svg
        xmlns="http://www.w3.org/2000/svg"
        width="2rem"
        height="2rem"
        viewBox="0 0 24 24"
      >
        <path fill="white" d="M3 18h18v-2H3v2zm0-5h18v-2H3v2zm0-7v2h18V6H3z" />
      </svg>
    </button>
  </nav>
  <div v-if="dropdownMenuVisible" class="dropdown-menu">
    <div v-if="authStore.hasToken">
      <h2>Menu</h2>
      <ul>
        <!--        <li>-->
        <!--          <router-link to="/about">About</router-link>-->
        <!--        </li>-->
        <li>
          <span class="link" @click="logout">{{ t("general.logout") }}</span>
        </li>
      </ul>
    </div>
    <div>
      <CButton @click="closeDropdownMenu">{{ t("general.back") }}</CButton>
      <small>
        App {{ versionStore.clientVersion }} | Server:
        {{ versionStore.serverVersion }}
      </small>
    </div>
  </div>
  <section>
    <slot />
  </section>
</template>

<script setup lang="ts">
import { useVersionStore } from "@/store/versionStore.ts";
import { ref } from "vue";
import CButton from "@/components/partials/general/CButton.vue";
import { useI18n } from "vue-i18n";
import { useAuthStore } from "@/store/authStore.ts";
import router from "@/core/router.ts";

const versionStore = useVersionStore();
const dropdownMenuVisible = ref(false);
const { t } = useI18n();
const authStore = useAuthStore();

function onBurgerButtonClick(): void {
  dropdownMenuVisible.value = !dropdownMenuVisible.value;
}

function closeDropdownMenu(): void {
  dropdownMenuVisible.value = false;
}

function logout(): void {
  authStore.token = "";
  closeDropdownMenu();
  router.push("/login");
}
</script>

<style lang="scss" scoped>
nav {
  background: rgb(117, 59, 22);
  color: white;
  text-align: center;
  width: 100%;
  height: 4rem;
  padding: 0 1rem;
  display: flex;
  flex-direction: row;
  justify-content: space-between;
  align-items: center;

  h1 {
    font-size: 2rem;
    line-height: 4rem;
    margin: 0;

    img {
      height: 2rem;
      margin-right: 0rem;
      transform: translateY(0.35rem);
    }
  }

  button.burger {
    background: transparent;
    border: none;
    cursor: pointer;
  }
}

.dropdown-menu {
  position: fixed;
  top: 4rem;
  right: 0;
  background: antiquewhite;
  color: black;
  width: 100%;
  height: calc(100vh - 4rem);
  z-index: 101;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  padding: 4rem;

  ul {
    list-style-type: none;
    padding: 0;
    margin: 0;

    li {
      margin-bottom: 0.5rem;

      &:before {
        content: ">";
        margin-right: 0.5rem;
      }

      a:link,
      a:visited,
      .link {
        color: black;
        text-decoration: none;
        cursor: pointer;
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
}

section {
  display: flex;
  width: 100%;
  min-height: calc(100vh - 4rem);
}
</style>
