import { beforeEach, describe, expect, it } from 'vitest'
import { createPinia, setActivePinia } from 'pinia'
import { useAccessibilityStore } from '../tts/accessibilityStore'

describe('useAccessibilityStore', () => {
  beforeEach(() => {
    localStorage.clear()
    setActivePinia(createPinia())
  })

  it('toggles tts and persists value', () => {
    const store = useAccessibilityStore()
    expect(store.ttsEnabled).toBe(false)

    store.toggleTTS()

    expect(store.ttsEnabled).toBe(true)
    expect(localStorage.getItem('ttsEnabled')).toBe('true')
  })

  it('stores speech rate and voice selection', () => {
    const store = useAccessibilityStore()
    const voice = { name: 'Norwegian Voice', lang: 'nb-NO' } as SpeechSynthesisVoice

    store.setSpeechRate(1.4)
    store.setSelectedVoice(voice)

    expect(store.speechRate).toBe(1.4)
    expect(store.selectedVoice?.name).toBe('Norwegian Voice')
    expect(localStorage.getItem('speechRate')).toBe('1.4')
    expect(localStorage.getItem('selectedVoiceName')).toBe('Norwegian Voice')
  })
})
