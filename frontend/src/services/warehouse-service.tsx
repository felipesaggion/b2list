import api from "../config/axios-config";
import type { Warehouse } from "../models/warehouse";

export const getWarehousesBySellerExternalReference = async (externalReference: string) => {
    try {
        const accessToken = localStorage.getItem('accessToken');
        const tenantCode = localStorage.getItem('tenantCode');

        api.defaults.headers.common['Authorization'] = `Bearer ${accessToken}`;
        api.defaults.headers.common['x-tenant'] = tenantCode;
        api.defaults.headers.common['x-origin'] = "API";

        const response = await api.get(`/warehouse/seller/${externalReference}`);

        return response.data as Warehouse[];
    } catch (error: unknown) {
        throw error;
    }
};