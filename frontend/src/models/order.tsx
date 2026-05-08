import type { OrderItem } from "./order-item";

export interface Order {
  externalReference: string;
  buyerReference: string;
  sellerReference: string;
  warehouseReference: string;
  paymentConditionCode: string;
  items: OrderItem[];
}