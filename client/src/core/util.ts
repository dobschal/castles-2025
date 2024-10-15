let componentCounter = 0;

export function getComponentId(): string {
  return "" + componentCounter++;
}
