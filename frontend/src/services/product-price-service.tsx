import api from "../config/axios-config";
import type { ProductPrice } from "../models/product-price";

export const getProductPrices = async (warehouseExternalReference: string) => {
    try {
        const accessToken = localStorage.getItem('accessToken');
        const tenantCode = localStorage.getItem('tenantCode');

        api.defaults.headers.common['Authorization'] = `Bearer ${accessToken}`;
        api.defaults.headers.common['x-tenant'] = tenantCode;
        api.defaults.headers.common['x-origin'] = "API";

        const response = await api.get(`/product-price/warehouse/${warehouseExternalReference}`);

        return response.data as ProductPrice[];
    } catch (error: unknown) {
        throw error;
    }
};