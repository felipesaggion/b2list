export interface ProductPrice {
  id: string; 
  productCode: string;
  productName: string;
  warehouseId: string;
  unitPrice: number;
  listPrice: number;
  tenantCode: string;
  enabled: boolean;
  lastModified: string;
}