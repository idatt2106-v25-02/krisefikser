<script lang="ts">
import { defineComponent } from 'vue';
import { useAuthStore } from '@/stores/auth/useAuthStore';

export default defineComponent({
  name: 'HeroSection',
  setup() {
    const authStore = useAuthStore();
    return { authStore };
  }
});
</script>

<template>
  <header class="relative bg-blue-900 text-white border-b-0">
    <div class="container mx-auto px-4 py-16 md:py-24 relative z-10">
      <div class="max-w-3xl">
        <h1 class="text-3xl md:text-4xl lg:text-5xl font-bold mb-4">Vær forberedt når krisen rammer</h1>
        <p class="text-xl mb-8">Krisefikser.app hjelper deg å øke din egenberedskap og gir deg tilgang til viktig informasjon før, under og etter en krise.</p>

        <!-- Different button sets based on authentication status -->
        <div v-if="!authStore.isAuthenticated" class="flex flex-col sm:flex-row space-y-4 sm:space-y-0 sm:space-x-4">
          <router-link to="/registrer" class="bg-white text-blue-900 font-bold py-3 px-6 rounded-md text-center hover:bg-blue-100 transition">
            Kom i gang nå
          </router-link>
          <router-link to="/kart" class="bg-transparent border-2 border-white py-3 px-6 rounded-md text-center hover:bg-blue-800 transition">
            Se kart
          </router-link>
        </div>

        <div v-else class="flex flex-col sm:flex-row space-y-4 sm:flex-row sm:space-y-0 sm:space-x-4">
          <router-link to="/husstand" class="bg-white text-blue-900 font-bold py-3 px-6 rounded-md text-center hover:bg-blue-100 transition">
            Min husstand
          </router-link>
          <router-link to="/kart" class="bg-transparent border-2 border-white py-3 px-6 rounded-md text-center hover:bg-blue-800 transition">
            Se kart
          </router-link>
        </div>
      </div>
    </div>
    <!-- Wave decoration at bottom of hero -->
    <div class="absolute bottom-0 left-0 right-0 z-0">
      <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 1440 100" class="fill-slate-50">
        <path d="M0,32L60,42.7C120,53,240,75,360,74.7C480,75,600,53,720,48C840,43,960,53,1080,58.7C1200,64,1320,64,1380,64L1440,64L1440,100L1380,100C1320,100,1200,100,1080,100C960,100,840,100,720,100C600,100,480,100,360,100C240,100,120,100,60,100L0,100Z"></path>
      </svg>
    </div>
  </header>
</template>
