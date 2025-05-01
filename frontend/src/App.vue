<script setup lang="ts">
import { RouterView } from 'vue-router'
import { onMounted } from 'vue'
import AppNavbar from '@/components/layout/Navbar.vue'
import AppFooter from '@/components/layout/Footer.vue'
import Toaster from '@/components/ui/toast/toaster.vue'
import AccessibilityMenu from '@/components/AccessibilityMenu.vue'
import router from '@/router'

// Add this function to make all interactive elements focusable
onMounted(() => {
  // Make all router-links and interactive elements focusable
  const makeElementsFocusable = () => {
    document.querySelectorAll('a, [to], .router-link, button').forEach(el => {
      if (!el.hasAttribute('tabindex')) {
        el.setAttribute('tabindex', '0');
      }
    });
  };

  // Run immediately
  makeElementsFocusable();

  // And after route changes
  if (router && router.isReady) {
    router.afterEach(() => {
      setTimeout(makeElementsFocusable, 200);
    });
  }
});
</script>

<template>
  <div id="app">
    <AppNavbar />
    <div class="min-h-screen">
      <router-view />
    </div>
    <AppFooter />
    <Toaster />
    <AccessibilityMenu />
  </div>
</template>

<style>
/* Add these global focus styles */
a, [role="link"], button, [role="button"], input, select, textarea, [tabindex]:not([tabindex="-1"]) {
  outline: 2px solid transparent;
}

a:focus-visible,
[role="link"]:focus-visible,
button:focus-visible,
[role="button"]:focus-visible,
input:focus-visible,
select:focus-visible,
textarea:focus-visible,
[tabindex]:not([tabindex="-1"]):focus-visible {
  outline: 2px solid #4285f4 !important;
  outline-offset: 2px !important;
}
</style>
