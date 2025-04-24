<script setup lang="ts">
import { RouterView } from "vue-router"
import AppNavbar from '@/components/layout/Navbar.vue';
import AppFooter from '@/components/layout/Footer.vue';

</script>

<template>
  <div id="app">
    <AppNavbar />
    <router-view />
    <AppFooter />
  </div>
</template>
