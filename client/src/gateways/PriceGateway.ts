import { Gateway } from "@/core/Gateway.ts";
import { PricesEntity } from "@/types/model/PricesEntity.ts";

export class PriceGateway extends Gateway {
  async getPrices(): Promise<PricesEntity> {
    return this.request<PricesEntity>("GET", "/v1/prices");
  }
}
