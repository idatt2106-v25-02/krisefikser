import type { ApiError } from '@/types/auth'

export function useLoginError() {
  function getLoginErrorMessage(error: ApiError) {
    const message = 'Kunne ikke logge inn. Vennligst prøv igjen.'
    const errorMessage = error?.response?.data?.message || ''
    const statusCode = error?.response?.status

    if (
      errorMessage.includes('Bad credentials') ||
      errorMessage.includes('incorrect password') ||
      errorMessage.includes('wrong password') ||
      statusCode === 401
    ) {
      return 'Feil e-post eller passord. Vennligst prøv igjen.'
    }

    if (statusCode === 404) {
      return 'E-postadressen er ikke registrert. Vennligst registrer deg eller sjekk om du har skrevet riktig e-post.'
    }

    if (errorMessage.includes('locked')) {
      const lockTimeMatch = errorMessage.match(/locked until (.+)/i)
      if (lockTimeMatch && lockTimeMatch[1]) {
        try {
          const lockTime = new Date(lockTimeMatch[1])
          const now = new Date()
          const minutesRemaining = Math.ceil((lockTime.getTime() - now.getTime()) / 60000)

          if (minutesRemaining > 0) {
            return `Kontoen er låst på grunn av for mange innloggingsforsøk. Vennligst prøv igjen om ${minutesRemaining} minutt${minutesRemaining === 1 ? '' : 'er'}.`
          }
        } catch (_e) {
          console.error('Error parsing lock time:', _e)
          return 'Kontoen er låst på grunn av for mange innloggingsforsøk. Vennligst prøv igjen senere.'
        }
      }
      return 'Kontoen er låst på grunn av for mange innloggingsforsøk. Vennligst prøv igjen senere.'
    }

    if (errorMessage.includes('disabled')) {
      return 'Kontoen er deaktivert. Vennligst kontakt administrator for hjelp.'
    }

    if (statusCode === 429) {
      return 'For mange innloggingsforsøk. Vennligst vent litt før du prøver igjen.'
    }

    return errorMessage || message
  }

  return {
    getLoginErrorMessage
  }
}
