import { ref } from 'vue'

export interface Toast {
  id: string
  title?: string
  description?: string
  variant?: 'default' | 'destructive'
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
