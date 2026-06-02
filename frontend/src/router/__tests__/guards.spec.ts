import { describe, expect, it, vi } from 'vitest'
import { applyAuthGuards } from '../guards'

const createBaseRoute = () => ({
  meta: {},
  fullPath: '/admin',
  path: '/admin',
  matched: [{}],
})

const createBaseAuth = (overrides: Partial<{
  isAuthenticated: boolean
  isAdmin: boolean
  isSuperAdmin: boolean
  currentUser: unknown
}> = {}) => {
  const state = {
    isAuthenticated: true,
    isAdmin: false,
    isSuperAdmin: false,
    currentUser: { id: 'u-1' } as unknown,
    ...overrides,
  }
  return {
    isAuthenticated: () => state.isAuthenticated,
    isAdmin: () => state.isAdmin,
    isSuperAdmin: () => state.isSuperAdmin,
    currentUser: () => state.currentUser,
    refetchUser: vi.fn().mockImplementation(async () => {
      state.isAdmin = true
      state.isSuperAdmin = true
    }),
  }
}

describe('applyAuthGuards', () => {
  it('redirects unauthenticated users when auth is required', () => {
    const next = vi.fn()
    const handled = applyAuthGuards(
      { ...createBaseRoute(), meta: { requiresAuth: true }, fullPath: '/dashboard' },
      createBaseAuth({ isAuthenticated: false }),
      next,
    )

    expect(handled).toBe(true)
    expect(next).toHaveBeenCalledWith({
      name: 'login',
      query: { redirect: '/dashboard' },
    })
  })

  it('redirects authenticated users away from guest-only pages', () => {
    const next = vi.fn()
    const handled = applyAuthGuards(
      { ...createBaseRoute(), meta: { requiresGuest: true } },
      createBaseAuth(),
      next,
    )

    expect(handled).toBe(true)
    expect(next).toHaveBeenCalledWith({ name: 'dashboard' })
  })

  it('redirects non-admin users from admin routes', () => {
    const next = vi.fn()
    const handled = applyAuthGuards(
      { ...createBaseRoute(), meta: { requiresAdmin: true } },
      createBaseAuth(),
      next,
    )

    expect(handled).toBe(true)
    expect(next).toHaveBeenCalledWith({ name: 'not-found' })
  })

  it('redirects unknown admin paths to admin dashboard', () => {
    const next = vi.fn()
    const handled = applyAuthGuards(
      { ...createBaseRoute(), path: '/admin/unknown', matched: [] },
      createBaseAuth({ isAdmin: true }),
      next,
    )

    expect(handled).toBe(true)
    expect(next).toHaveBeenCalledWith({ name: 'admin-dashboard' })
  })

  it('allows admin navigation after refetch resolves roles', async () => {
    const next = vi.fn()
    const auth = createBaseAuth({ currentUser: null, isAdmin: false })

    const handled = applyAuthGuards(
      { ...createBaseRoute(), meta: { requiresAdmin: true } },
      auth,
      next,
    )

    expect(handled).toBe(true)
    expect(next).not.toHaveBeenCalled()
    await auth.refetchUser()
    expect(next).toHaveBeenCalledWith()
  })

  it('allows navigation when no guard handles route', () => {
    const next = vi.fn()
    const handled = applyAuthGuards(
      { ...createBaseRoute(), path: '/scenarioer', fullPath: '/scenarioer' },
      createBaseAuth({ isAdmin: true }),
      next,
    )

    expect(handled).toBe(false)
    expect(next).not.toHaveBeenCalled()
  })
})
