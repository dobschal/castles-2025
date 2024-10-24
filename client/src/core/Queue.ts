export class Queue {
  private queue: Array<() => Promise<unknown>>;
  private isProcessing: boolean;
  private readonly delay: number;
  private readonly maxEntries: number;

  constructor(delay: number, maxEntries: number) {
    this.queue = [];
    this.isProcessing = false;
    this.delay = delay;
    this.maxEntries = maxEntries;
  }

  public async add(fn: () => Promise<unknown>): Promise<void> {
    if (this.queue.length >= this.maxEntries) {
      return;
    }

    this.queue.push(fn);
    await this.processQueue();
  }

  private async processQueue(): Promise<void> {
    if (this.isProcessing || this.queue.length === 0) {
      return;
    }

    this.isProcessing = true;

    const fn = this.queue.shift();

    const t1 = Date.now();

    if (typeof fn === "function") await fn();

    const delay = this.delay - (Date.now() - t1);
    setTimeout(
      () => {
        this.isProcessing = false;
        this.processQueue();
      },
      delay > 0 ? delay : 0,
    );
  }
}
