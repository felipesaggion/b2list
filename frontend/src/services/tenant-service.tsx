import api from "../config/axios-config";
import { AxiosError } from "axios";
import type Tenant from "../models/tenant";


export const getTenants = async () => {
  try {
    const response = await api.get('/tenant');    
    return response.data as Tenant[];
  } catch (error: unknown) {
    if (error instanceof AxiosError && error.response) {
      throw error.response.data;
    }
    throw error;
  }
};