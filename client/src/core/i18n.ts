import { createI18n } from "vue-i18n";
import { de } from "@/i18n/de";
import { en } from "@/i18n/en";

const localeFromStorage = window.localStorage.getItem("locale");

export let currentLocale =
  localeFromStorage ?? window.navigator.language.substring(0, 2).toLowerCase();

export const i18n = createI18n({
  warnHtmlMessage: false, // https://stackoverflow.com/questions/69795845/vue-3-eslint-warning-intlify-detected-html
  legacy: false,
  locale: currentLocale,
  fallbackLocale: "en",
  messages: {
    de,
    en,
  },
});

export function setLocale(locale: "de" | "en"): void {
  window.localStorage.setItem("locale", locale);
  currentLocale = locale;
  i18n.global.locale.value = locale;
  document.documentElement.lang = locale;
}
