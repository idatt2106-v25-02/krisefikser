import * as z from 'zod'

// Common password validation schema
export const passwordValidationSchema = z
  .string()
  .min(8, 'Passord må være minst 8 tegn')
  .max(50, 'Passord kan være maks 50 tegn')
  .regex(/[A-Z]/, 'Må inneholde minst én stor bokstav')
  .regex(/[a-z]/, 'Må inneholde minst én liten bokstav')
  .regex(/[0-9]/, 'Må inneholde minst ett tall')
  .regex(/[^A-Za-z0-9]/, 'Må inneholde minst ett spesialtegn')

// Schema for password confirmation
export function createPasswordConfirmationSchema(passwordField: string = 'password', confirmField: string = 'confirmPassword') {
  return {
    [passwordField]: passwordValidationSchema,
    [confirmField]: z.string(),
  }
}

// Common API error type
export type ApiError = {
  response?: {
    data?: {
      message?: string
    }
    status?: number
  }
}
