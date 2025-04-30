<template>
  <footer class="bg-blue-900 text-white">
    <div class="container mx-auto px-4 py-12">
      <div class="grid md:grid-cols-4 gap-8">
        <!-- Column 1: Logo and description -->
        <div>
          <div class="flex items-center mb-4">
            <img src="@/assets/logo.svg" alt="Krisefikser.no" class="h-10 w-auto mr-3" />
            <span class="text-xl font-bold">Krisefikser.no</span>
          </div>
          <p class="text-blue-200 mb-4">Vi hjelper deg å være forberedt når krisen rammer – før, under og etter.</p>
        </div>

        <!-- Column 2: Navigation -->
        <div>
          <h3 class="text-lg font-semibold mb-4">Navigasjon</h3>
          <ul class="space-y-2">
            <!-- Links for all users -->
            <li><router-link to="/" class="text-blue-200 hover:text-white transition">Hjem</router-link></li>
            <li><router-link to="/kart" class="text-blue-200 hover:text-white transition">Kart</router-link></li>

            <!-- Authentication-aware links -->
            <template v-if="!authStore.isAuthenticated">
              <li><router-link to="/register" class="text-blue-200 hover:text-white transition">Registrer deg</router-link></li>
              <li><router-link to="/logg-inn" class="text-blue-200 hover:text-white transition">Logg inn</router-link></li>
            </template>

            <template v-else>
              <li><router-link to="/dashboard" class="text-blue-200 hover:text-white transition">Min profil</router-link></li>
              <li><router-link to="/husstand" class="text-blue-200 hover:text-white transition">Husstand</router-link></li>
              <li><router-link to="/husstand/:id/beredskapslager" class="text-blue-200 hover:text-white transition">Beredskapslager</router-link></li>
            </template>

            <!-- Admin links -->
            <li v-if="authStore.isAdmin">
              <router-link to="/admin" class="text-blue-200 hover:text-white transition">Admin</router-link>
            </li>
          </ul>
        </div>

        <!-- Column 3: About -->
        <div>
          <h3 class="text-lg font-semibold mb-4">Om oss</h3>
          <ul class="space-y-2">
            <li><router-link to="/om-oss" class="text-blue-200 hover:text-white transition">Om Krisefikser</router-link></li>
            <li><router-link to="/personvern" class="text-blue-200 hover:text-white transition">Personvern</router-link></li>
            <li><router-link to="/info/for-krisen" class="text-blue-200 hover:text-white transition">Før krisen</router-link></li>
            <li><router-link to="/info/under-krisen" class="text-blue-200 hover:text-white transition">Under krisen</router-link></li>
            <li><router-link to="/info/etter-krisen" class="text-blue-200 hover:text-white transition">Etter krisen</router-link></li>
          </ul>
        </div>

        <!-- Column 4: Contact -->
        <div>
          <h3 class="text-lg font-semibold mb-4">Kontakt</h3>
          <p class="text-blue-200 mb-2">Har du spørsmål eller tilbakemeldinger?</p>
          <a href="mailto:kontakt@krisefikser.no" class="text-blue-200 hover:text-white transition">kontakt@krisefikser.no</a>

          <!-- Show logout option for authenticated users -->
          <div v-if="authStore.isAuthenticated" class="mt-6">
            <button
              @click="authStore.logout"
              class="text-blue-200 hover:text-white transition flex items-center"
            >
              <LogOut class="h-4 w-4 mr-2" />
              Logg ut
            </button>
          </div>
        </div>
      </div>

      <div class="border-t border-blue-800 mt-8 pt-8 text-center text-blue-300">
        <p>&copy; 2025 Krisefikser.no. Alle rettigheter reservert.</p>
        <p class="mt-2">Et prosjekt utviklet ved NTNU</p>
      </div>
    </div>
  </footer>
</template>

<script lang="ts">
import { defineComponent } from 'vue';
import { useAuthStore } from '@/stores/useAuthStore';
import { LogOut } from 'lucide-vue-next';

export default defineComponent({
  name: 'AppFooter',
  components: {
    LogOut
  },
  setup() {
    const authStore = useAuthStore();
    return { authStore };
  }
});
</script>
