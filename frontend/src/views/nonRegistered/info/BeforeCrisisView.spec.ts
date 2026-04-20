import { mount } from '@vue/test-utils'
import { describe, expect, it, vi } from 'vitest'

vi.mock('@/components/layout/PageLayout.vue', () => ({
  default: { name: 'PageLayout', template: '<div><slot /></div>' },
}))
vi.mock('@/components/ui/card/ContentCard.vue', () => ({
  default: { name: 'ContentCard', template: '<section><slot /></section>' },
}))
vi.mock('@/components/ui/alert/AlertBox.vue', () => ({
  default: { name: 'AlertBox', template: '<div><slot /></div>' },
}))
vi.mock('@/components/ui/cta/CTASection.vue', () => ({
  default: { name: 'CTASection', template: '<div />' },
}))
vi.mock('@/components/quiz/QuizComponent.vue', () => ({
  default: { name: 'QuizComponent', template: '<div />' },
}))

import BeforeCrisisView from './BeforeCrisisView.vue'

describe('BeforeCrisisView', () => {
  it('renders key sections and quiz data', () => {
    const wrapper = mount(BeforeCrisisView)

    expect(wrapper.text()).toContain('Forbered deg og din husstand')
    expect(wrapper.text()).toContain(
      'En beredskapsplan hjelper hele husstanden med å vite hva som skal gjøres i en krisesituasjon.',
    )
    expect(wrapper.text()).toContain('Test din kunnskap om kriseberedskap')
    expect((wrapper.vm as unknown as { quizQuestions: unknown[] }).quizQuestions).toHaveLength(4)
  })
})
