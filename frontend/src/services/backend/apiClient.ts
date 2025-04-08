import { getAuthToken } from '../../store/auth.store';

// 从环境变量获取API基础URL
const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080';

interface RequestOptions extends RequestInit {
  requiresAuth?: boolean;
}

/**
 * 发送HTTP请求的基础函数
 */
async function sendRequest<T>(
  endpoint: string,
  method: string,
  data?: unknown,
  options: RequestOptions = {}
): Promise<T> {
  const { requiresAuth = true, ...fetchOptions } = options;
  
  // 构建完整URL
  const url = `${API_BASE_URL}${endpoint}`;
  
  // 准备headers
  const headers: Record<string, string> = {
    'Content-Type': 'application/json',
    'Accept': 'application/json',
    ...(fetchOptions.headers as Record<string, string> || {}),
  };
  
  // 如果需要授权，添加JWT token
  if (requiresAuth) {
    const token = getAuthToken();
    if (token) {
      headers['Authorization'] = `Bearer ${token}`;
    }
  }
  
  // 配置请求选项
  const requestOptions: RequestInit = {
    method,
    headers,
    ...fetchOptions,
  };
  
  // 对于GET和HEAD请求不包含body
  if (data && !['GET', 'HEAD'].includes(method)) {
    requestOptions.body = JSON.stringify(data);
  }
  
  try {
    const response = await fetch(url, requestOptions);
    
    // 检查响应状态
    if (!response.ok) {
      // 尝试解析错误响应
      const errorData = await response.json().catch(() => null);
      throw new Error(
        errorData?.message || `请求失败: ${response.status} ${response.statusText}`
      );
    }
    
    // 如果是204 No Content，返回null
    if (response.status === 204) {
      return null as T;
    }
    
    // 解析JSON响应
    return await response.json();
  } catch (error) {
    if (error instanceof Error) {
      throw error;
    }
    throw new Error('发送请求时出错');
  }
}

// 导出HTTP方法
export const apiClient = {
  get: <T>(endpoint: string, options?: RequestOptions) => 
    sendRequest<T>(endpoint, 'GET', undefined, options),
    
  post: <T>(endpoint: string, data: unknown, options?: RequestOptions) => 
    sendRequest<T>(endpoint, 'POST', data, options),
    
  put: <T>(endpoint: string, data: unknown, options?: RequestOptions) => 
    sendRequest<T>(endpoint, 'PUT', data, options),
    
  patch: <T>(endpoint: string, data: unknown, options?: RequestOptions) => 
    sendRequest<T>(endpoint, 'PATCH', data, options),
    
  delete: <T>(endpoint: string, options?: RequestOptions) => 
    sendRequest<T>(endpoint, 'DELETE', undefined, options),
}; 