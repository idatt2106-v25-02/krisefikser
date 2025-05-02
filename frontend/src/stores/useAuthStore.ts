import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useLogin, useRegister, useMe } from '@/api/generated/authentication/authentication'
import type { LoginRequest, RegisterRequest } from '@/api/generated/model'

export const useAuthStore = defineStore('auth', () => {
  const router = useRouter()

  // State
  const accessToken = ref<string | null>(null)
  const refreshToken = ref<string | null>(null)

  // Initialize tokens from localStorage
    console.log('Initializing tokens from localStorage')
    const storedAccessToken = localStorage.getItem('accessToken')
    const storedRefreshToken = localStorage.getItem('refreshToken')
    console.log('Stored access token:', storedAccessToken)

    if (storedAccessToken) {
      accessToken.value = storedAccessToken
      refreshToken.value = storedRefreshToken
    }

  // Computed
  const isAuthenticated = computed(() => {
    const isAuth = !!accessToken.value
    console.log('isAuthenticated:', isAuth, 'token:', accessToken.value)
    return isAuth
  })

  // Get the login mutation
  const { mutateAsync: loginMutation } = useLogin()

  // Get the register mutation
  const { mutateAsync: registerMutation } = useRegister()

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
  console.log('currentUser', currentUser)
  console.log('currentUser.value', currentUser.value)
  console.log('isAdmin', isAdmin.value)

  const isSuperAdmin = computed(() => {
    return currentUser.value?.roles?.includes('SUPER_ADMIN') || false
  })
  console.log('isSuperAdmin', isSuperAdmin.value)
  // Actions
  async function login(credentials: LoginRequest) {
    try {
      const response = await loginMutation({ data: credentials })
      console.log('Login response:', response)

      if (response.accessToken) {
        // Store tokens
        accessToken.value = response.accessToken
        refreshToken.value = response.refreshToken ?? null

        // Save to localStorage
        localStorage.setItem('accessToken', response.accessToken)
        if (response.refreshToken) {
          localStorage.setItem('refreshToken', response.refreshToken)
        }

        // Refetch user data
        await refetchUser()

        // Removed automatic redirection
      }

      return response
    } catch (error) {
      console.error('Login failed:', error)
      throw error
    }
  }

  async function register(data: RegisterRequest) {
    try {
      const response = await registerMutation({ data })

      // After successful registration, log the user in automatically
      if (response.accessToken) {
        // Store tokens directly from registration response
        accessToken.value = response.accessToken
        refreshToken.value = response.refreshToken ?? null

        // Save to localStorage
        localStorage.setItem('accessToken', response.accessToken)
        if (response.refreshToken) {
          localStorage.setItem('refreshToken', response.refreshToken)
        }

        // Refetch user data
        await refetchUser()

        // Removed automatic redirection
      }

      return response
    } catch (error) {
      console.error('Registration failed:', error)
      throw error
    }
  }

  function logout() {
    console.log('Logging out')
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
    console.log('Has token on init, fetching user data')
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
    logout,

    // Expose for debugging
    refetchUser,
  }
})
