import axios, { AxiosResponse } from "axios";
import { useAuthStore } from "@/store/authStore";
import { showErrorToast } from "@/utilities/ErrorHelper.ts";

export default class Gateway {
  get baseUrl(): string {
    throw new Error("Fatal: Set baseUrl for inherited Gateway!");
  }

  static get instance(): Gateway {
    return new this();
  }

  async makePostRequest<RequestType, ResponseType>(
    path: string,
    requestPayload: RequestType,
    headers = {},
  ): Promise<AxiosResponse<ResponseType>> {
    return await axios.post(this.baseUrl + path, requestPayload, {
      headers: this._cleanUpHeaders({
        ...this._defaultHeaders,
        ...headers,
      }),
    });
  }

  async makeGetRequest<ResponseType>(
    path: string,
    queryParams: Record<string, string | number | boolean | undefined> = {},
    headers = {},
  ): Promise<AxiosResponse<ResponseType>> {
    const query = this._objectToQueryString(queryParams);

    return await axios.get(this.baseUrl + path + (query ? "?" + query : ""), {
      headers: this._cleanUpHeaders({
        ...this._defaultHeaders,
        ...headers,
      }),
    });
  }

  async makePutRequest<RequestType, ResponseType>(
    path: string,
    requestPayload: RequestType,
    headers = {},
  ): Promise<AxiosResponse<ResponseType>> {
    return await axios.put(this.baseUrl + path, requestPayload, {
      headers: this._cleanUpHeaders({
        ...this._defaultHeaders,
        ...headers,
      }),
    });
  }

  async makeDeleteRequest<ResponseType>(
    path: string,
    headers = {},
  ): Promise<AxiosResponse<ResponseType>> {
    return await axios.delete(this.baseUrl + path, {
      headers: this._cleanUpHeaders({
        ...this._defaultHeaders,
        ...headers,
      }),
    });
  }

  static handleServerError(
    error: unknown,
    message: string | undefined = undefined,
  ): void {
    console.error("Server error: ", error);
    showErrorToast(
      message ?? `An error occurred (${error}). Please try again later.`,
    );
  }

  get _defaultHeaders(): Record<string, string> {
    const authStore = useAuthStore();

    return {
      "Content-Type": "application/json;  charset=utf-8",
      ...(authStore.hasValidJWT()
        ? { Authorization: "Bearer " + authStore.token }
        : {}),
    };
  }

  _cleanUpHeaders(headers: Record<string, string>): Record<string, string> {
    return Object.fromEntries(
      Object.entries(headers).filter(([, value]) => Boolean(value)),
    );
  }

  _objectToQueryString(
    obj: Record<string, string | number | boolean | undefined>,
  ): string {
    return Object.keys(obj)
      .filter((key) => typeof obj[key] !== "undefined")
      .map((key) => key + "=" + obj[key])
      .join("&");
  }
}
