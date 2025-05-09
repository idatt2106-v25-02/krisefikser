import { mount } from '@vue/test-utils'
import { describe, it, expect, vi } from 'vitest'
import { createRouter, createWebHistory } from 'vue-router'
import type { Component } from 'vue'
import type { RouteRecordRaw } from 'vue-router'
import HouseholdEmergencySupplies from '@/components/household/HouseholdEmergencySupplies.vue'

// Mock the router
const routes: RouteRecordRaw[] = [
  {
    path: '/husstand/beredskapslager',
    name: 'inventory',
    component: {} as Component
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

describe('HouseholdEmergencySupplies', () => {
  const mockInventoryItems = [
    {
      name: 'Test Item',
      expiryDate: '2024-12-31',
      amount: 5,
      productType: {
        name: 'Food',
        unit: 'pcs'
      }
    }
  ]

  const mockInventory = {
    food: { current: 10000, target: 14000, unit: 'kcal' },
    water: { current: 20, target: 28, unit: 'L' },
    other: { current: 5, target: 10 },
    preparedDays: 5,
    targetDays: 7
  }

  const defaultProps = {
    inventory: mockInventory,
    inventoryItems: mockInventoryItems,
    householdId: 'test-id',
    showDetailsButton: true
  }

  const mountComponent = (props = defaultProps) => {
    return mount(HouseholdEmergencySupplies, {
      props,
      global: {
        plugins: [router]
      }
    })
  }

  it('renders summary boxes with correct data', () => {
    const wrapper = mountComponent()

    // Check food summary
    expect(wrapper.text()).toContain('Mat')
    expect(wrapper.text()).toContain('10000/14000 kcal')

    // Check water summary
    expect(wrapper.text()).toContain('Vann')
    expect(wrapper.text()).toContain('20/28 L')

    // Check other items summary
    expect(wrapper.text()).toContain('Annet')
    expect(wrapper.text()).toContain('5/10')
  })

  it('displays prepared days information', () => {
    const wrapper = mountComponent()

    expect(wrapper.text()).toContain('Dager forberedt')
    expect(wrapper.text()).toContain('5/7')
    expect(wrapper.text()).toContain('Norske myndigheter anbefaler')
  })

  it('applies correct styling to progress bar', () => {
    const wrapper = mountComponent()
    const progressBar = wrapper.find('.bg-blue-600')

    // Progress should be 5/7 ≈ 71.4%
    expect(progressBar.attributes('style')).toContain('width: 71.42857142857143%')
  })

  it('shows details button when showDetailsButton is true', () => {
    const wrapper = mountComponent()
    const button = wrapper.find('button')

    expect(button.exists()).toBe(true)
    expect(button.text()).toContain('Vis detaljer')
  })

  it('hides details button when showDetailsButton is false', () => {
    const wrapper = mountComponent({
      ...defaultProps,
      showDetailsButton: false
    })

    expect(wrapper.find('button').exists()).toBe(false)
  })

  it('navigates to inventory page when details button is clicked', async () => {
    const routerPushSpy = vi.spyOn(router, 'push')
    const wrapper = mountComponent()

    await wrapper.find('button').trigger('click')
    expect(routerPushSpy).toHaveBeenCalledWith('/husstand/beredskapslager')
  })

  it('emits open-info-dialog event when days prepared section is clicked', async () => {
    const wrapper = mountComponent()
    await wrapper.find('[role="button"]').trigger('click')
    expect(wrapper.emitted('open-info-dialog')).toBeTruthy()
  })

  it('applies warning color when prepared days is low', () => {
    const wrapper = mountComponent({
      ...defaultProps,
      inventory: {
        ...mockInventory,
        preparedDays: 2
      }
    })

    const daysCount = wrapper.find('.text-3xl')
    expect(daysCount.classes()).toContain('text-red-600')
  })

  it('applies correct styling to container', () => {
    const wrapper = mountComponent()
    const container = wrapper.find('.bg-white')

    expect(container.classes()).toContain('border')
    expect(container.classes()).toContain('border-gray-200')
    expect(container.classes()).toContain('rounded-lg')
    expect(container.classes()).toContain('p-6')
  })

  it('applies correct styling to summary grid', () => {
    const wrapper = mountComponent()
    const grid = wrapper.find('.grid')

    expect(grid.classes()).toContain('grid-cols-1')
    expect(grid.classes()).toContain('sm:grid-cols-3')
    expect(grid.classes()).toContain('gap-4')
  })

  it('applies correct styling to info section', () => {
    const wrapper = mountComponent()
    const infoSection = wrapper.find('[role="button"]')

    expect(infoSection.classes()).toContain('bg-blue-50')
    expect(infoSection.classes()).toContain('border-blue-100')
    expect(infoSection.classes()).toContain('hover:bg-blue-100')
    expect(infoSection.classes()).toContain('transition-colors')
  })
})
