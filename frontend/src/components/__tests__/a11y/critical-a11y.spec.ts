import { describe, expect, it } from 'vitest'
import { mount } from '@vue/test-utils'
import { axe, toHaveNoViolations } from 'jest-axe'
import { createMemoryHistory, createRouter } from 'vue-router'
import NotificationModal from '@/components/notification/NotificationModal.vue'
import DataTable from '@/components/ui/data-table/DataTable.vue'

expect.extend(toHaveNoViolations)

describe('Critical accessibility checks', () => {
  it('renders notification modal without axe violations', async () => {
    const router = createRouter({
      history: createMemoryHistory(),
      routes: [{ path: '/', component: { template: '<div />' } }],
    })
    await router.push('/')
    await router.isReady()

    const wrapper = mount(NotificationModal, {
      props: {
        show: true,
        title: 'Varsel',
        message: 'Dette er en testmelding',
        type: 'update',
      },
      attachTo: document.body,
      global: {
        plugins: [router],
      },
    })

    const results = await axe(document.body)
    expect(results).toHaveNoViolations()
    wrapper.unmount()
  })

  it('renders data table without axe violations', async () => {
    const wrapper = mount({
      components: { DataTable },
      template: `
        <main>
          <DataTable
            :columns="[
              { key: 'name', label: 'Navn' },
              { key: 'email', label: 'E-post' }
            ]"
            caption="Brukerliste"
            :row-count="0"
          />
        </main>
      `,
    })

    const main = wrapper.find('main').element
    const results = await axe(main)
    expect(results).toHaveNoViolations()
    wrapper.unmount()
  })
})
