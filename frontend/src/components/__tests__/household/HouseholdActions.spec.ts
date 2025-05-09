import { mount } from '@vue/test-utils'
import { describe, it, expect, vi } from 'vitest'
import type { Component } from 'vue'
import HouseholdActions from '@/components/household/HouseholdActions.vue'

// Mock lucide-vue-next icons
vi.mock('lucide-vue-next', () => ({
  ExternalLink: {
    name: 'ExternalLink',
    template: '<div class="external-link-icon" />'
  } as Component,
  Trash: {
    name: 'Trash',
    template: '<div class="trash-icon" />'
  } as Component
}))

describe('HouseholdActions', () => {
  const mountComponent = (props = {}) => {
    return mount(HouseholdActions, {
      props
    })
  }

  it('renders the component with correct title', () => {
    const wrapper = mountComponent({ isOwner: false })
    expect(wrapper.find('h2').text()).toBe('Husstandshandlinger')
  })

  it('shows leave button only when not owner', () => {
    const nonOwnerWrapper = mountComponent({ isOwner: false })
    const ownerWrapper = mountComponent({ isOwner: true })

    expect(nonOwnerWrapper.text()).toContain('Forlat husstand')
    expect(ownerWrapper.text()).not.toContain('Forlat husstand')
  })

  it('shows delete button for all users', () => {
    const nonOwnerWrapper = mountComponent({ isOwner: false })
    const ownerWrapper = mountComponent({ isOwner: true })

    expect(nonOwnerWrapper.text()).toContain('Slett husstand')
    expect(ownerWrapper.text()).toContain('Slett husstand')
  })

  it('emits leave event when leave button is clicked', async () => {
    const wrapper = mountComponent({ isOwner: false })
    await wrapper.find('button:first-child').trigger('click')
    expect(wrapper.emitted('leave')).toBeTruthy()
  })

  it('emits delete event when delete button is clicked', async () => {
    const wrapper = mountComponent({ isOwner: false })
    await wrapper.find('.bg-red-100').trigger('click')
    expect(wrapper.emitted('delete')).toBeTruthy()
  })

  it('applies correct styling to leave button', () => {
    const wrapper = mountComponent({ isOwner: false })
    const leaveButton = wrapper.find('button:first-child')

    expect(leaveButton.classes()).toContain('bg-white')
    expect(leaveButton.classes()).toContain('border')
    expect(leaveButton.classes()).toContain('border-gray-300')
    expect(leaveButton.classes()).toContain('text-gray-700')
    expect(leaveButton.classes()).toContain('hover:bg-gray-50')
  })

  it('applies correct styling to delete button', () => {
    const wrapper = mountComponent({ isOwner: false })
    const deleteButton = wrapper.find('.bg-red-100')

    expect(deleteButton.classes()).toContain('bg-red-100')
    expect(deleteButton.classes()).toContain('text-red-600')
    expect(deleteButton.classes()).toContain('hover:bg-red-200')
  })

  it('renders icons in buttons', () => {
    const wrapper = mountComponent({ isOwner: false })

    expect(wrapper.find('.external-link-icon').exists()).toBe(true)
    expect(wrapper.find('.trash-icon').exists()).toBe(true)
  })

  it('has correct container styling', () => {
    const wrapper = mountComponent({ isOwner: false })
    const container = wrapper.find('div:first-child')

    expect(container.classes()).toContain('bg-white')
    expect(container.classes()).toContain('rounded-lg')
    expect(container.classes()).toContain('shadow-sm')
    expect(container.classes()).toContain('border')
    expect(container.classes()).toContain('border-gray-200')
    expect(container.classes()).toContain('p-6')
  })
})
