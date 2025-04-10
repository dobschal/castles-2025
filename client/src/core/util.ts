import { TOAST } from "@/events.ts";
import { ref } from "vue";
import { Optional } from "@/types/core/Optional.ts";
import { PointDto } from "@/types/dto/PointDto.ts";

let componentCounter = 0;

export function getComponentId(): string {
  return "" + componentCounter++;
}

export function isTouchDevice(): boolean {
  return "ontouchstart" in window || navigator.maxTouchPoints > 0;
}

// Handle errors that should not occurr... we should always try to disable UI elements
// that could cause these errors, but just in case, we should handle them.
export function handleFatalError(error: unknown): void {
  if (error instanceof Response && error.status >= 400 && error.status < 500) {
    error.json().then((json) => {
      TOAST.dispatch({
        type: "danger",
        messageKey: json.message,
      });
    });
  } else {
    console.error("Error: ", error);
    TOAST.dispatch({
      type: "danger",
      messageKey: "general.serverError",
    });
  }
}

export function TODO(...something: unknown[]): void {
  console.warn("TODO: ", ...something);
}

export function delay(duration: number): Promise<void> {
  return new Promise((resolve) => {
    setTimeout(resolve, duration);
  });
}

export const NOW = ref(Date.now());

setInterval(() => {
  NOW.value = Date.now();
}, 1000);

export function parseServerDateString(dateString: Optional<string>): Date {
  if (!dateString) return new Date(0);
  const isDevMode = import.meta.env.VITE_DEV_MODE === "true";
  const timestamp = Date.parse(dateString + (isDevMode ? "" : "Z"));

  return new Date(timestamp);
}

export function ensure<Type>(value: Type | null | undefined): Type {
  if (typeof value === "undefined" || value === null) {
    throw new Error("Fatal: Expected value to be set, but got: " + value);
  }

  return value;
}

export function atSamePosition(obj1: PointDto): (obj2: PointDto) => boolean {
  return (obj2: PointDto) => {
    return obj2.x === obj1.x && obj2.y === obj1.y;
  };
}

export function loadImage(source: string): HTMLImageElement {
  const image = new Image();
  image.src = source;

  return image;
}
