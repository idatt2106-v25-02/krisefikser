import { toast } from 'vue-sonner'
import type { ApiError } from '../validation/passwordSchemas'

export function handleApiError(error: unknown, defaultMessage: string = 'En feil oppstod'): string {
  const apiError = error as ApiError
  const errorMessage = apiError.response?.data?.message || defaultMessage

  if (apiError.response?.status === 429) {
    return 'For mange forsøk. Vennligst vent litt før du prøver igjen.'
  }

  if (apiError.response?.status === 500) {
    return 'Det oppstod en serverfeil. Vennligst prøv igjen senere.'
  }

  return errorMessage
}

export function showErrorToast(title: string, error: unknown, defaultMessage: string = 'En feil oppstod') {
  const message = handleApiError(error, defaultMessage)
  toast(title, {
    description: message,
  })
  return message
}

export function showSuccessToast(title: string, description?: string) {
  toast(title, {
    description,
  })
}
