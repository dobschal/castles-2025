let componentCounter = 0;

export function getComponentId(): string {
  return "" + componentCounter++;
}

export function isTouchDevice(): boolean {
  return "ontouchstart" in window || navigator.maxTouchPoints > 0;
}
