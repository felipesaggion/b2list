import api from "../config/axios-config";
import type { Buyer } from "../models/buyer";

export const getBuyers = async () => {
    try {
        const accessToken = localStorage.getItem('accessToken');
        const tenantCode = localStorage.getItem('tenantCode');

        api.defaults.headers.common['Authorization'] = `Bearer ${accessToken}`;
        api.defaults.headers.common['x-tenant'] = tenantCode;
        api.defaults.headers.common['x-origin'] = "API";

        const response = await api.get(`/buyer`);

        return response.data as Buyer[];
    } catch (error: unknown) {
        throw error;
    }
};