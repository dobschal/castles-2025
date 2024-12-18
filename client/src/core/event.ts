interface IEvent<DataType> {
  on: (callback: (data: DataType) => void) => void;
  off: (callback: (data: DataType) => void) => void;
  dispatch: (data: DataType) => unknown;
}

/**
 * This function creates a custom event you can dispatch or listen to via "on".
 * Every event should be stored in a single variable and can be accessed throughout the project.
 */
export function defineEvent<DataType>(): IEvent<DataType> {
  const _listeners: Array<{
    callback: (data: DataType) => unknown;
  }> = [];

  return {
    on(callback: (data: DataType) => void): void {
      _listeners.push({ callback });
    },
    off(callback: (data: DataType) => void): void {
      const index = _listeners.findIndex((l) => l.callback === callback);

      if (index === -1) {
        return console.warn("Tried to remove non existing event listener.");
      }

      _listeners.splice(index, 1);
    },
    async dispatch(data: DataType): Promise<void> {
      const promises: Array<Promise<unknown>> = [];
      for (const listener of _listeners) {
        const response = listener.callback(data);

        if (response instanceof Promise) {
          promises.push(response);
        }
      }

      await Promise.all(promises);
    },
  };
}
