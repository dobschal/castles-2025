import { Optional } from "@/types/core/Optional.ts";

export class AudioPlayer {
  audioFileUrl: string;
  type: "audio/mpeg" | "audio/wav";
  volume: number = 1.0;
  loop: boolean = true;

  constructor(
    audioFileUrl: string,
    type: "audio/mpeg" | "audio/wav",
    volume: number = 1.0,
    loop: boolean = false,
  ) {
    this.audioFileUrl = audioFileUrl;
    this.type = type;
    this.volume = volume;
    this.loop = loop;

    if (!this.audioElement) {
      this.createAudioElement();
    }
  }

  play(): AudioPlayer {
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
      <audio id="${this.audioFileUrl}" ${this.loop ? "loop" : ""} volume="${this.volume}">
        <source src="${this.audioFileUrl}" type="audio/mpeg" />
      </audio>
    `,
    );
  }
}
