import { Optional } from "@/types/core/Optional.ts";
import { useAuthStore } from "@/store/authStore.ts";

export class AudioPlayer {
  audioFileUrl: string;
  type: "audio/mpeg" | "audio/wav";
  volume: number = 1.0;
  loop: boolean = true;
  autoplay: boolean = false;

  constructor(
    audioFileUrl: string,
    type: "audio/mpeg" | "audio/wav",
    volume: number = 1.0,
    loop: boolean = false,
    autoplay: boolean = false,
  ) {
    this.audioFileUrl = audioFileUrl;
    this.type = type;
    this.volume = volume;
    this.loop = loop;
    this.autoplay = autoplay;

    if (!this.audioElement) {
      this.createAudioElement();
    }
  }

  play(): AudioPlayer {
    if (useAuthStore().audioPaused) {
      return this;
    }

    this.audioElement?.play();

    return this;
  }

  stop(): AudioPlayer {
    this.audioElement?.pause();

    return this;
  }

  private get audioElement(): Optional<HTMLAudioElement> {
    const element = document.getElementById(this.audioFileUrl);

    if (element instanceof HTMLAudioElement) {
      return element;
    }
  }

  private createAudioElement(): void {
    document.body.insertAdjacentHTML(
      "beforeend",
      `
      <audio id="${this.audioFileUrl}" ${this.loop ? "loop" : ""} volume="${this.volume}" ${this.autoplay ? "autoplay" : ""}>
        <source src="${this.audioFileUrl}" type="audio/mpeg" />
      </audio>
    `,
    );
  }
}
