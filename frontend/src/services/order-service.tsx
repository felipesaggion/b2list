import api from "../config/axios-config";
import type OrderFilters from "../models/order-filters";
import { formatISO } from "date-fns";
import type { StatisticsResponse } from "../models/statistics-response";

export const getOrdersPaginated = async (size: number, page: number, filters: OrderFilters) => {
    try {
        const params = new URLSearchParams();

        if (filters.status) params.append('status', filters.status === 'ALL' ? '' : filters.status);
        if (filters.buyerRef) params.append('buyerRef', filters.buyerRef);
        if (filters.startDate) params.append('startDate', formatISO(filters.startDate));
        if (filters.endDate) params.append('endDate', formatISO(filters.endDate));

        const queryString = params.toString();

        const accessToken = localStorage.getItem('accessToken');
        const tenantCode = localStorage.getItem('tenantCode');

        api.defaults.headers.common['Authorization'] = `Bearer ${accessToken}`;
        api.defaults.headers.common['x-tenant'] = tenantCode;
        api.defaults.headers.common['x-origin'] = "API";

        const response = await api.get(`/order?size=${size}&page=${page}&${queryString}`);
        return response.data;
    } catch (error: unknown) {
        throw error;
    }
};

export const getOrderDetails = async (externalReference: string) => {
    try {
        const accessToken = localStorage.getItem('accessToken');
        const tenantCode = localStorage.getItem('tenantCode');

        api.defaults.headers.common['Authorization'] = `Bearer ${accessToken}`;
        api.defaults.headers.common['x-tenant'] = tenantCode;
        api.defaults.headers.common['x-origin'] = "API";

        const response = await api.get(`/order/${externalReference}`);

        return response.data;
    } catch (error: unknown) {
        throw error;
    }
};

export const getOrderStatistics = async (startDate: Date, endDate: Date) => {
    try {
        const accessToken = localStorage.getItem('accessToken');
        const tenantCode = localStorage.getItem('tenantCode');

        api.defaults.headers.common['Authorization'] = `Bearer ${accessToken}`;
        api.defaults.headers.common['x-tenant'] = tenantCode;
        api.defaults.headers.common['x-origin'] = "API";

        const params = new URLSearchParams();

        if (startDate) params.append('startDate', formatISO(startDate));
        if (endDate) params.append('endDate', formatISO(endDate));

        const queryString = params.toString();

        const response = await api.get(`/order/statistics?${queryString}`);
        const data = response.data;
        return {
            ...data,
            confirmedOrders: data.confirmedOrders ?? 0,
            cancelledOrders: data.cancelledOrders ?? 0,
            totalRevenue: data.totalRevenue ?? 0,
            averageOrderValue: data.averageOrderValue ?? 0,
            topBuyers: data.topBuyers ?? [],
            topProducts: data.topProducts ?? []
        } as StatisticsResponse;
    } catch (error: unknown) {
        throw error;
    }
};

export const cancelOrder = async (externalReference: string) => {
    try {
        const accessToken = localStorage.getItem('accessToken');
        const tenantCode = localStorage.getItem('tenantCode');

        api.defaults.headers.common['Authorization'] = `Bearer ${accessToken}`;
        api.defaults.headers.common['x-tenant'] = tenantCode;
        api.defaults.headers.common['x-origin'] = "API";

        await api.post(`/order/${externalReference}/cancel`);
    } catch (error: unknown) {
        throw error;
    }
};
