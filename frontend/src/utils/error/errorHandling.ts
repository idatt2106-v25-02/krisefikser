import { toast } from 'vue-sonner'
import type { ApiError } from '../validation/passwordSchemas'

function problemDetailMessage(data: unknown): string | undefined {
  if (!data || typeof data !== 'object') return undefined
  const o = data as { detail?: unknown; message?: unknown }
  if (typeof o.detail === 'string' && o.detail.length > 0) return o.detail
  if (typeof o.message === 'string' && o.message.length > 0) return o.message
  return undefined
}

export function handleApiError(error: unknown, defaultMessage: string = 'En feil oppstod'): string {
  const apiError = error as ApiError
  const status = apiError.response?.status

  if (status === 429) {
    return 'For mange forsøk. Vennligst vent litt før du prøver igjen.'
  }

  if (status === 503) {
    return 'Kunne ikke sende bekreftelses-e-post. Prøv igjen senere. Ved lokal utvikling: sjekk at MAILTRAP_API_TOKEN er satt i backend .env.'
  }

  if (status === 500) {
    return 'Det oppstod en serverfeil. Vennligst prøv igjen senere.'
  }

  return problemDetailMessage(apiError.response?.data) || defaultMessage
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
