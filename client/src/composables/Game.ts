import { onMounted } from "vue";

export function useGame(): void {
  onMounted(() => {
    console.info("Game started...");
  });

  // TODO: fetch the start village. if not exist, let the user decide where to start --> open action dialog
}
