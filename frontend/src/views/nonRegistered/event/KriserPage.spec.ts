import { mount } from '@vue/test-utils'
import { describe, expect, it, vi, beforeEach } from 'vitest'
import { ref } from 'vue'
import KriserPage from './KriserPage.vue'
import { EventResponseStatus } from '@/api/generated/model'

const pushMock = vi.fn()

vi.mock('vue-router', () => ({
  useRouter: vi.fn(() => ({ push: pushMock })),
}))

const eventsData = ref([
  {
    id: 'e-ongoing',
    title: 'Flomfare',
    description: '<p>Beskrivelse</p>',
    status: EventResponseStatus.ONGOING,
    startTime: '2026-01-01T10:00:00Z',
  },
  {
    id: 'e-finished',
    title: 'Storm over',
    description: '<p>Ferdig</p>',
    status: EventResponseStatus.FINISHED,
    endTime: '2026-01-02T10:00:00Z',
  },
])

vi.mock('@/api/generated/event/event', () => ({
  useGetAllEvents: vi.fn(() => ({
    data: eventsData,
    isLoading: ref(false),
    error: ref(null),
  })),
}))

vi.mock('@/api/generated/scenario/scenario', () => ({
  useGetAllScenarios: vi.fn(() => ({
    data: ref([
      { id: 's-1', title: 'Langvarig strombrudd', content: '<p>Innhold</p>' },
      { id: 's-2', title: 'Evakuering', content: '<p>Innhold</p>' },
    ]),
    isLoading: ref(false),
    error: ref(null),
  })),
}))

describe('KriserPage', () => {
  beforeEach(() => {
    pushMock.mockClear()
  })

  it('renders events and switches tabs', async () => {
    const wrapper = mount(KriserPage, {
      global: {
        stubs: {
          BaseButton: { template: '<button @click="$emit(\'click\')"><slot /></button>' },
          BookText: true,
          RouterLink: { template: '<a><slot /></a>' },
        },
      },
    })

    expect(wrapper.text()).toContain('Kriser og hendelser')
    expect(wrapper.text()).toContain('Flomfare')

    const upcomingTab = wrapper.findAll('button').find((btn) => btn.text().includes('Kommende'))
    expect(upcomingTab).toBeTruthy()
    await upcomingTab!.trigger('click')

    expect(wrapper.text()).toContain('Ingen hendelser funnet i denne kategorien.')
  })

  it('navigates to public reflections from button click', async () => {
    const wrapper = mount(KriserPage, {
      global: {
        stubs: {
          BaseButton: { template: '<button @click="$emit(\'click\')"><slot /></button>' },
          BookText: true,
          RouterLink: { template: '<a><slot /></a>' },
        },
      },
    })

    const reflectionsButton = wrapper
      .findAll('button')
      .find((btn) => btn.text().includes('Offentlige Refleksjoner'))
    expect(reflectionsButton).toBeTruthy()
    await reflectionsButton!.trigger('click')

    expect(pushMock).toHaveBeenCalledWith('/refleksjoner/offentlige')
  })
})
