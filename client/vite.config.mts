// https://vitejs.dev/config/

import { defineConfig } from "vite";
import vue from "@vitejs/plugin-vue";
import path from "path";
import * as fs from "fs";

// this writes the current version of the build into a file and saves it
fs.writeFileSync(
  "public/version.json",
  JSON.stringify({ version: process.env.npm_package_version }),
);

export default defineConfig({
  build: {
    target: "esnext",
  },
  plugins: [vue()],
  resolve: {
    alias: {
      "@": path.resolve(__dirname, "./src"),
      "vue-i18n": "vue-i18n/dist/vue-i18n.cjs.js",
    },
  },
  server: {
    open: true,
    host: "castles.games",
    origin: "http://localhost:5173",
    cors: true,
  },
});
