import { Gateway } from "@/core/Gateway.ts";
import { TutorialEntity } from "@/types/model/TutorialEntity.ts";
import { TutorialStatus } from "@/types/enum/TutorialStatus.ts";

export class TutorialGateway extends Gateway {
  static get instance(): TutorialGateway {
    return new TutorialGateway();
  }

  async getNextTutorial(): Promise<TutorialEntity> {
    return await this.request<TutorialEntity>("GET", "/v1/tutorials/next");
  }

  async completeTutorial(tutorialId: number): Promise<void> {
    await this.request<void>("POST", "/v1/tutorials", {
      tutorialId,
      status: TutorialStatus.COMPLETED,
    });
  }
}
