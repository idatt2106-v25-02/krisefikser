import { ref } from 'vue'
import { useForm } from 'vee-validate'
import { useRouter } from 'vue-router'
import { toast } from 'vue-sonner'
import { useAuthStore } from '@/stores/auth/useAuthStore'
import { loginFormSchema } from './schema'
import { useLoginError } from './useLoginError'
import type { ApiError } from '@/types/auth'

export function useLoginForm(redirectPath: string) {
  const router = useRouter()
  const authStore = useAuthStore()
  const { getLoginErrorMessage } = useLoginError()

  const isLoading = ref(false)
  const hasAttemptedLogin = ref(false)
  const formError = ref('')

  const { handleSubmit, meta, resetForm, setFieldError } = useForm({
    validationSchema: loginFormSchema,
  })

  const onSubmit = handleSubmit(async (values) => {
    if (isLoading.value) return

    isLoading.value = true
    hasAttemptedLogin.value = true
    formError.value = ''

    try {
      const loginData = {
        email: values.identifier,
        password: values.password,
      }

      try {
        await authStore.login(loginData)
        toast('Suksess', {
          description: 'Du er nå logget inn',
        })
        await router.push(redirectPath)
      } catch (error: unknown) {
        const errorMessage = getLoginErrorMessage(error as ApiError)
        formError.value = errorMessage

        if (errorMessage.includes('Feil e-post eller passord')) {
          setFieldError('password', 'Feil passord')
        }

        if (errorMessage.includes('ikke registrert')) {
          setFieldError('identifier', 'E-postadressen er ikke registrert')
        }

        toast('Innloggingsfeil', {
          description: errorMessage,
        })
      }
    } catch (error: unknown) {
      const errorMessage = getLoginErrorMessage(error as ApiError)
      formError.value = errorMessage

      toast('Innloggingsfeil', {
        description: errorMessage,
      })

      resetForm({
        values: {
          identifier: values.identifier,
          password: '',
        },
        touched: { identifier: true, password: false },
        errors: {},
      })

      hasAttemptedLogin.value = false
    } finally {
      isLoading.value = false
    }
  })

  return {
    isLoading,
    hasAttemptedLogin,
    formError,
    meta,
    onSubmit
  }
}
