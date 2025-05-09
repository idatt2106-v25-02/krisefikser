import { mount } from '@vue/test-utils'
import { describe, it, expect } from 'vitest'
import HouseholdHeader from '@/components/household/HouseholdHeader.vue'
import type { HouseholdResponse } from '@/api/generated/model/householdResponse'
import { createRouter, createWebHistory } from 'vue-router'

describe('HouseholdHeader', () => {
  const mockHousehold: HouseholdResponse = {
    id: '1',
    name: 'Test Household',
    address: '123 Test St',
    latitude: 59.9139,
    longitude: 10.7522,
    postalCode: '0123',
    city: 'Oslo',
    owner: {
      id: '1',
      email: 'test@example.com',
      firstName: 'Test',
      lastName: 'User',
      notifications: true,
      emailUpdates: true,
      locationSharing: true,
      roles: ['USER']
    },
    members: [],
    guests: [],
    createdAt: new Date().toISOString(),
    active: true
  }

  const defaultProps = {
    household: mockHousehold,
    allHouseholdsCount: 2
  }

  const router = createRouter({
    history: createWebHistory(),
    routes: [
      {
        path: '/husstand/refleksjoner',
        name: 'reflections',
        component: { template: '<div>Reflections</div>' }
      }
    ]
  })

  const mountComponent = (props = defaultProps) => {
    return mount(HouseholdHeader, {
      props,
      global: {
        plugins: [router],
        stubs: {
          Button: {
            template: '<button :class="variant" :disabled="disabled" @click="$emit(\'click\')"><slot /></button>',
            props: ['variant', 'disabled']
          },
          MapPin: true,
          Edit: true,
          RefreshCw: true,
          BookText: true
        }
      }
    })
  }

  it('renders household name and address', () => {
    const wrapper = mountComponent()
    expect(wrapper.text()).toContain('Test Household')
    expect(wrapper.text()).toContain('123 Test St, 0123 Oslo')
  })

  it('emits edit event when edit button is clicked', async () => {
    const wrapper = mountComponent()
    const editButton = wrapper.findAll('button').filter(btn => btn.text().includes('Endre informasjon'))[0]
    await editButton.trigger('click')
    expect(wrapper.emitted('edit')).toBeTruthy()
  })

  it('emits changeHousehold event when change household button is clicked', async () => {
    const wrapper = mountComponent()
    const changeButton = wrapper.findAll('button').filter(btn => btn.text().includes('Bytt husstand'))[0]
    await changeButton.trigger('click')
    expect(wrapper.emitted('changeHousehold')).toBeTruthy()
  })

  it('disables change household button when allHouseholdsCount is 1 or less', () => {
    const wrapper = mountComponent({
      household: mockHousehold,
      allHouseholdsCount: 1
    })
    const changeButton = wrapper.findAll('button').filter(btn => btn.text().includes('Bytt husstand'))[0]
    expect(changeButton.attributes('disabled')).toBe('')
  })

  it('emits showLocation event when address is clicked', async () => {
    const wrapper = mountComponent()
    await wrapper.find('.cursor-pointer').trigger('click')
    expect(wrapper.emitted('showLocation')).toBeTruthy()
  })
})
