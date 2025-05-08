<script setup lang="ts">
import { RouterView } from 'vue-router'
import { onMounted, ref, watch } from 'vue'
import AppNavbar from '@/components/layout/Navbar.vue'
import AppFooter from '@/components/layout/Footer.vue'
import AccessibilityMenu from '@/components/textToSpeech/AccessibilityMenu.vue'
import ReadPageButton from '@/components/textToSpeech/ReadPageButton.vue'
import LocationTracker from '@/components/LocationTracker.vue'
import CookieBanner from '@/components/CookieBanner.vue'
import router from '@/router'
import { useAccessibilityStore } from '@/stores/tts/accessibilityStore.ts'
import { Toaster } from '@/components/ui/sonner'

// Get accessibility store and set up reactive state
const accessibilityStore = useAccessibilityStore()
const ttsEnabled = ref(accessibilityStore.ttsEnabled)

// Watch for changes in TTS state
watch(
  () => accessibilityStore.ttsEnabled,
  (newValue) => {
    ttsEnabled.value = newValue
  },
)

onMounted(() => {
  // Make all router-links and interactive elements focusable
  const makeElementsFocusable = () => {
    document.querySelectorAll('a, [to], .router-link, button').forEach((el) => {
      if (!el.hasAttribute('tabindex')) {
        el.setAttribute('tabindex', '0')
      }
    })
  }

  // Run immediately
  makeElementsFocusable()

  // And after route changes
  if (router) {
    router.afterEach(() => {
      setTimeout(makeElementsFocusable, 200)
    })
  }
})
</script>

<template>
  <div id="app">
    <LocationTracker />
    <CookieBanner />
    <!-- TTS active indicator -->
    <div
      v-if="ttsEnabled"
      class="fixed top-4 right-4 bg-blue-100 text-blue-800 px-3 py-1 rounded-full text-sm font-medium z-50 flex items-center"
    >
      <svg
        xmlns="http://www.w3.org/2000/svg"
        class="h-4 w-4 mr-1"
        fill="none"
        viewBox="0 0 24 24"
        stroke="currentColor"
      >
        <path
          stroke-linecap="round"
          stroke-linejoin="round"
          stroke-width="2"
          d="M15.536 8.464a5 5 0 010 7.072m2.828-9.9a9 9 0 010 12.728M5.586 15H4a1 1 0 01-1-1v-4a1 1 0 011-1h1.586l4.707-4.707C10.923 3.663 12 4.109 12 5v14c0 .891-1.077 1.337-1.707.707L5.586 15z"
        />
      </svg>
      Tekst-til-tale aktiv
    </div>

    <AppNavbar />
    <div id="main-content" class="min-h-screen">
      <router-view />
    </div>
    <AppFooter />
    <Toaster />
    <AccessibilityMenu />
    <ReadPageButton />
  </div>
</template>

<style>
/* Add these global focus styles */
a,
[role='link'],
button,
[role='button'],
input,
select,
textarea,
[tabindex]:not([tabindex='-1']) {
  outline: 2px solid transparent;
}

a:focus-visible,
[role='link']:focus-visible,
button:focus-visible,
[role='button']:focus-visible,
input:focus-visible,
select:focus-visible,
textarea:focus-visible,
[tabindex]:not([tabindex='-1']):focus-visible {
  outline: 2px solid #4285f4 !important;
  outline-offset: 2px !important;
}
</style>
