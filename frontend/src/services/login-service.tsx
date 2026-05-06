import api from "../config/axios-config";
import { AxiosError } from "axios";


export const login = async (username: string, password: string, tenantCode:string) => {
  try {
    const response = await api.post('/auth/login', { username, password, tenantCode });
    
    const { accessToken } = response.data;
    
    localStorage.setItem('accessToken', accessToken);  
    localStorage.setItem('tenantCode', tenantCode);  
    
    api.defaults.headers.common['Authorization'] = `Bearer ${accessToken}`;
    api.defaults.headers.common['x-tenant'] = tenantCode;
    api.defaults.headers.common['x-origin'] = "API";
    
    return response.data;
  } catch (error: unknown) {
    if (error instanceof AxiosError && error.response) {
      throw error.response.data;
    }
    throw error;
  }
};

export const logout = () => {
  localStorage.removeItem('accessToken');
  localStorage.removeItem('tenantCode');
};