<script setup lang="ts">
import { ref } from 'vue'
import {
  grantAllCookies,
  grantNecessaryCookiesOnly,
  rejectOptionalCookies,
  isCookieBannerDismissed,
} from '@/plugins/docs/posthog-consent'
import { openCookieSettings } from '@/plugins/docs/cookie-settings-ui'

const isVisible = ref(true)

const acceptAll = () => {
  isVisible.value = false
  grantAllCookies('banner_accept_all')
}

const acceptNecessaryOnly = () => {
  isVisible.value = false
  grantNecessaryCookiesOnly('banner_necessary_only')
}

const rejectOptional = () => {
  isVisible.value = false
  rejectOptionalCookies('banner_reject')
}

const customize = () => {
  openCookieSettings()
}

const checkCookieConsent = () => {
  if (isCookieBannerDismissed()) {
    isVisible.value = false
    return
  }

  isVisible.value = true
}

checkCookieConsent()
</script>

<template>
  <div
    v-if="isVisible"
    class="fixed bottom-0 left-0 right-0 bg-gray-900 text-white p-4 shadow-lg z-50"
    role="region"
    aria-label="Informasjon om informasjonskapsler"
  >
    <div class="container mx-auto flex flex-col gap-4">
      <p class="text-sm text-center sm:text-left">
        Vi bruker nødvendige informasjonskapsler for drift og sikkerhet. Valgfri analyse (PostHog)
        brukes bare hvis du samtykker. Les mer i
        <router-link to="/personvern" class="underline font-medium text-blue-200 hover:text-white">
          personvernerklæringen
        </router-link>
        .
      </p>
      <div
        class="flex flex-col sm:flex-row flex-wrap items-stretch sm:items-center justify-center gap-2 sm:gap-3"
      >
        <button
          type="button"
          class="bg-blue-500 hover:bg-blue-600 text-white px-4 py-2 rounded-md transition-colors text-sm font-medium"
          @click="acceptAll"
        >
          Aksepter alle
        </button>
        <button
          type="button"
          class="bg-gray-700 hover:bg-gray-600 text-white px-4 py-2 rounded-md transition-colors text-sm font-medium border border-gray-600"
          @click="acceptNecessaryOnly"
        >
          Kun nødvendige
        </button>
        <button
          type="button"
          class="bg-transparent border border-white hover:bg-white hover:text-gray-900 text-white px-4 py-2 rounded-md transition-colors text-sm font-medium"
          @click="rejectOptional"
        >
          Avslå valgfrie
        </button>
        <button
          type="button"
          class="bg-transparent border border-blue-300 text-blue-100 hover:bg-blue-900/40 px-4 py-2 rounded-md transition-colors text-sm font-medium"
          @click="customize"
        >
          Tilpass
        </button>
      </div>
    </div>
  </div>
</template>
