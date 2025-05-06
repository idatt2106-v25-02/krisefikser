import { createComponentWrapper } from '@/components/__tests__/test-utils'
import { describe, it, expect, vi } from 'vitest'
import QuizComponent from '@/components/quiz/QuizComponent.vue'

// Mock accessibility store
vi.mock('@/stores/accessibilityStore', () => ({
  useAccessibilityStore: vi.fn(() => ({
    ttsEnabled: true,
    speechRate: 1,
  })),
}))

// Mock speech synthesis
global.SpeechSynthesisUtterance = vi.fn()
global.speechSynthesis = {
  speak: vi.fn(),
  cancel: vi.fn(),
  pause: vi.fn(),
  resume: vi.fn(),
  speaking: false,
  pending: false,
  paused: false,
  onvoiceschanged: null,
  getVoices: vi.fn().mockReturnValue([]),
  addEventListener: vi.fn(),
  removeEventListener: vi.fn(),
  dispatchEvent: vi.fn().mockReturnValue(true),
}

describe('QuizComponent', () => {
  const mockQuestions = [
    {
      question: 'What is Vue.js?',
      options: [
        'A back-end framework',
        'A front-end framework',
        'A database',
        'An operating system',
      ],
      correctAnswer: 1,
      explanation: 'Vue.js is a progressive JavaScript framework for building user interfaces.',
    },
    {
      question: 'Which is not a Vue.js hook?',
      options: ['onMounted', 'onCreated', 'onUpdated', 'onUnmounted'],
      correctAnswer: 1,
      explanation: 'The correct lifecycle hook is created, not onCreated.',
    },
  ]

  it('renders intro state initially', () => {
    const wrapper = createComponentWrapper(QuizComponent, {
      questions: mockQuestions,
      title: 'Vue Test',
    })

    expect(wrapper.text()).toContain('Vue Test')
    expect(wrapper.text()).toContain('Start Quiz')
  })

  it('transitions to question state when Start Quiz button is clicked', async () => {
    const wrapper = createComponentWrapper(QuizComponent, {
      questions: mockQuestions,
    })

    // Click start button
    await wrapper.find('button').trigger('click')

    // Check that we're now in question state
    expect(wrapper.text()).toContain('Spørsmål 1 av 2')
    expect(wrapper.text()).toContain('What is Vue.js?')
  })

  it('selects answer when option is clicked', async () => {
    const wrapper = createComponentWrapper(QuizComponent, {
      questions: mockQuestions,
    })

    // Start quiz
    await wrapper.find('button').trigger('click')

    // Find and click the first answer option
    const options = wrapper.findAll('[role="radio"]')
    await options[0].trigger('click')

    // Check that the option is selected
    expect(options[0].attributes('aria-checked')).toBe('true')
  })

  it('checks answer when Check Answer button is clicked', async () => {
    const wrapper = createComponentWrapper(QuizComponent, {
      questions: mockQuestions,
    })

    // Start quiz
    await wrapper.find('button').trigger('click')

    // Select an answer (correct one)
    const options = wrapper.findAll('[role="radio"]')
    await options[1].trigger('click')

    // Click check answer button
    await wrapper.find('button:last-child').trigger('click')

    // Check that explanation is shown
    expect(wrapper.text()).toContain('Vue.js is a progressive JavaScript framework')
  })

  it('calculates score correctly', async () => {
    const wrapper = createComponentWrapper(QuizComponent, {
      questions: mockQuestions,
    })

    // Start quiz
    await wrapper.find('button').trigger('click')

    // Answer first question correctly
    const options1 = wrapper.findAll('[role="radio"]')
    await options1[1].trigger('click')
    await wrapper.find('button:last-child').trigger('click')

    // Go to next question
    await wrapper.find('button:last-child').trigger('click')

    // Answer second question incorrectly
    const options2 = wrapper.findAll('[role="radio"]')
    await options2[0].trigger('click')
    await wrapper.find('button:last-child').trigger('click')

    // Finish quiz
    await wrapper.find('button:last-child').trigger('click')

    // Check score display
    expect(wrapper.text()).toContain('1 av 2 riktige svar')
  })
})
