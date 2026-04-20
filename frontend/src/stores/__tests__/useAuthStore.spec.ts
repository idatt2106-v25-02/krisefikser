import { beforeEach, describe, expect, it, vi } from 'vitest'
import { createPinia, setActivePinia } from 'pinia'
import axios from 'axios'
import { useAuthStore } from '../auth/useAuthStore'

const {
  mockPush,
  loginMutation,
  registerMutation,
  registerAdminMutation,
  refetchUser,
  currentUser,
} = vi.hoisted(() => ({
  mockPush: vi.fn(),
  loginMutation: vi.fn(),
  registerMutation: vi.fn(),
  registerAdminMutation: vi.fn(),
  refetchUser: vi.fn().mockResolvedValue(undefined),
  currentUser: { value: null as { roles?: string[] } | null },
}))

vi.mock('@/router', () => ({
  default: {
    push: mockPush,
  },
}))

vi.mock('@/api/generated/authentication/authentication', () => ({
  useLogin: () => ({ mutateAsync: loginMutation }),
  useRegister: () => ({ mutateAsync: registerMutation }),
  useRegisterAdmin: () => ({ mutateAsync: registerAdminMutation }),
  useMe: () => ({ data: currentUser, refetch: refetchUser }),
}))

describe('useAuthStore', () => {
  beforeEach(() => {
    localStorage.clear()
    currentUser.value = null
    setActivePinia(createPinia())
    vi.clearAllMocks()
  })

  it('updates tokens and auth state', () => {
    const store = useAuthStore()
    store.updateTokens('access-1', 'refresh-1')

    expect(store.accessToken).toBe('access-1')
    expect(store.refreshToken).toBe('refresh-1')
    expect(store.isAuthenticated).toBe(true)
    expect(localStorage.getItem('accessToken')).toBe('access-1')
    expect(localStorage.getItem('refreshToken')).toBe('refresh-1')
  })

  it('clears tokens on failed login', async () => {
    const store = useAuthStore()
    store.updateTokens('access-1', 'refresh-1')
    loginMutation.mockRejectedValueOnce(new Error('Unauthorized'))

    await expect(store.login({ email: 'x@test.no', password: 'bad' })).rejects.toThrow(
      'Unauthorized',
    )

    expect(store.accessToken).toBeNull()
    expect(store.refreshToken).toBeNull()
    expect(localStorage.getItem('accessToken')).toBeNull()
    expect(localStorage.getItem('refreshToken')).toBeNull()
  })

  it('returns admin/super-admin flags from current user roles', () => {
    currentUser.value = { roles: ['SUPER_ADMIN'] }
    const store = useAuthStore()

    expect(store.isAdmin).toBe(true)
    expect(store.isSuperAdmin).toBe(true)
  })

  it('logs out and redirects to login route', () => {
    const store = useAuthStore()
    store.updateTokens('access-1', 'refresh-1')
    store.logout()

    expect(store.accessToken).toBeNull()
    expect(store.refreshToken).toBeNull()
    expect(mockPush).toHaveBeenCalledWith('/logg-inn')
  })

  it('refreshes tokens with axios refresh endpoint', async () => {
    const store = useAuthStore()
    store.updateTokens('access-old', 'refresh-old')
    vi.spyOn(axios, 'post').mockResolvedValueOnce({
      data: { accessToken: 'access-new', refreshToken: 'refresh-new' },
    })

    await store.refreshTokens()

    expect(store.accessToken).toBe('access-new')
    expect(store.refreshToken).toBe('refresh-new')
  })
})
