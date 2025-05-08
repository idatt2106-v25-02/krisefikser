import axios from 'axios'
import { getStoreRef } from './storeRef'

// Create a custom Axios instance with the correct base URL
export const AXIOS_INSTANCE = axios.create({
  headers: {
    'Content-Type': 'application/json',
  },
})

// Flag to prevent multiple refresh token requests
let isRefreshing = false
// Queue of failed requests to retry after token refresh
// eslint-disable-next-line @typescript-eslint/no-explicit-any
let failedQueue: { resolve: (value: unknown) => void; reject: (reason?: any) => void }[] = []

// Process the queue of failed requests
// eslint-disable-next-line @typescript-eslint/no-explicit-any
const processQueue = (error: any | null, token: string | null = null) => {
  failedQueue.forEach((promise) => {
    if (error) {
      promise.reject(error)
    } else {
      promise.resolve(token)
    }
  })
  failedQueue = []
}

// Request interceptor
AXIOS_INSTANCE.interceptors.request.use(
  (config) => {
    // Try to get token from store first, fall back to localStorage
    const store = getStoreRef()
    const token = store?.accessToken || localStorage.getItem('accessToken')

    if (token && config.headers) {
      config.headers.Authorization = `Bearer ${token}`
    }

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
    return response
  },
  async (error) => {
    const originalRequest = error.config

    console.error('Response error:', {
      url: originalRequest?.url,
      status: error.response?.status,
      message: error.message,
    })

    // Check if the error is 401 and not already retrying
    if (
      error.response?.status === 401 &&
      !originalRequest._retry &&
      originalRequest.url !== '/api/auth/refresh'
    ) {
      if (isRefreshing) {
        // If already refreshing, queue this request
        return new Promise((resolve, reject) => {
          failedQueue.push({ resolve, reject })
        })
          .then((token) => {
            originalRequest.headers.Authorization = `Bearer ${token}`
            return AXIOS_INSTANCE(originalRequest)
          })
          .catch((err) => Promise.reject(err))
      }

      originalRequest._retry = true
      isRefreshing = true

      // Try to get refreshToken from store, fall back to localStorage
      const store = getStoreRef()
      const refreshToken = store?.refreshToken || localStorage.getItem('refreshToken')

      if (!refreshToken) {
        // Handle logout - use store if available, otherwise just clear localStorage
        if (store) {
          store.logout()
        } else {
          localStorage.removeItem('accessToken')
          localStorage.removeItem('refreshToken')

          // If we're not already on the login page, redirect to it
          if (!window.location.pathname.includes('/logg-inn')) {
            window.location.href = '/logg-inn'
          }
        }

        processQueue(new Error('No refresh token available'))
        isRefreshing = false
        return Promise.reject(error)
      }

      try {
        const response = await axios.post(
          `${import.meta.env.VITE_API_URL ?? 'http://localhost:8080'}/api/auth/refresh`,
          {
            refreshToken: refreshToken,
          },
        )

        const { accessToken: newAccessToken, refreshToken: newRefreshToken } = response.data

        // Update store if available
        if (store) {
          store.accessToken = newAccessToken
          store.refreshToken = newRefreshToken
        }

        // Also update localStorage
        localStorage.setItem('accessToken', newAccessToken)
        localStorage.setItem('refreshToken', newRefreshToken)

        // Update authorization header for the original request
        originalRequest.headers.Authorization = `Bearer ${newAccessToken}`

        // Process all queued requests with the new token
        processQueue(null, newAccessToken)
        isRefreshing = false

        // Retry the original request
        return AXIOS_INSTANCE(originalRequest)
      } catch (refreshError) {
        console.error('Token refresh failed:', refreshError)

        // Handle logout - use store if available
        const store = getStoreRef()
        if (store) {
          store.logout()
        } else {
          localStorage.removeItem('accessToken')
          localStorage.removeItem('refreshToken')

          // If we're not already on the login page, redirect to it
          if (!window.location.pathname.includes('/logg-inn')) {
            window.location.href = '/logg-inn'
          }
        }

        // Process queued requests with the error
        processQueue(refreshError)
        isRefreshing = false

        return Promise.reject(refreshError)
      }
    }

    return Promise.reject(error)
  },
)

// Custom instance function used by the generated API files from Orval
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

  // Handle the pageable flattening for notifications API
  if (config.params && config.params.pageable) {
    config.params = {
      ...config.params.pageable,
    }
  }
  // Create a custom paramsSerializer to handle arrays without brackets
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  const paramsSerializer = (params: any) => {
    const parts: string[] = []

    Object.keys(params).forEach((key) => {
      const value = params[key]
      if (Array.isArray(value)) {
        // Handle arrays without brackets
        value.forEach((item) => {
          parts.push(`${encodeURIComponent(key)}=${encodeURIComponent(item)}`)
        })
      } else if (value !== null && value !== undefined) {
        parts.push(`${encodeURIComponent(key)}=${encodeURIComponent(value)}`)
      }
    })

    return parts.join('&')
  }

  try {
    // Use your AXIOS_INSTANCE with the config object
    const { data } = await AXIOS_INSTANCE({
      ...config,
      paramsSerializer,
    })
    return data
  } catch (error) {
    if (handleError) {
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
