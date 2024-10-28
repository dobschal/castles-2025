import { useAuthStore } from "@/store/authStore.ts";

export class Gateway {
  baseUrl = import.meta.env.VITE_API_URL;

  async request<ResponseType>(
    method: "POST" | "GET" | "DELETE" | "PUT" | "PATCH",
    urlPath: string,
    data: object | undefined = undefined,
    headers = {},
  ): Promise<ResponseType> {
    const authStore = useAuthStore();

    const t1 = Date.now();

    const options: RequestInit = {
      method,
      headers: {
        "Content-Type": "application/json;charset=UTF-8",
        ...(authStore.hasToken
          ? { Authorization: `Bearer ${authStore.token}` }
          : {}),
        ...headers,
      },
      body: data ? JSON.stringify(data) : undefined,
    };

    const rawResponse = await fetch(this.baseUrl + urlPath, options);

    if (Date.now() - t1 > 500) {
      console.warn(
        "Request took too long: ",
        method,
        Date.now() - t1 + "ms",
        urlPath,
      );
    }

    if (rawResponse.status >= 400) {
      throw rawResponse;
    }

    return rawResponse.json();
  }

  objectToQueryString(obj: object): string {
    return Object.keys(obj)
      .map((key) => `${key}=${obj[key]}`)
      .join("&");
  }
}
