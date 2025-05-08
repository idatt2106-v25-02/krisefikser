import { defineStore } from 'pinia'
import { computed, ref } from 'vue'
import router from '@/router'
import {
  useLogin,
  useMe,
  useRegister,
  useRegisterAdmin,
} from '@/api/generated/authentication/authentication.ts'
import type { LoginRequest, RegisterRequest } from '@/api/generated/model'
import axios from 'axios'

export const useAuthStore = defineStore('auth', () => {
  // State
  const accessToken = ref<string | null>(localStorage.getItem('accessToken'))
  const refreshToken = ref<string | null>(localStorage.getItem('refreshToken'))

  // Computed
  const isAuthenticated = computed(() => {
    const isAuth = !!accessToken.value
    return isAuth
  })

  // Get the login mutation
  const { mutateAsync: loginMutation } = useLogin()

  // Get the register mutation
  const { mutateAsync: registerMutation } = useRegister()

  // Get the register admin mutation
  const { mutateAsync: registerAdminMutation } = useRegisterAdmin()

  // Get current user query - only enabled when we have a token
  const { data: currentUser, refetch: refetchUser } = useMe({
    query: {
      enabled: isAuthenticated.value,
      refetchOnMount: true,
      refetchOnWindowFocus: true,
    },
  })

  // Role-based authorization
  const isAdmin = computed(() => {
    return (
      currentUser.value?.roles?.includes('ADMIN') ||
      currentUser.value?.roles?.includes('SUPER_ADMIN') ||
      false
    )
  })

  const isSuperAdmin = computed(() => {
    return currentUser.value?.roles?.includes('SUPER_ADMIN') || false
  })

  // Function to update tokens in both store and localStorage
  function updateTokens(newAccessToken: string, newRefreshToken: string) {
    accessToken.value = newAccessToken
    refreshToken.value = newRefreshToken
    localStorage.setItem('accessToken', newAccessToken)
    localStorage.setItem('refreshToken', newRefreshToken)
  }

  // Actions
  async function login(credentials: LoginRequest) {
    try {
      const response = await loginMutation({ data: credentials })

      if (response.accessToken) {
        updateTokens(response.accessToken, response.refreshToken ?? '')
        await refetchUser()
      }

      return response
    } catch (error) {
      console.error('Login failed:', error)

      // Clear any partial auth data on login failure
      // especially important for 401 unauthorized errors
      accessToken.value = null
      refreshToken.value = null
      localStorage.removeItem('accessToken')
      localStorage.removeItem('refreshToken')

      throw error
    }
  }

  async function register(data: RegisterRequest) {
    try {
      const response = await registerMutation({ data })

      /*if (response.accessToken) {
        updateTokens(response.accessToken, response.refreshToken ?? '')
        await refetchUser()
      }*/

      return response
    } catch (error) {
      console.error('Registration failed:', error)
      throw error
    }
  }

  async function registerAdmin(data: RegisterRequest) {
    try {
      const response = await registerAdminMutation({ data })
      if (response.accessToken) {
        updateTokens(response.accessToken, response.refreshToken ?? '')
        await refetchUser()
      }
      return response
    } catch (error) {
      console.error('Admin registration failed:', error)
      throw error
    }
  }

  async function refreshTokens() {
    if (!refreshToken.value) {
      console.error('No refresh token available')
      logout()
      throw new Error('No refresh token available')
    }

    try {
      // Use direct axios for the refresh call
      const response = await axios.post(import.meta.env.VITE_API_URL + '/api/auth/refresh', {
        refreshToken: refreshToken.value,
      })

      const { accessToken: newAccessToken, refreshToken: newRefreshToken } = response.data
      updateTokens(newAccessToken, newRefreshToken)

      return response.data
    } catch (error) {
      console.error('Token refresh failed:', error)
      logout()
      throw error
    }
  }

  async function updatePassword(oldPassword: string, newPassword: string) {
    try {
      const response = await axios.post(
        import.meta.env.VITE_API_URL + '/api/auth/update-password',
        {
          oldPassword,
          password: newPassword,
        },
        {
          headers: {
            'Content-Type': 'application/json',
            Authorization: `Bearer ${accessToken.value}`,
          },
        },
      )
      return response.data
    } catch (error) {
      console.error('Password update failed:', error)
      throw error
    }
  }

  function logout() {
    // Clear tokens
    accessToken.value = null
    refreshToken.value = null
    localStorage.removeItem('accessToken')
    localStorage.removeItem('refreshToken')

    // Redirect to login
    router.push('/logg-inn')
  }

  // Initialize - check if we have a token and fetch user data
  if (isAuthenticated.value) {
    refetchUser()
  }

  return {
    // State
    accessToken,
    refreshToken,
    currentUser,

    // Computed
    isAuthenticated,
    isAdmin,
    isSuperAdmin,

    // Actions
    login,
    register,
    registerAdmin,
    logout,
    refreshTokens,
    updateTokens,
    updatePassword,

    // Expose for debugging
    refetchUser,
  }
})
