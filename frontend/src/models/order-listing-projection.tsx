export default interface OrderListingProjection {
  orderId: string; 
  externalReference: string;
  buyerName: string;
  sellerName: string;
  warehouseName: string;
  status: string;
  subtotal: number; 
  discountValue: number;
  total: number;
  itemCount: number;
  origin: string;
  createdAt: string; 
}