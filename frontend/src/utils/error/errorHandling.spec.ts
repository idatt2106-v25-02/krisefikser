import { describe, expect, it } from 'vitest'
import { handleApiError } from './errorHandling'

describe('handleApiError', () => {
  it('reads Spring ProblemDetail detail field', () => {
    const msg = handleApiError(
      {
        response: {
          status: 400,
          data: { detail: 'Invalid payload', title: 'Bad Request' },
        },
      },
      'fallback',
    )
    expect(msg).toBe('Invalid payload')
  })

  it('prefers detail over message when both exist', () => {
    const msg = handleApiError({
      response: {
        status: 400,
        data: { detail: 'From detail', message: 'From message' },
      },
    })
    expect(msg).toBe('From detail')
  })

  it('uses Norwegian copy for 503', () => {
    const msg = handleApiError({
      response: { status: 503, data: { detail: 'Failed to send email' } },
    })
    expect(msg).toContain('bekreftelses-e-post')
    expect(msg).toContain('MAILTRAP_API_TOKEN')
  })

  it('falls back to default when body has no detail or message', () => {
    expect(handleApiError({ response: { status: 400, data: {} } }, 'fallback')).toBe('fallback')
  })
})
