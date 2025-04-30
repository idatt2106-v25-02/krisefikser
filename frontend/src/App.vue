<script setup lang="ts">
import { RouterView } from 'vue-router'
import AppNavbar from '@/components/layout/Navbar.vue'
import AppFooter from '@/components/layout/Footer.vue'
import Toaster from '@/components/ui/toast/toaster.vue'
</script>

<template>
  <div id="app">
    <AppNavbar />
    <div class="min-h-screen">
      <router-view />
    </div>
    <AppFooter />
    <Toaster />
  </div>
</template>
