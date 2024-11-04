// With the help of: https://stackoverflow.com/questions/10343913/how-to-create-a-web-worker-from-a-string

export class Thread<ParamType, ReturnType> {
  worker: Worker;

  constructor(
    fn: (param: ParamType) => Promise<ReturnType>,
    globals: Record<string, unknown>,
  ) {
    const fnString = fn.toString();
    const fnName = fnString?.match(/([a-zA-Z_{1}][a-zA-Z0-9_]+)(?=\()/g)?.[0];

    if (!fnName) {
      throw new Error("Function name not found");
    }

    const workerScript = `
const global = ${JSON.stringify(globals)};
  
${fn};

self.onmessage = async function(e){
  try {
    const result = await ${fnName}(e.data);
    postMessage(result);
  } catch (error) {
    if(error instanceof Response){
      postMessage({
        status: error.status,
        error: await error.json()
      });
    } else {
      postMessage({
        error: {
          message: error.message,
        }
      });
    }
  }
}
    `
      .trimStart()
      .trimStart();
    const blob = new Blob([workerScript], { type: "application/javascript" });
    this.worker = new Worker(window.URL.createObjectURL(blob));
  }

  run(param: ParamType): Promise<ReturnType> {
    return new Promise((resolve, reject) => {
      this.worker.onmessage = function (e) {
        if (e.data.error) {
          return reject(e.data.error);
        }

        resolve(e.data as ReturnType);
      };

      this.worker.postMessage(param);
    });
  }
}
