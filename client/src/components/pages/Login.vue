<template>
  <Page>
    <h1>Castles <small>of Beer and Dragons</small></h1>
    <p v-html="t('registration.intro')"></p>
    <router-link class="link-to-wiki" :to="OpenWikiPageRoute.path">
      > {{ t("login.aboutTheGame") }}
    </router-link>
    <h2>
      <CText path="login.title" />
    </h2>
    <p v-if="hasError" class="error-message">
      <CText path="login.error" />
    </p>
    <form @submit.prevent="submitForm">
      <LabeledInput v-model="username" autofocus>
        <CText path="general.username" />
      </LabeledInput>
      <LabeledInput type="password" v-model="password">
        <CText path="general.password" />
      </LabeledInput>
      <CButton type="submit" :disabled="isLoading || !hasInput">
        <CText path="login.action" />
      </CButton>
      <hr />
      <CButton @click="router.push(RegistrationPageRoute.path)">
        <CText path="login.toRegistration" />
      </CButton>
    </form>
    <p>
      <small>
        <CText path="registration.cookieInfo" />
        <br />
        <br />
        <i
          >Background music is
          <a href="https://maxkomusic.com/" target="_blank"
            >"Majesty" by MaxKoMusic</a
          >
          (CC BY-SA 3.0) from
          <a href="https://www.chosic.com/free-music/all/" target="_blank"
            >Chosic</a
          >.</i
        >
      </small>
    </p>
  </Page>
</template>

<script lang="ts" setup>
import { computed, ref } from "vue";
import Page from "@/components/partials/general/CPage.vue";
import LabeledInput from "@/components/partials/general/CLabeledInput.vue";
import CText from "@/components/partials/general/CText.vue";
import CButton from "@/components/partials/general/CButton.vue";
import { UserGateway } from "@/gateways/UserGateway.ts";
import { useAuthStore } from "@/store/authStore.ts";
import router from "@/core/router.ts";
import {
  MainPageRoute,
  OpenWikiPageRoute,
  RegistrationPageRoute,
} from "@/routes.ts";
import { useI18n } from "vue-i18n";

const authStore = useAuthStore();
const username = ref("");
const password = ref("");
const hasError = ref(false);
const isLoading = ref(false);
const { t } = useI18n();

const hasInput = computed(() => username.value.length > 0);

async function submitForm(): Promise<void> {
  isLoading.value = true;
  hasError.value = false;
  try {
    authStore.token = await UserGateway.instance.login(
      username.value,
      password.value,
    );
    await router.push(MainPageRoute.path);
  } catch (error) {
    console.error("Error during registration: ", error);
    hasError.value = true;
  } finally {
    isLoading.value = false;
  }
}
</script>

<style lang="scss" scoped>
.error-message {
  color: red;
}

small {
  font-size: 0.8rem;
  line-height: 1.5;
  display: block;
  opacity: 0.5;
}

form {
  margin-bottom: 2rem;
}

.link-to-wiki {
  display: block;
  margin-bottom: 1rem;
  text-decoration: none;
  color: black;
}

a:link,
a:visited {
  color: black;
}
</style>
