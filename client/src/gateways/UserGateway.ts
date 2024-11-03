import { Gateway } from "@/core/Gateway.ts";
import { LoginResponse } from "@/types/response/LoginResponse.ts";
import { UserEntity } from "@/types/model/UserEntity.ts";
import { UserRankingEntity } from "@/types/model/UserRankingEntity.ts";

export class UserGateway extends Gateway {
  static get instance(): UserGateway {
    return new UserGateway();
  }

  async getUser(): Promise<UserEntity> {
    return await this.request<UserEntity>("GET", "/v1/users/current");
  }

  async updateAvatarImageId(
    userId: number,
    avatarImageId: number,
  ): Promise<void> {
    await this.request(
      "PUT",
      `/v1/users/${userId}/avatar/${avatarImageId}`,
      {},
    );
  }

  async getUserRanking(): Promise<Array<UserRankingEntity>> {
    return await this.request<Array<UserRankingEntity>>(
      "GET",
      "/v1/users/ranking",
    );
  }

  async getUserRankingById(userId: number): Promise<UserRankingEntity> {
    return await this.request<UserRankingEntity>(
      "GET",
      `/v1/users/${userId}/ranking`,
    );
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
