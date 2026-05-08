import api from "../config/axios-config";
import type { Seller } from "../models/seller";

export const getSellers = async () => {
    try {
        const accessToken = localStorage.getItem('accessToken');
        const tenantCode = localStorage.getItem('tenantCode');

        api.defaults.headers.common['Authorization'] = `Bearer ${accessToken}`;
        api.defaults.headers.common['x-tenant'] = tenantCode;
        api.defaults.headers.common['x-origin'] = "API";

        const response = await api.get(`/seller`);

        return response.data as Seller[];
    } catch (error: unknown) {
        throw error;
    }
};