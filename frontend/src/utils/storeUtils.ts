import { getActivePinia } from 'pinia'
import type { Store } from 'pinia'

export function getStore<T extends Store>(storeId: string): T | null {
  try {
    const pinia = getActivePinia()
    if (!pinia) {
      console.warn('No active Pinia instance found')
      return null
    }
    return pinia.state.value[storeId] as T
  } catch (error) {
    console.warn('Error accessing store:', error)
    return null
  }
}

export function getAccessibilityStore() {
  const store = getStore('accessibility')
  if (!store) {
    return {
      ttsEnabled: false,
      speechRate: 1,
      selectedVoice: null,
      voices: []
    }
  }
  return store
} 