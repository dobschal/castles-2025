import { TOAST } from "@/events.ts";

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
