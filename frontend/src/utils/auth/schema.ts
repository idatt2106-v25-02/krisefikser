import { toTypedSchema } from '@vee-validate/zod'
import * as z from 'zod'

export const loginFormSchema = toTypedSchema(
  z.object({
    identifier: z.string().email('Ugyldig e-post').min(5, 'E-post er for kort'),
    password: z.string().min(1, 'Passord er påkrevd'),
  }),
)
