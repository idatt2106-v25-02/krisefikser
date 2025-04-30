import axios from 'axios'

// Create a custom Axios instance
export const AXIOS_INSTANCE = axios.create({
  baseURL: import.meta.env.VITE_API_URL,
  headers: {
    'Content-Type': 'application/json',
  },
})

// Request interceptor
AXIOS_INSTANCE.interceptors.request.use(
  (config) => {
    console.log('Request interceptor - checking for token')
    const token = localStorage.getItem('accessToken')
    console.log('Token from localStorage:', token ? 'exists' : 'not found')

    if (token && config.headers) {
      config.headers.Authorization = `Bearer ${token}`
      console.log('Added Authorization header')
    }

    console.log('Final request config:', {
      url: config.url,
      method: config.method,
      headers: config.headers,
    })

    return config
  },
  (error) => {
    console.error('Request interceptor error:', error)
    return Promise.reject(error)
  },
)

// Response interceptor
AXIOS_INSTANCE.interceptors.response.use(
  (response) => {
    console.log('Response received:', {
      url: response.config.url,
      status: response.status,
    })
    return response
  },
  async (error) => {
    console.error('Response error:', {
      url: error.config?.url,
      status: error.response?.status,
      message: error.message,
    })

    // Handle 401 errors
    if (error.response?.status === 401) {
      console.log('401 error - clearing tokens')
      // Clear tokens
      localStorage.removeItem('accessToken')
      localStorage.removeItem('refreshToken')

      // If we're not already on the login page, redirect to it
      if (!window.location.pathname.includes('/logg-inn')) {
        console.log('Redirecting to login page')
        window.location.href = '/logg-inn'
      }
    }
    return Promise.reject(error)
  },
)

// Custom instance function used by the generated API files
export const customInstance = async <T>(
  config: {
    url: string
    method: string
    signal?: AbortSignal
    headers?: Record<string, string>
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    data?: any
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    [key: string]: any
  },
  options?: { handleError?: boolean },
): Promise<T> => {
  const { handleError = true } = options || {}

  try {
    // Use your AXIOS_INSTANCE with the config object
    const { data } = await AXIOS_INSTANCE(config)
    return data
  } catch (error) {
    if (handleError) {
      // Your existing error handling
      console.error('API request failed:', error)
    }
    throw error
  }
}

// Define types for error and body handling
export type ErrorType<E> = E
export type BodyType<Body> = Body

// Export the customInstance as default
export default customInstance
