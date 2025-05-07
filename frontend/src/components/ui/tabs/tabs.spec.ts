import { mount } from '@vue/test-utils'
import { describe, it, expect } from 'vitest'
import { nextTick } from 'vue'
import { Tabs, TabsList, TabsTrigger, TabsContent } from '.'

describe('Tabs Components', () => {
  it('renders basic tabs structure', async () => {
    const wrapper = mount({
      components: {
        Tabs,
        TabsList,
        TabsTrigger,
        TabsContent,
      },
      template: `
        <Tabs default-value="tab1">
          <TabsList>
            <TabsTrigger value="tab1">Tab 1</TabsTrigger>
            <TabsTrigger value="tab2">Tab 2</TabsTrigger>
          </TabsList>
          <TabsContent value="tab1">Content 1</TabsContent>
          <TabsContent value="tab2">Content 2</TabsContent>
        </Tabs>
      `,
    })

    // Allow components to render
    await nextTick()

    // Test the structure exists correctly
    expect(wrapper.findComponent(Tabs).exists()).toBe(true)
    expect(wrapper.findComponent(TabsList).exists()).toBe(true)

    // Test tab triggers render correctly
    const triggers = wrapper.findAllComponents(TabsTrigger)
    expect(triggers.length).toBe(2)
    expect(triggers[0].text()).toBe('Tab 1')
    expect(triggers[1].text()).toBe('Tab 2')

    // Test tab contents exist (but don't test their text content)
    const contents = wrapper.findAllComponents(TabsContent)
    expect(contents.length).toBe(2)

    // Check the first tab's content, which should be visible by default
    expect(contents[0].text()).toBe('Content 1')

    // Verify the second tab exists
    expect(contents[1].exists()).toBe(true)
  })

  it('TabsList renders with correct classes', async () => {
    // Mount TabsList within a Tabs component
    const wrapper = mount(Tabs, {
      slots: {
        default: '<TabsList></TabsList>',
      },
      global: {
        components: { TabsList },
      },
    })

    await nextTick()

    const tabsList = wrapper.findComponent(TabsList)
    expect(tabsList.exists()).toBe(true)
    expect(tabsList.classes()).toContain('bg-muted')
  })

  it('TabsTrigger renders with value and slot content', async () => {
    const wrapper = mount(Tabs, {
      slots: {
        default: `
          <TabsList>
            <TabsTrigger value="test-tab">Test Tab</TabsTrigger>
          </TabsList>
        `,
      },
      global: {
        components: {
          TabsList,
          TabsTrigger,
        },
      },
    })

    await nextTick()

    const trigger = wrapper.findComponent(TabsTrigger)
    expect(trigger.exists()).toBe(true)
    expect(trigger.text()).toBe('Test Tab')
  })
})
