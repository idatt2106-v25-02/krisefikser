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

import DuringCrisisView from './DuringCrisisView.vue'

describe('DuringCrisisView', () => {
  it('renders crisis guidance sections and quiz data', () => {
    const wrapper = mount(DuringCrisisView)

    expect(wrapper.text()).toContain('Når krisen inntreffer')
    expect(wrapper.text()).toContain('En krisesituasjon kan være psykisk belastende.')
    expect(wrapper.text()).toContain('Test din kunnskap om krisehåndtering')
    expect((wrapper.vm as unknown as { quizQuestions: unknown[] }).quizQuestions).toHaveLength(4)
  })
})
