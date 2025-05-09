import { mount } from '@vue/test-utils'
import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import LocationTracker from '@/components/LocationTracker.vue'
import { useAuthStore } from '@/stores/auth/useAuthStore'
import type { UserResponse } from '@/api/generated/model'

// Mock the auth store
vi.mock('@/stores/auth/useAuthStore', () => ({
  useAuthStore: vi.fn(() => ({
    accessToken: null,
    refreshToken: null,
    currentUser: undefined as UserResponse | undefined,
    isAuthenticated: true,
    isAdmin: false,
    isSuperAdmin: false,
    login: vi.fn(),
    register: vi.fn(),
    registerAdmin: vi.fn(),
    logout: vi.fn(),
    refreshTokens: vi.fn(),
    updateTokens: vi.fn(),
    refetchUser: vi.fn(),
    $state: {
      accessToken: null,
      refreshToken: null,
      currentUser: undefined
    },
    $patch: vi.fn(),
    $reset: vi.fn(),
    $subscribe: vi.fn(),
    $dispose: vi.fn(),
    $onAction: vi.fn(),
    $id: 'auth',
    _customProperties: new Set()
  }))
}))

// Mock the API mutation
const mockMutateAsync = vi.fn()
vi.mock('@/api/generated/user/user', () => ({
  useUpdateCurrentUserLocation: vi.fn(() => ({
    mutateAsync: mockMutateAsync
  }))
}))

describe('LocationTracker', () => {
  const mockGeolocation = {
    getCurrentPosition: vi.fn(),
    watchPosition: vi.fn(),
    clearWatch: vi.fn()
  }

  beforeEach(() => {
    // Mock the navigator.geolocation
    vi.stubGlobal('navigator', {
      geolocation: mockGeolocation
    })

    // Mock timers
    vi.useFakeTimers()

    // Reset all mocks
    vi.clearAllMocks()
  })

  afterEach(() => {
    vi.clearAllMocks()
    vi.clearAllTimers()
    vi.useRealTimers()
    vi.restoreAllMocks()
  })

  it('starts location tracking when mounted and user is authenticated', async () => {
    const mockPosition = {
      coords: {
        latitude: 59.9139,
        longitude: 10.7522
      }
    }

    mockGeolocation.getCurrentPosition.mockImplementation((success) => {
      setTimeout(() => success(mockPosition), 100)
    })
    mockMutateAsync.mockResolvedValue({})

    mount(LocationTracker)

    // Wait for the initial location update
    await vi.advanceTimersByTimeAsync(100)

    expect(mockGeolocation.getCurrentPosition).toHaveBeenCalled()
    expect(mockMutateAsync).toHaveBeenCalledWith({
      data: {
        latitude: mockPosition.coords.latitude,
        longitude: mockPosition.coords.longitude
      }
    })
  })

  it('updates location periodically when user is authenticated', async () => {
    const mockPosition = {
      coords: {
        latitude: 59.9139,
        longitude: 10.7522
      }
    }

    mockGeolocation.getCurrentPosition.mockImplementation((success) => {
      setTimeout(() => success(mockPosition), 100)
    })
    mockMutateAsync.mockResolvedValue({})

    mount(LocationTracker)

    // Wait for initial location update
    await vi.advanceTimersByTimeAsync(100)

    // Fast-forward time and wait for the next update
    await vi.advanceTimersByTimeAsync(15000)

    expect(mockGeolocation.getCurrentPosition).toHaveBeenCalledTimes(2)
    expect(mockMutateAsync).toHaveBeenCalledTimes(2)
  })

  it('does not start tracking when user is not authenticated', () => {
    vi.mocked(useAuthStore).mockReturnValue({
      ...vi.mocked(useAuthStore)(),
      isAuthenticated: false
    })

    mount(LocationTracker)
    expect(mockGeolocation.getCurrentPosition).not.toHaveBeenCalled()
  })

  it('handles geolocation errors gracefully', async () => {
    const mockError = new Error('Geolocation error')
    const consoleSpy = vi.spyOn(console, 'error')

    mockGeolocation.getCurrentPosition.mockImplementation((success, error) => {
      setTimeout(() => error(mockError), 100)
    })

    mount(LocationTracker)

    // Wait for the initial error
    await vi.advanceTimersByTimeAsync(100)

    expect(consoleSpy).toHaveBeenCalledWith('Failed to update location:', mockError)
  })

  it('cleans up interval on unmount', async () => {
    const clearIntervalSpy = vi.spyOn(window, 'clearInterval')
    const wrapper = mount(LocationTracker)

    // Wait for the interval to be set up
    await vi.advanceTimersByTimeAsync(100)

    wrapper.unmount()
    expect(clearIntervalSpy).toHaveBeenCalled()
  })
})
