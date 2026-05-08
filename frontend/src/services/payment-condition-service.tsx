import api from "../config/axios-config";
import type { PaymentCondition } from "../models/payment-condition";

export const getPaymentConditions = async () => {
    try {
        const accessToken = localStorage.getItem('accessToken');
        const tenantCode = localStorage.getItem('tenantCode');

        api.defaults.headers.common['Authorization'] = `Bearer ${accessToken}`;
        api.defaults.headers.common['x-tenant'] = tenantCode;
        api.defaults.headers.common['x-origin'] = "API";

        const response = await api.get(`/payment-condition`);

        return response.data as PaymentCondition[];
    } catch (error: unknown) {
        throw error;
    }
};