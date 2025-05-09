<script setup lang="ts">
import { Home, Map as MapIcon, Plus, UserPlus, Users } from 'lucide-vue-next'
import { useAuthStore } from '@/stores/auth/useAuthStore'
import { Button } from '@/components/ui/button'
import { computed } from 'vue'
import router from '@/router'

const authStore = useAuthStore()
const isAuthenticated = computed(() => authStore.isAuthenticated)

const handleCreateHousehold = () => {
  if (isAuthenticated.value) {
    router.push('/husstand/opprett')
  } else {
    router.push('/registrer')
  }
}
const handleInviteHousehold = () => {
  if (isAuthenticated.value) {
    router.push('/dashboard')
  } else {
    router.push('/logg-inn')
  }
}
</script>

<template>
  <div class="bg-gray-50 min-h-screen py-12">
    <div class="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8">
      <!-- Header -->
      <div class="text-center mb-12">
        <h1 class="text-4xl font-bold text-gray-900 mb-4">Kom i gang med husstand</h1>
        <p class="text-xl text-gray-600 max-w-3xl mx-auto">
          Husstander hjelper deg å organisere beredskapslageret ditt og planlegge møtepunkter for
          familien din i en krisesituasjon.
        </p>
      </div>

      <!-- Options cards -->
      <div class="grid md:grid-cols-2 gap-8 mb-12">
        <!-- Create household card -->
        <div
          class="bg-white p-8 rounded-xl shadow-md border border-gray-200 hover:shadow-lg transition-shadow"
        >
          <div class="flex flex-col items-center text-center">
            <div class="bg-blue-100 p-4 rounded-full mb-6">
              <Home class="h-10 w-10 text-blue-600" />
            </div>
            <h2 class="text-2xl font-bold text-gray-800 mb-3">Opprett en husstand</h2>
            <p class="text-gray-600 mb-6">
              Start en ny husstand for deg og din familie. Du kan invitere andre medlemmer til å
              delta og samarbeide om beredskapslageret ditt.
            </p>
            <Button
              @click="handleCreateHousehold"
              class="bg-blue-600 text-white hover:bg-blue-700 transition flex items-center"
            >
              <Plus class="h-4 w-4 mr-2" />
              {{ isAuthenticated ? 'Opprett ny husstand' : 'Registrer deg for å opprette' }}
            </Button>
          </div>
        </div>

        <!-- Join household card -->
        <div
          class="bg-white p-8 rounded-xl shadow-md border border-gray-200 hover:shadow-lg transition-shadow"
        >
          <div class="flex flex-col items-center text-center">
            <div class="bg-green-100 p-4 rounded-full mb-6">
              <UserPlus class="h-10 w-10 text-green-600" />
            </div>
            <h2 class="text-2xl font-bold text-gray-800 mb-3">Bli med i en husstand</h2>
            <p class="text-gray-600 mb-6">
              Har du blitt invitert? For å bli med i en eksisterende husstand må du motta en
              invitasjon. Se invitasjonen på din profil.
            </p>
            <Button
              @click="handleInviteHousehold"
              class="bg-green-600 text-white hover:bg-text-700 transition flex items-center"
            >
              <Plus class="h-4 w-4 mr-2" />
              {{ isAuthenticated ? 'Din profil' : 'Logg inn' }}
            </Button>
          </div>
        </div>
      </div>

      <!-- Benefits section -->
      <div class="bg-white p-8 rounded-xl shadow-md border border-gray-200 mb-12">
        <h2 class="text-2xl font-bold text-gray-800 mb-6 text-center">Fordeler med en husstand</h2>

        <div class="grid md:grid-cols-3 gap-6">
          <div class="flex flex-col items-center text-center">
            <div class="bg-blue-50 p-3 rounded-full mb-4">
              <Users class="h-6 w-6 text-blue-600" />
            </div>
            <h3 class="text-lg font-semibold text-gray-800 mb-2">Samarbeid med familien</h3>
            <p class="text-gray-600">
              Planlegg beredskap sammen med din familie. Alle medlemmer kan se og bidra til
              beredskapslageret.
            </p>
          </div>

          <div class="flex flex-col items-center text-center">
            <div class="bg-blue-50 p-3 rounded-full mb-4">
              <Home class="h-6 w-6 text-blue-600" />
            </div>
            <h3 class="text-lg font-semibold text-gray-800 mb-2">Felles beredskapslager</h3>
            <p class="text-gray-600">
              Hold oversikt over mat, vann og forsyninger for hele husstanden. Få varslinger når
              varer nærmer seg utløpsdato.
            </p>
          </div>

          <div class="flex flex-col items-center text-center">
            <div class="bg-blue-50 p-3 rounded-full mb-4">
              <MapIcon class="h-6 w-6 text-blue-600" />
            </div>
            <h3 class="text-lg font-semibold text-gray-800 mb-2">Møtepunkter ved krise</h3>
            <p class="text-gray-600">
              Definer møtesteder hvor familien skal samles i en krisesituasjon. Del kart og planer
              med alle husstandsmedlemmer.
            </p>
          </div>
        </div>
      </div>

      <!-- CTA section - Only show when not authenticated -->
      <div v-if="!isAuthenticated" class="text-center">
        <h2 class="text-2xl font-bold text-gray-800 mb-4">Klar til å komme i gang?</h2>
        <p class="text-gray-600 mb-6">
          Det tar bare noen få minutter å registrere deg og begynne å forberede husstanden din på
          krisesituasjoner.
        </p>
        <div class="flex flex-col sm:flex-row justify-center space-y-4 sm:space-y-0 sm:space-x-4">
          <router-link
            to="/registrer"
            class="bg-blue-600 text-white px-6 py-3 rounded-md hover:bg-blue-700 transition"
          >
            Registrer deg nå
          </router-link>
          <router-link
            to="/logg-inn"
            class="bg-white border border-gray-300 text-gray-700 px-6 py-3 rounded-md hover:bg-gray-50 transition"
          >
            Logg inn
          </router-link>
        </div>
      </div>
    </div>
  </div>
</template>
