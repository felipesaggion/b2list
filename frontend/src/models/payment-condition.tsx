export interface PaymentCondition {
    tenantCode: string;
    code: string;
    description: string;
    minOrderValue: number;
    maxItems: number;
    allowBonusOrder: boolean;
    businessHoursOnly: boolean;
    operationalFeePercent: number;
    discountPercent: number;
    discountExtraPercent: number;
    discountCondition: string;
    freeShippingThreshold: number;
    alwaysFreeShipping: boolean;
    enabled: boolean;
}