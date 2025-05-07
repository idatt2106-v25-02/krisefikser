import { createComponentWrapper } from '@/components/__tests__/test-utils'
import { describe, it, expect, vi, beforeEach } from 'vitest'
import HouseholdEmergencySupplies from '@/components/household/HouseholdEmergencySupplies.vue'

const mockPush = vi.fn()

// Mock vue-router specifically for this test
vi.mock('vue-router', () => ({
  useRouter: () => ({
    push: mockPush,
  }),
}))

describe('HouseholdEmergencySupplies', () => {
  const mockInventory = {
    food: { current: 10, target: 20, unit: 'kg' },
    water: { current: 15, target: 30, unit: 'L' },
    other: { current: 5, target: 10 },
    preparedDays: 3,
    targetDays: 7,
  }

  beforeEach(() => {
    mockPush.mockReset()
  })

  it('renders inventory data correctly', () => {
    const wrapper = createComponentWrapper(HouseholdEmergencySupplies, {
      inventory: mockInventory,
      householdId: '123',
    })

    // Check food data
    expect(wrapper.text()).toContain('10/20 kg')

    // Check water data
    expect(wrapper.text()).toContain('15/30 L')

    // Check other data
    expect(wrapper.text()).toContain('5/10')

    // Check prepared days
    expect(wrapper.text()).toContain('3/7')
  })

  it('applies correct color to days prepared based on value', () => {
    // Test with low prepared days
    const lowInventory = {
      ...mockInventory,
      preparedDays: 2,
    }

    const wrapperLow = createComponentWrapper(HouseholdEmergencySupplies, {
      inventory: lowInventory,
      householdId: '123',
    })

    // Should have red text for low days
    const textElementLow = wrapperLow.find('.text-3xl.font-bold')
    expect(textElementLow.classes()).toContain('text-red-600')

    // Test with adequate prepared days
    const goodInventory = {
      ...mockInventory,
      preparedDays: 5,
    }

    const wrapperGood = createComponentWrapper(HouseholdEmergencySupplies, {
      inventory: goodInventory,
      householdId: '123',
    })

    // Should have blue text for adequate days
    const textElementGood = wrapperGood.find('.text-3xl.font-bold')
    expect(textElementGood.classes()).toContain('text-blue-700')
  })

  it('displays details button when showDetailsButton prop is true', () => {
    const wrapper = createComponentWrapper(HouseholdEmergencySupplies, {
      inventory: mockInventory,
      householdId: '123',
      showDetailsButton: true,
    })

    // Check if details button is present
    expect(wrapper.find('button').exists()).toBe(true)
    expect(wrapper.find('button').text()).toContain('Vis detaljer')
  })

  it('navigates to inventory page when details button is clicked', async () => {
    const wrapper = createComponentWrapper(HouseholdEmergencySupplies, {
      inventory: mockInventory,
      householdId: '123',
      showDetailsButton: true,
    })

    // Click details button
    await wrapper.find('button').trigger('click')

    // Check if router.push was called with correct path
    expect(mockPush).toHaveBeenCalledWith('/husstand/123/beredskapslager')
  })
})
