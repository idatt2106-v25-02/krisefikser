<script lang="ts" setup>
import { ShieldCheck, UserPlus, Key } from 'lucide-vue-next'
import { Button } from '@/components/ui/button'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth/useAuthStore'

const router = useRouter()
const authStore = useAuthStore()

const sendPasswordResetLink = () => {
  router.push('/admin/reset-passord-link')
}
</script>

<template>
  <div>
    <div v-if="!authStore.isSuperAdmin" class="text-sm text-gray-500 italic flex items-center">
      <ShieldCheck class="h-4 w-4 mr-1 text-gray-400" />
      Kun Super Admin kan administrere brukere
    </div>

    <div v-else class="flex space-x-3">
      <Button
        class="flex items-center bg-blue-600 hover:bg-blue-700 text-white"
        variant="default"
        @click="router.push('/admin/invite')"
      >
        <UserPlus class="h-4 w-4 mr-1" />
        Inviter ny admin
      </Button>
      <Button
        class="flex items-center bg-green-600 hover:bg-green-700 text-white"
        variant="default"
        @click="sendPasswordResetLink"
      >
        <Key class="h-4 w-4 mr-1" />
        Send passord-link
      </Button>
    </div>
  </div>
</template>
