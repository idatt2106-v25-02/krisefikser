<script setup lang="ts">
import { Button } from '@/components/ui/button'
import { useRouter } from 'vue-router'

const router = useRouter()

const handleChangePassword = () => {
  router.push('/endre-passord')
}
</script>

<template>
  <div class="bg-white rounded-lg shadow-sm border border-gray-200 p-6">
    <h2 class="text-lg font-semibold text-gray-800 mb-4">Sikkerhet</h2>
    <p class="text-sm text-gray-600 mb-4">Endre passordet ditt for å holde kontoen din sikker.</p>
    <Button variant="outline" class="w-full" @click="handleChangePassword">Endre passord</Button>
  </div>
</template>
