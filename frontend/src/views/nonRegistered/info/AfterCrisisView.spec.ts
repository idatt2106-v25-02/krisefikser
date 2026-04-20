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

import AfterCrisisView from './AfterCrisisView.vue'

describe('AfterCrisisView', () => {
  it('renders recovery sections and quiz data', () => {
    const wrapper = mount(AfterCrisisView)

    expect(wrapper.text()).toContain('Gjenoppbygging og normalisering')
    expect(wrapper.text()).toContain('Gjenopprettelse av normalitet er viktig, men kan ta tid:')
    expect(wrapper.text()).toContain('Test din kunnskap om tiden etter en krise')
    expect((wrapper.vm as unknown as { quizQuestions: unknown[] }).quizQuestions).toHaveLength(4)
  })
})
