<script setup lang="ts">
import { ref } from 'vue'

const isVisible = ref(true)

const acceptCookies = () => {
  isVisible.value = false
  localStorage.setItem('cookiesAccepted', 'true')
}

// Check if cookies were previously accepted
const checkCookieConsent = () => {
  const cookiesAccepted = localStorage.getItem('cookiesAccepted')
  if (cookiesAccepted === 'true') {
    isVisible.value = false
  }
}

// Check cookie consent status when component is mounted
checkCookieConsent()
</script>

<template>
  <div
    v-if="isVisible"
    class="fixed bottom-0 left-0 right-0 bg-gray-900 text-white p-4 shadow-lg z-50"
  >
    <div class="container mx-auto flex flex-col sm:flex-row items-center justify-between gap-4">
      <p class="text-sm">
        Vi bruker informasjonskapsler (cookies) for å gi deg en bedre brukeropplevelse.
      </p>
      <button
        @click="acceptCookies"
        class="bg-blue-500 hover:bg-blue-600 text-white px-6 py-2 rounded-md transition-colors"
      >
        Aksepter
      </button>
    </div>
  </div>
</template>
