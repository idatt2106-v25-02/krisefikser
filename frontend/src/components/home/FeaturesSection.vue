<script lang="ts">
import { defineComponent } from 'vue';
import { useAuthStore } from '@/stores/useAuthStore.ts'; // Adjust path if necessary

export default defineComponent({
  name: 'FeaturesSection',
  setup() {
    const authStore = useAuthStore();

    return {
      isAuthenticated: authStore.isAuthenticated
    };
  }
});
</script>
<template>
  <section class="mb-16">
    <div class="text-center mb-12">
      <h2 class="text-3xl font-bold text-gray-800 mb-4">Nyttige funksjoner</h2>
      <p class="text-xl text-gray-600 max-w-3xl mx-auto">Disse verktøyene hjelper deg å være forberedt og informert før, under og etter en krisesituasjon.</p>
    </div>

    <div class="grid md:grid-cols-2 lg:grid-cols-4 gap-6">
      <!-- Feature 1: Map -->
      <div class="bg-white p-6 rounded-lg shadow border border-gray-100 text-center">
        <div class="bg-blue-50 inline-block p-3 rounded-full mb-4">
          <svg xmlns="http://www.w3.org/2000/svg" class="h-8 w-8 text-blue-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 20l-5.447-2.724A1 1 0 013 16.382V5.618a1 1 0 011.447-.894L9 7m0 13l6-3m-6 3V7m6 10l4.553 2.276A1 1 0 0021 18.382V7.618a1 1 0 00-.553-.894L15 4m0 13V4m0 0L9 7" />
          </svg>
        </div>
        <h3 class="text-lg font-semibold text-gray-800 mb-2">Interaktivt kart</h3>
        <p class="text-gray-600 mb-4">Finn tilfluktsrom, hjertestartere og andre viktige ressurser i nærheten av deg.</p>
        <router-link to="/kart" class="text-blue-600 font-medium hover:underline">Utforsk kartet</router-link>
      </div>

      <!-- Feature 2: Inventory -->
      <div class="bg-white p-6 rounded-lg shadow border border-gray-100 text-center">
        <div class="bg-blue-50 inline-block p-3 rounded-full mb-4">
          <svg xmlns="http://www.w3.org/2000/svg" class="h-8 w-8 text-blue-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 10h18M7 15h1m4 0h1m-7 4h12a3 3 0 003-3V8a3 3 0 00-3-3H6a3 3 0 00-3 3v8a3 3 0 003 3z" />
          </svg>
        </div>
        <h3 class="text-lg font-semibold text-gray-800 mb-2">Beredskapslager</h3>
        <p class="text-gray-600 mb-4">Hold oversikt over ditt eget beredskapslager og få påminnelser om utløpsdatoer.</p>
        <router-link v-if="isAuthenticated" to="/husstand/:id/beredskapslager" class="text-blue-600 font-medium hover:underline">
          Gå til beredskapslager
        </router-link>
        <router-link v-else to="/logg-inn" class="text-blue-600 font-medium hover:underline">
          Logg inn for å bruke
        </router-link>
      </div>

      <!-- Feature 3: Notifications -->
      <div class="bg-white p-6 rounded-lg shadow border border-gray-100 text-center">
        <div class="bg-blue-50 inline-block p-3 rounded-full mb-4">
          <svg xmlns="http://www.w3.org/2000/svg" class="h-8 w-8 text-blue-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 17h5l-1.405-1.405A2.032 2.032 0 0118 14.158V11a6.002 6.002 0 00-4-5.659V5a2 2 0 10-4 0v.341C7.67 6.165 6 8.388 6 11v3.159c0 .538-.214 1.055-.595 1.436L4 17h5m6 0v1a3 3 0 11-6 0v-1m6 0H9" />
          </svg>
        </div>
        <h3 class="text-lg font-semibold text-gray-800 mb-2">Varsler</h3>
        <p class="text-gray-600 mb-4">Motta varsler om krisesituasjoner i ditt område og viktige oppdateringer.</p>
        <router-link v-if="isAuthenticated" to="/varsler" class="text-blue-600 font-medium hover:underline">
          Administrer varsler
        </router-link>
        <router-link v-else to="/register" class="text-blue-600 font-medium hover:underline">
          Registrer deg
        </router-link>
      </div>

      <!-- Feature 4: Household -->
      <div class="bg-white p-6 rounded-lg shadow border border-gray-100 text-center">
        <div class="bg-blue-50 inline-block p-3 rounded-full mb-4">
          <svg xmlns="http://www.w3.org/2000/svg" class="h-8 w-8 text-blue-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 12l2-2m0 0l7-7 7 7M5 10v10a1 1 0 001 1h3m10-11l2 2m-2-2v10a1 1 0 01-1 1h-3m-6 0a1 1 0 001-1v-4a1 1 0 011-1h2a1 1 0 011 1v4a1 1 0 001 1m-6 0h6" />
          </svg>
        </div>
        <h3 class="text-lg font-semibold text-gray-800 mb-2">Husstand</h3>
        <p class="text-gray-600 mb-4">Koble sammen husstanden din for å samarbeide om beredskap og sikkerhet.</p>
        <router-link v-if="isAuthenticated" to="/husstand" class="text-blue-600 font-medium hover:underline">
          Administrer husstand
        </router-link>
        <router-link v-else to="/register" class="text-blue-600 font-medium hover:underline">
          Opprett husstand
        </router-link>
      </div>
    </div>
  </section>
</template>
