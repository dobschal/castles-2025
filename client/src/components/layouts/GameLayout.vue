<template>
  <nav>
    <h1>
      <img src="@/assets/logo_white.svg" alt="Castles" />
      Castles
    </h1>
    <div>
      <button class="audio" @click="toggleAudio">
        <img
          v-if="!authStore.audioPaused"
          src="@/assets/audio-on.svg"
          alt="Audio"
        />
        <img v-else src="@/assets/audio-off.svg" alt="Audio" />
      </button>
      <button class="burger" @click="onBurgerButtonClick">
        <svg
          xmlns="http://www.w3.org/2000/svg"
          width="2rem"
          height="2rem"
          viewBox="0 0 24 24"
        >
          <path
            fill="white"
            d="M3 18h18v-2H3v2zm0-5h18v-2H3v2zm0-7v2h18V6H3z"
          />
        </svg>
      </button>
    </div>
  </nav>
  <div v-if="dropdownMenuVisible" class="dropdown-menu">
    <NavigationMenu @close="closeDropdownMenu" />
  </div>
  <section>
    <slot />
  </section>
</template>

<script setup lang="ts">
import { ref } from "vue";
import router from "@/core/router.ts";
import NavigationMenu from "@/components/partials/NavigationMenu.vue";
import { AudioPlayer } from "@/core/AudioPlayer.ts";
import { useAuthStore } from "@/store/authStore.ts";

const authStore = useAuthStore();
const dropdownMenuVisible = ref(false);
const audioPlayer = new AudioPlayer(
  "/sounds/background-music.mp3",
  "audio/mpeg",
  0.2,
  true,
  !authStore.audioPaused,
);

router.afterEach(() => {
  closeDropdownMenu();
});

function onBurgerButtonClick(): void {
  dropdownMenuVisible.value = !dropdownMenuVisible.value;
}

function closeDropdownMenu(): void {
  dropdownMenuVisible.value = false;
}

function toggleAudio(): void {
  if (authStore.audioPaused) {
    authStore.audioPaused = false;
    audioPlayer.play();
  } else {
    authStore.audioPaused = true;
    audioPlayer.stop();
  }
}
</script>

<style lang="scss" scoped>
@keyframes drive-in {
  0% {
    transform: translateX(100%);
  }
}

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
  user-select: none;

  h1 {
    font-size: 2rem;
    line-height: 4rem;
    margin: 0;

    img {
      height: 2rem;
      margin-right: 0;
      transform: translateY(0.35rem);
    }
  }

  button.burger {
    background: transparent;
    border: none;
    cursor: pointer;
  }

  button.audio {
    background: transparent;
    border: none;
    cursor: pointer;

    img {
      width: 1.8rem;
      margin-right: 0.6rem;
    }
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
  padding: 2rem 3rem calc(2rem + env(safe-area-inset-bottom)) 3rem;
  animation: drive-in 0.5s ease-in-out;
  overflow-y: auto;

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
  height: calc(100vh - 4rem);
  overflow-y: auto;
}
</style>
