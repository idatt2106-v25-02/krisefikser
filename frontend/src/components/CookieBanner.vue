<script setup lang="ts">
import { ref } from 'vue'
import {
  grantTrackingConsent,
  revokeTrackingConsent,
  hasTrackingConsent,
} from '@/plugins/docs/posthog-consent'

const isVisible = ref(true)

const acceptCookies = () => {
  isVisible.value = false
  localStorage.setItem('cookiesAccepted', 'true')
  grantTrackingConsent()
}

const rejectCookies = () => {
  isVisible.value = false
  localStorage.setItem('cookiesAccepted', 'false')
  revokeTrackingConsent()
}

// Check if cookies were previously accepted
const checkCookieConsent = () => {
  const cookiesAccepted = localStorage.getItem('cookiesAccepted')
  if (cookiesAccepted === 'true') {
    if (!hasTrackingConsent()) {
      grantTrackingConsent()
    }
    isVisible.value = false
    return
  }

  if (cookiesAccepted === 'false') {
    revokeTrackingConsent()
    isVisible.value = false
    return
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
      <button
        @click="rejectCookies"
        class="bg-transparent border border-white hover:bg-white hover:text-gray-900 text-white px-6 py-2 rounded-md transition-colors"
      >
        Avslå
      </button>
    </div>
  </div>
</template>
