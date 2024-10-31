import { defineStore } from "pinia";
import { ref } from "vue";
import { TutorialEntity } from "@/types/model/TutorialEntity.ts";
import { Optional } from "@/types/core/Optional.ts";
import { TutorialGateway } from "@/gateways/TutorialGateway.ts";
import { handleFatalError } from "@/core/util.ts";
import { TutorialStatus } from "@/types/enum/TutorialStatus.ts";
import { ACTION } from "@/events.ts";
import TutorialAction from "@/components/partials/game/actions/TutorialAction.vue";

export const useTutorialStore = defineStore("tutorial", () => {
  const tutorial = ref<Optional<TutorialEntity>>();

  async function loadTutorial(): Promise<void> {
    try {
      tutorial.value = await TutorialGateway.instance.getNextTutorial();
    } catch (error) {
      if (error instanceof Response && error.status === 404) {
        tutorial.value = undefined;

        return;
      }

      handleFatalError(error);
    }
  }

  async function loadAndShowTutorial(): Promise<void> {
    await loadTutorial();

    if (
      tutorial.value?.status &&
      [TutorialStatus.OPEN, TutorialStatus.CAN_BE_COMPLETED].includes(
        tutorial.value?.status,
      )
    ) {
      ACTION.dispatch(TutorialAction);
    }
  }

  return {
    tutorial,
    loadAndShowTutorial,
  };
});
