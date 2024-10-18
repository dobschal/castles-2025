import { Gateway } from "@/gateways/Gateway.ts";

export class VersionGateway extends Gateway {
  static get instance(): VersionGateway {
    return new VersionGateway();
  }

  async getVersion(): Promise<string> {
    const response = await this.request<{ version: string }>(
      "GET",
      "/v1/version",
    );

    return response.version;
  }
}
