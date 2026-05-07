export interface PaymentCondition {
  code: string;
  description: string;
  maxInstallments: number;
  discountPercentage: number;
  extraDiscountPercentage: number;
  tenantCode: string;
  operationalFeePercentage: number;
  minValue: number;
  maxItems: number;
  allowOnlyBusinessHours: boolean;
  allowBonus: boolean;
  freeShippingThreshold: number;
  enabled: boolean;
}