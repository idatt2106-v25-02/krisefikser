import { describe, expect, it } from 'vitest'
import { mount } from '@vue/test-utils'
import ProductSearch from '@/components/inventory/ProductSearch.vue'

describe('ProductSearch', () => {
  it('emits searchChanged when query matches an item', async () => {
    const wrapper = mount(ProductSearch, {
      props: {
        categories: [
          {
            id: 'food',
            name: 'Mat',
            icon: {},
            current: 1,
            target: 5,
            unit: 'stk',
            items: [{ id: 'i-1', name: 'Hermetikk', amount: 3, unit: 'stk' }],
          },
        ],
      },
      global: {
        stubs: {
          Input: {
            props: ['modelValue'],
            emits: ['update:modelValue'],
            template:
              '<input data-testid="search-input" @input="$emit(\'update:modelValue\', $event.target.value)" />',
          },
          Search: true,
          Filter: true,
        },
      },
    })

    await wrapper.find('[data-testid="search-input"]').setValue('herme')
    const emitted = wrapper.emitted('searchChanged')

    expect(emitted).toBeTruthy()
    const lastPayload = emitted?.at(-1) ?? []
    expect(lastPayload[0]).toBe(true)
    expect(lastPayload[1]).toHaveLength(1)
  })
})
