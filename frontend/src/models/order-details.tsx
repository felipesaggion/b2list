import type { Buyer } from "./buyer";
import type { OrderItem } from "./order-item";
import type { PaymentCondition } from "./payment-condition";
import type { Seller } from "./seller";
import type { Warehouse } from "./warehouse";

export interface OrderDetails {
    code: string;
    externalReference: string;
    buyer: Buyer;
    seller: Seller;
    warehouse: Warehouse;
    paymentCondition: PaymentCondition;
    status: 'PENDING' | 'COMPLETED' | 'CANCELLED'; // Use Union types para o status
    subtotal: number;
    discountValue: number;
    total: number;
    origin: string;
    tenantCode: string;
    createdAt: string;
    lastModified: string;
    version: number;
    items: OrderItem[];
}