<script setup lang="ts">
import { ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import ResetPasswordView from '@/views/auth/password/ResetPasswordView.vue'
import VerifyToken from '@/views/VerifyToken.vue'

const route = useRoute()
const router = useRouter()
const isTokenValid = ref<boolean>(false)

// If no token is present, redirect immediately
if (!route.query.token) {
  router.push('/registrer')
}

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
        <VerifyToken
          :token="route.query.token as string"
          verify-endpoint="/api/auth/verify-password-reset"
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
        <ResetPasswordView :token="route.query.token as string" />
      </template>
    </div>
  </div>
</template>
