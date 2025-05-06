import { defineStore } from 'pinia'
import { ref, computed, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useLogin, useRegister, useMe } from '@/api/generated/authentication/authentication.ts'
import type { LoginRequest, RegisterRequest } from '@/api/generated/model'
import axios from 'axios'

export const useAuthStore = defineStore('auth', () => {
  const router = useRouter()

  // State
  const accessToken = ref<string | null>(localStorage.getItem('accessToken'))
  const refreshToken = ref<string | null>(localStorage.getItem('refreshToken'))

  // Computed
  const isAuthenticated = computed(() => {
    const isAuth = !!accessToken.value
    return isAuth
  })

  const { data: currentUser, refetch: refetchUser } = useMe({
    query: {
      enabled: isAuthenticated.value,
      refetchOnMount: true,
      refetchOnWindowFocus: true,
    },
  })

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

  function updateTokens(newAccessToken: string, newRefreshToken: string) {
    accessToken.value = newAccessToken
    refreshToken.value = newRefreshToken
    localStorage.setItem('accessToken', newAccessToken)
    localStorage.setItem('refreshToken', newRefreshToken)
  }

  // Get the login mutation
  const { mutateAsync: loginMutation } = useLogin()

  // Get the register mutation
  const { mutateAsync: registerMutation } = useRegister()

  // Actions
  async function login(credentials: LoginRequest) {
    try {
      const response = await loginMutation({ data: credentials })
      if (response.accessToken) {
        updateTokens(response.accessToken, response.refreshToken ?? '')
      }
      return response
    } catch (error) {
      console.error('Login failed:', error)
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
      if (response.accessToken) {
        updateTokens(response.accessToken, response.refreshToken ?? '')
      }
      return response
    } catch (error) {
      console.error('Registration failed:', error)
      throw error
    }
  }

  async function refreshTokens() {
    if (!refreshToken.value) {
      console.error('No refresh token available')
      await logout()
      throw new Error('No refresh token available')
    }
    try {
      const response = await axios.post(
        `${import.meta.env.VITE_API_BASE_URL}/auth/refresh`,
        { refreshToken: refreshToken.value },
      )
      const { accessToken: newAccessToken, refreshToken: newRefreshToken } = response.data
      updateTokens(newAccessToken, newRefreshToken)
      return response.data
    } catch (error) {
      console.error('Token refresh failed:', error)
      await logout()
      throw error
    }
  }

  async function logout() {
    accessToken.value = null
    refreshToken.value = null
    currentUser.value = undefined
    localStorage.removeItem('accessToken')
    localStorage.removeItem('refreshToken')
    await router.push('/logg-inn')
  }

  // Watch isAuthenticated to manage user fetching
  watch(isAuthenticated, async (newIsAuthenticated, oldIsAuthenticated) => {
    if (newIsAuthenticated) {
      try {
        await refetchUser();
      } catch (error) {
        console.error("Error refetching user on auth change:", error);
      }
    } else if (oldIsAuthenticated && !newIsAuthenticated) {
      currentUser.value = undefined
    }
  }, { immediate: true })

  return {
    accessToken,
    refreshToken,
    currentUser,
    isAuthenticated,
    isAdmin,
    isSuperAdmin,
    login,
    register,
    logout,
    refreshTokens,
    updateTokens,
    refetchUser,
  }
})
