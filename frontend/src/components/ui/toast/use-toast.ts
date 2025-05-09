import { ref } from 'vue'
import speechService from '@/services/tts/speechService'
import { useAccessibilityStore } from '@/stores/tts/accessibilityStore'

export interface Toast {
  id: string
  title?: string
  description?: string
  variant?: 'default' | 'destructive' | 'warning'
  class?: string
  dismiss: () => void
}

const toasts = ref<Toast[]>([])

export function useToast() {
  function toast(props: Omit<Toast, 'id' | 'dismiss'>) {
    const id = Math.random().toString(36).slice(2)
    const toast = {
      ...props,
      id,
      dismiss: () => {
        toasts.value = toasts.value.filter((t) => t.id !== id)
      },
    }

    toasts.value = [...toasts.value, toast]

    // Speak the toast if TTS is enabled
    try {
      const accessibilityStore = useAccessibilityStore()
      if (accessibilityStore.ttsEnabled) {
        const toastText = `${props.title ? props.title + '. ' : ''}${props.description || ''}`
        speechService.speak(toastText)
      }
    } catch (error) {
      console.warn('Could not access accessibility store:', error)
    }

    // Auto dismiss after 5 seconds
    setTimeout(() => {
      toast.dismiss()
    }, 5000)

    return toast
  }

  return {
    toast,
    toasts,
  }
}
