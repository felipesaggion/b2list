export interface Warehouse {
    externalReference: string;
    name: string;
    tenantCode: string;
    enabled: boolean;
    sellerId: string | null;
    createdAt: string;
}