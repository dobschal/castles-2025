import { useAuthStore } from "@/store/authStore.ts";

export class Gateway {
  get baseUrl(): string {
    return import.meta.env.VITE_API_URL;
  }

  async request<ResponseType>(
    method: "POST" | "GET" | "DELETE" | "PUT" | "PATCH",
    urlPath: string,
    data: object | null,
    headers = {},
  ): Promise<ResponseType> {
    const authStore = useAuthStore();

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

    if (rawResponse.status >= 400) {
      throw rawResponse;
    }

    return rawResponse.json();
  }
}
