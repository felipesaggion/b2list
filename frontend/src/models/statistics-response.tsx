import type { Period } from "./period";
import type { TopBuyer } from "./top-buyer";
import type { TopProduct } from "./top-product";

export interface StatisticsResponse {
  tenant: string;
  period: Period;
  totalOrders: number;
  confirmedOrders: number | null;
  cancelledOrders: number | null;
  totalRevenue: number | null;
  averageOrderValue: number | null;
  topBuyers: TopBuyer[];
  topProducts: TopProduct[];
}