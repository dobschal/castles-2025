import { SHOW_TOAST } from "@/events.ts";

let componentCounter = 0;

export function getComponentId(): string {
  return "" + componentCounter++;
}

export function isTouchDevice(): boolean {
  return "ontouchstart" in window || navigator.maxTouchPoints > 0;
}

export function handleFatalError(error: unknown): void {
  console.error("Error: ", error);
  SHOW_TOAST.dispatch({
    type: "danger",
    messageKey: "general.serverError",
  });
}
