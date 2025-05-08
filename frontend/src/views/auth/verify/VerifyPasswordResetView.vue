<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import TokenVerifier from '@/components/auth/TokenVerifier.vue'
import ResetPasswordView from '@/views/auth/password/ResetPasswordView.vue'

const route = useRoute()
const router = useRouter()
const token = ref<string>('')
const isTokenValid = ref<boolean>(false)

onMounted(() => {
  // Extract token from URL query parameters
  const tokenFromUrl = route.query.token as string
  if (!tokenFromUrl) {
    router.push('/registrer')
    return
  }
  token.value = tokenFromUrl
})

const handleTokenValidated = (valid: boolean) => {
  isTokenValid.value = valid
  if (!valid) {
    router.push('/registrer')
  }
}
</script>

<template>
  <div class="min-h-screen flex items-center justify-center bg-white py-12">
    <div class="w-full max-w-sm p-8 border border-gray-200 rounded-xl shadow-sm bg-white space-y-5">
      <template v-if="!isTokenValid">
        <TokenVerifier
          :token="token"
          verify-endpoint="http://localhost:8080/api/auth/verify-password-reset"
          success-message="Din passordtilbakestillingslenke er gyldig. Du kan nå sette et nytt passord."
          error-message="Lenken er ugyldig eller utløpt. Vennligst be om en ny passordtilbakestillingslenke."
          success-redirect-path="/reset-passord"
          error-redirect-path="/registrer"
          loading-message="Validerer passordtilbakestillingslenken..."
          title="Passordtilbakestilling"
          @token-validated="handleTokenValidated"
        />
      </template>
      <template v-else>
        <ResetPasswordView :token="token" />
      </template>
    </div>
  </div>
</template>
