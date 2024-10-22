import { Gateway } from "@/gateways/Gateway.ts";
import { LoginResponse } from "@/types/response/LoginResponse.ts";
import { UserEntity } from "@/types/model/UserEntity.ts";

export class UserGateway extends Gateway {
  static get instance(): UserGateway {
    return new UserGateway();
  }

  async getUser(): Promise<UserEntity> {
    return await this.request<UserEntity>("GET", "/v1/users/current");
  }

  async register(username: string, password: string): Promise<void> {
    await this.request("POST", "/v1/users/register", { username, password });
  }

  async login(username: string, password: string): Promise<string> {
    const { jwt } = await this.request<LoginResponse>(
      "POST",
      "/v1/users/login",
      {
        username,
        password,
      },
    );

    return jwt;
  }
}
