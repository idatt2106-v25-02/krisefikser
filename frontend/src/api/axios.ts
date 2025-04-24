import axios from 'axios';

// Create a custom Axios instance
export const AXIOS_INSTANCE = axios.create({
  // Use a direct string for the baseURL if import.meta is not available
  baseURL: 'http://localhost:8080',
  headers: {
    'Content-Type': 'application/json'
  }
});

// Request interceptor
AXIOS_INSTANCE.interceptors.request.use(
  (config) => {
    // Add authorization header or other logic if needed
    const token = localStorage.getItem('token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// Response interceptor
AXIOS_INSTANCE.interceptors.response.use(
  (response) => response,
  (error) => {
    // Handle errors (like 401 unauthorized)
    if (error.response?.status === 401) {
      // Handle unauthorized access
      localStorage.removeItem('token');
      // Redirect to login or show notification
    }
    return Promise.reject(error);
  }
);

// Custom instance function used by the generated API files
export const customInstance = async <T>(
  config: Parameters<typeof AXIOS_INSTANCE>[0],
  options?: { handleError?: boolean }
): Promise<T> => {
  const { handleError = true } = options || {};
  try {
    const { data } = await AXIOS_INSTANCE(config);
    return data;
  } catch (error) {
    if (handleError) {
      // You can handle errors globally here
      console.error('API request failed:', error);
    }
    throw error;
  }
};

// Define types for error and body handling
export type ErrorType<E> = E;
export type BodyType<Body> = Body;

// Export the customInstance as default
export default customInstance;
