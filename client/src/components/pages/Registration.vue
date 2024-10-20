<template>
  <Page>
    <h1>
      <CText path="registration.title" />
    </h1>
    <p v-if="hasError" class="error-message">
      <CText path="registration.error" />
    </p>
    <form @submit.prevent="submitForm">
      <LabeledInput v-model="username" autofocus>
        <CText path="general.username" />
      </LabeledInput>
      <LabeledInput type="password" v-model="password">
        <CText path="general.password" />
      </LabeledInput>
      <CButton type="submit" :disabled="isLoading || !hasInput">
        <CText path="registration.action" />
      </CButton>
      <hr />
      <CButton @click="router.push(LoginPageRoute.path)">
        <CText path="registration.toLogin" />
      </CButton>
    </form>
  </Page>
</template>

<script lang="ts" setup>
import { computed, ref } from "vue";
import Page from "@/components/partials/general/CPage.vue";
import LabeledInput from "@/components/partials/general/CLabeledInput.vue";
import CText from "@/components/partials/general/CText.vue";
import CButton from "@/components/partials/general/CButton.vue";
import { UserGateway } from "@/gateways/UserGateway.ts";
import router from "@/core/router.ts";
import { LoginPageRoute } from "@/routes.ts";

const username = ref("");
const password = ref("");
const hasError = ref(false);
const isLoading = ref(false);

const hasInput = computed(
  () => username.value.length > 0 && password.value.length > 0,
);

async function submitForm(): Promise<void> {
  isLoading.value = true;
  hasError.value = false;
  try {
    await UserGateway.instance.register(username.value, password.value);
    await router.push(LoginPageRoute.path);
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
</style>
