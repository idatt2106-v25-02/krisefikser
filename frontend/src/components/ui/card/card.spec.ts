import { mount } from '@vue/test-utils'
import { describe, it, expect } from 'vitest'
import { Card, CardHeader, CardTitle, CardDescription, CardContent, CardFooter } from '.'

describe('Card Components', () => {
  it('Card renders correctly', () => {
    const wrapper = mount(Card)
    expect(wrapper.exists()).toBe(true)
    expect(wrapper.classes()).toContain('bg-card')
  })

  it('CardHeader renders correctly', () => {
    const wrapper = mount(CardHeader)
    expect(wrapper.exists()).toBe(true)
    expect(wrapper.classes()).toContain('grid')
  })

  it('CardTitle renders correctly', () => {
    const title = 'Card Title'
    const wrapper = mount(CardTitle, {
      slots: {
        default: title,
      },
    })
    expect(wrapper.exists()).toBe(true)
    expect(wrapper.text()).toBe(title)
    expect(wrapper.classes()).toContain('font-semibold')
  })

  it('CardDescription renders correctly', () => {
    const description = 'Card Description'
    const wrapper = mount(CardDescription, {
      slots: {
        default: description,
      },
    })
    expect(wrapper.exists()).toBe(true)
    expect(wrapper.text()).toBe(description)
    expect(wrapper.classes()).toContain('text-muted-foreground')
  })

  it('CardContent renders correctly', () => {
    const content = 'Content'
    const wrapper = mount(CardContent, {
      slots: {
        default: content,
      },
    })
    expect(wrapper.exists()).toBe(true)
    expect(wrapper.text()).toBe(content)
    expect(wrapper.classes()).toContain('px-6')
  })

  it('CardFooter renders correctly', () => {
    const footer = 'Footer Content'
    const wrapper = mount(CardFooter, {
      slots: {
        default: footer,
      },
    })
    expect(wrapper.exists()).toBe(true)
    expect(wrapper.text()).toBe(footer)
    expect(wrapper.classes()).toContain('flex')
  })

  it('renders full card with all components', () => {
    const wrapper = mount({
      components: {
        Card,
        CardHeader,
        CardTitle,
        CardDescription,
        CardContent,
        CardFooter,
      },
      template: `
        <Card>
          <CardHeader>
            <CardTitle>Test Title</CardTitle>
            <CardDescription>Test Description</CardDescription>
          </CardHeader>
          <CardContent>Test Content</CardContent>
          <CardFooter>Test Footer</CardFooter>
        </Card>
      `,
    })

    expect(wrapper.findComponent(Card).exists()).toBe(true)
    expect(wrapper.findComponent(CardHeader).exists()).toBe(true)
    expect(wrapper.findComponent(CardTitle).text()).toBe('Test Title')
    expect(wrapper.findComponent(CardDescription).text()).toBe('Test Description')
    expect(wrapper.findComponent(CardContent).text()).toBe('Test Content')
    expect(wrapper.findComponent(CardFooter).text()).toBe('Test Footer')
  })
})
