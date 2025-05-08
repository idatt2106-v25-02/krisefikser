<script setup lang="ts">
import { onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useVerifyAdminLogin } from '@/api/generated/authentication/authentication'
import { useAuthStore } from '@/stores/auth/useAuthStore'
import { toast } from 'vue-sonner'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

const { mutate: verifyAdminLogin, isPending } = useVerifyAdminLogin({
  mutation: {
    onSuccess: (data) => {
      // Store the tokens and redirect to admin dashboard
      authStore.updateTokens(data.accessToken, data.refreshToken)
      toast.success('Admin login verified successfully')
      router.push('/admin')
    },
    onError: () => {
      toast.error('Invalid or expired verification link')
      router.push('/logg-inn')
    },
  },
})

onMounted(() => {
  const token = route.query.token as string
  if (token) {
    verifyAdminLogin({ params: { token } })
  } else {
    toast.error('No verification token found')
    router.push('/logg-inn')
  }
})
</script>

<template>
  <div class="min-h-screen flex items-center justify-center bg-gray-50">
    <div class="max-w-md w-full space-y-8 p-8 bg-white rounded-lg shadow">
      <div class="text-center">
        <h2 class="mt-6 text-3xl font-bold text-gray-900">
          Verifying Admin Login
        </h2>
        <p class="mt-2 text-sm text-gray-600">
          Please wait while we verify your admin login...
        </p>
      </div>
      <div v-if="isPending" class="flex justify-center">
        <div class="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600"></div>
      </div>
    </div>
  </div>
</template>
