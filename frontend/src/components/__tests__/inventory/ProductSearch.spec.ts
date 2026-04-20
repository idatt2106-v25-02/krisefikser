import { describe, expect, it } from 'vitest'
import { mount } from '@vue/test-utils'
import ProductSearch from '@/components/inventory/ProductSearch.vue'

type SearchChangedPayload = [isActive: boolean, results: Array<{ item: { id: string } }>]

const categories = [
  {
    id: 'food',
    name: 'Mat',
    icon: {},
    current: 1,
    target: 5,
    unit: 'stk',
    items: [
      { id: 'i-1', name: 'Hermetikk', amount: 3, unit: 'stk', expiryDate: '2099-12-31' },
      { id: 'i-2', name: 'Ris', amount: 1, unit: 'kg', expiryDate: '2099-12-31' },
    ],
  },
  {
    id: 'water',
    name: 'Vann',
    icon: {},
    current: 1,
    target: 5,
    unit: 'liter',
    items: [{ id: 'w-1', name: 'Drikkevann', amount: 10, unit: 'liter', expiryDate: '2099-12-31' }],
  },
]

function mountSearch() {
  return mount(ProductSearch, {
    props: { categories },
    global: {
      stubs: {
        Input: {
          props: ['modelValue'],
          emits: ['update:modelValue'],
          template:
            '<input data-testid="search-input" :value="modelValue" @input="$emit(\'update:modelValue\', $event.target.value)" />',
        },
        Search: true,
        Filter: true,
      },
    },
  })
}

describe('ProductSearch', () => {
  it('emits searchChanged when query matches an item', async () => {
    const wrapper = mountSearch()

    await wrapper.find('[data-testid="search-input"]').setValue('herme')
    const emitted = wrapper.emitted('searchChanged')

    expect(emitted).toBeTruthy()
    const lastPayload = emitted?.at(-1) ?? []
    expect(lastPayload[0]).toBe(true)
    expect(lastPayload[1]).toHaveLength(1)
  })

  it('emits jumpToItem when a result is clicked', async () => {
    const wrapper = mountSearch()

    await wrapper.find('[data-testid="search-input"]').setValue('herme')
    await wrapper.find('li').trigger('click')

    expect(wrapper.emitted('jumpToItem')?.[0]).toEqual(['food', 'i-1'])
  })

  it('filters low stock items when low stock filter is enabled', async () => {
    const wrapper = mountSearch()

    await wrapper.find('button').trigger('click')
    const checkboxes = wrapper.findAll('input[type="checkbox"]')
    await checkboxes[1].setValue(true)

    const emitted = wrapper.emitted('searchChanged')
    expect(emitted).toBeTruthy()
    const lastPayload = (emitted?.at(-1) ?? []) as SearchChangedPayload
    expect(lastPayload[0]).toBe(true)
    expect(lastPayload[1]).toHaveLength(1)
    expect(lastPayload[1][0].item.id).toBe('i-2')
  })

  it('clears search and emits inactive state when reset button is clicked', async () => {
    const wrapper = mountSearch()

    await wrapper.find('[data-testid="search-input"]').setValue('herme')
    const clearButton = wrapper.findAll('button').find((btn) => btn.text().includes('×'))
    expect(clearButton).toBeTruthy()
    await clearButton!.trigger('click')

    const emitted = wrapper.emitted('searchChanged')
    expect(emitted).toBeTruthy()
    expect(emitted?.at(-1)).toEqual([false, []])
  })
})
