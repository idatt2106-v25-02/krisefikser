// src/components/__tests__/PersonalInfo.spec.ts
import { createComponentWrapper } from '@/components/__tests__/test-utils'
import { describe, it, expect, vi } from 'vitest'
import PersonalInfo from '@/components/dashboard/PersonalInfo.vue'
import { useMe } from '@/api/generated/authentication/authentication'
import { useUpdateUser } from '@/api/generated/user/user'

// Mock the API hooks
vi.mock('@/api/generated/authentication/authentication', () => ({
  useMe: vi.fn(),
}))

vi.mock('@/api/generated/user/user', () => ({
  useUpdateUser: vi.fn(),
}))

// Mock the auth store
vi.mock('@/stores/useAuthStore', () => ({
  useAuthStore: vi.fn(() => ({
    isAuthenticated: true,
  })),
}))

function mockFn<T extends (...args: any[]) => any>(fn: T) {
  return fn as unknown as T & {
    mockReturnValue: (val: any) => void
    mockImplementation: (implementation: T) => void
  }
}

describe('PersonalInfo', () => {
  it('renders user information correctly', async () => {
    // Setup mock data
    const mockUser = {
      id: '123',
      firstName: 'John',
      lastName: 'Doe',
      email: 'john.doe@example.com',
    }

    // Setup mock API responses
    mockFn(useMe).mockReturnValue({
      data: { value: mockUser },
      refetch: vi.fn(),
    })

    mockFn(useUpdateUser).mockReturnValue({
      mutate: vi.fn(),
    })

    // Mount component
    const wrapper = createComponentWrapper(PersonalInfo)
    await wrapper.vm.$nextTick()

    // Check if user data is displayed correctly
    expect((wrapper.find('#firstName').element as HTMLInputElement).value).toBe('John')
    expect((wrapper.find('#lastName').element as HTMLInputElement).value).toBe('Doe')
    expect((wrapper.find('#email').element as HTMLInputElement).value).toBe('john.doe@example.com')
  })

  it('toggles edit mode on button click', async () => {
    // Setup mock data
    const mockUser = {
      id: '123',
      firstName: 'John',
      lastName: 'Doe',
      email: 'john.doe@example.com',
    }

    // Setup mock API responses
    mockFn(useMe).mockReturnValue({
      data: { value: mockUser },
      refetch: vi.fn(),
    })

    mockFn(useUpdateUser).mockReturnValue({
      mutate: vi.fn(),
    })

    // Mount component
    const wrapper = createComponentWrapper(PersonalInfo)
    await wrapper.vm.$nextTick()

    // Check initial state (not editing)
    expect(wrapper.find('#firstName').attributes()).toHaveProperty('disabled')

    // Click edit button
    await wrapper.find('button').trigger('click')

    // Check if inputs are now enabled
    expect(wrapper.find('#firstName').attributes('disabled')).toBeUndefined()
  })
})
