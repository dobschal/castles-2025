<template>
  <Page>
    <h1>Castles <small>of Beer and Dragons</small></h1>
    <p>
      {{ t("wiki.description") }}
    </p>
    <h2>{{ t("wiki.title1") }}</h2>
    <p>
      <img src="@/assets/wiki/map.png" alt="Map" />
      {{ t("wiki.paragraph1") }}
    </p>
    <h2>{{ t("wiki.title2") }}</h2>
    <p>
      <img src="@/assets/wiki/castle.png" alt="Castle" />
      {{ t("wiki.paragraph2") }}
    </p>
    <h2>{{ t("wiki.title3") }}</h2>
    <p>
      <img src="@/assets/wiki/unit.png" alt="Unit" />
      {{ t("wiki.paragraph3") }}
    </p>
    <h2>{{ t("wiki.title4") }}</h2>
    <p>
      <img src="@/assets/wiki/barbar.png" alt="Barbar" />
      {{ t("wiki.paragraph4") }}
    </p>
    <h2>{{ t("wiki.title5") }}</h2>
    <p>
      <img src="@/assets/wiki/dragon.png" alt="Dragon" />
      {{ t("wiki.paragraph5") }}
    </p>
  </Page>
</template>

<script setup lang="ts">
import Page from "@/components/partials/general/CPage.vue";
import { useI18n } from "vue-i18n";

const { t } = useI18n();
</script>

<style lang="scss" scoped>
p {
  clear: both;
  text-align: justify;

  img {
    width: 100%;
    display: block;
    box-shadow: 0.5rem 0.5rem 0.1rem 0 rgba(0, 0, 0, 0.3);
    border: solid 3px rgb(117, 59, 22);
    margin: 0 0 1rem 0;

    @media screen and (min-width: 640px) {
      float: right;
      max-width: 16rem;
      margin: 0 0 1rem 1rem;
    }
  }
}
</style>
