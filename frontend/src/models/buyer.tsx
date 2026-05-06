export interface Buyer {
    externalReference: string;
    name: string;
    creditLimit: number;
    tenantCode: string;
    enabled: boolean;
    createdAt: string; // ISO String
    lastModified: string;
    version: number;
}