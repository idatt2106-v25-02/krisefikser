<script lang="ts">
export default {
  name: 'CrisisInfoSection'
}
</script>
<template>
  <section class="mb-16">
    <div class="text-center mb-12">
      <h2 class="text-3xl font-bold text-gray-800 mb-4">Generell kriseinformasjon</h2>
      <p class="text-xl text-gray-600 max-w-3xl mx-auto">
        Lær om hva du bør gjøre før, under og etter en krise for å være best mulig forberedt.
        Hver seksjon inneholder også interaktive quizer for å teste dine kunnskaper.
      </p>
    </div>

    <div class="grid md:grid-cols-3 gap-8">
      <!-- Før krisen -->
      <div class="bg-white p-6 rounded-lg shadow-md transition hover:shadow-lg relative">
        <!-- Quiz badge positioned at top right -->
        <span class="absolute top-2 right-2 bg-blue-100 text-blue-800 text-sm font-medium px-2.5 py-0.5 rounded-full flex items-center">
          <svg xmlns="http://www.w3.org/2000/svg" class="h-3.5 w-3.5 mr-1" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
          </svg>
          Inneholder quiz!
        </span>

        <div class="bg-blue-100 inline-block p-3 rounded-full mb-4">
          <svg xmlns="http://www.w3.org/2000/svg" class="h-8 w-8 text-blue-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 6v6m0 0v6m0-6h6m-6 0H6" />
          </svg>
        </div>
        <h3 class="text-xl font-semibold text-gray-800 mb-2">Før krisen</h3>
        <p class="text-gray-600 mb-4">Lær hvordan du kan forberede deg selv og din husstand før en krise inntreffer. Opprett beredskapslager og lær om viktige forhåndsregler.</p>
        <router-link to="/info/for-krisen" class="text-blue-600 font-medium hover:underline inline-flex items-center">
          Les mer
          <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 ml-1" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7" />
          </svg>
        </router-link>
      </div>

      <!-- Under krisen -->
      <div class="bg-white p-6 rounded-lg shadow-md transition hover:shadow-lg relative">
        <!-- Quiz badge positioned at top right -->
        <span class="absolute top-2 right-2 bg-yellow-100 text-yellow-800 text-sm font-medium px-2.5 py-0.5 rounded-full flex items-center">
          <svg xmlns="http://www.w3.org/2000/svg" class="h-3.5 w-3.5 mr-1" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
          </svg>
           Inneholder quiz!
        </span>

        <div class="bg-yellow-100 inline-block p-3 rounded-full mb-4">
          <svg xmlns="http://www.w3.org/2000/svg" class="h-8 w-8 text-yellow-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
          </svg>
        </div>
        <h3 class="text-xl font-semibold text-gray-800 mb-2">Under krisen</h3>
        <p class="text-gray-600 mb-4">Få tilgang til kritisk informasjon og råd om hvordan du bør handle når en krise oppstår. Finn nærmeste tilfluktsrom og hjelperessurser.</p>
        <router-link to="/info/under-krisen" class="text-blue-600 font-medium hover:underline inline-flex items-center">
          Les mer
          <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 ml-1" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7" />
          </svg>
        </router-link>
      </div>

      <!-- Etter krisen -->
      <div class="bg-white p-6 rounded-lg shadow-md transition hover:shadow-lg relative">
        <!-- Quiz badge positioned at top right -->
        <span class="absolute top-2 right-2 bg-green-100 text-green-800 text-sm font-medium px-2.5 py-0.5 rounded-full flex items-center">
          <svg xmlns="http://www.w3.org/2000/svg" class="h-3.5 w-3.5 mr-1" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
          </svg>
 Inneholder quiz!        </span>

        <div class="bg-green-100 inline-block p-3 rounded-full mb-4">
          <svg xmlns="http://www.w3.org/2000/svg" class="h-8 w-8 text-green-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7" />
          </svg>
        </div>
        <h3 class="text-xl font-semibold text-gray-800 mb-2">Etter krisen</h3>
        <p class="text-gray-600 mb-4">Veiledning for gjenoppbygging og normalisering etter en krise. Lær om hvordan du kan evaluere og forbedre din beredskap for fremtiden.</p>
        <router-link to="/info/etter-krisen" class="text-blue-600 font-medium hover:underline inline-flex items-center">
          Les mer
          <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 ml-1" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7" />
          </svg>
        </router-link>
      </div>
    </div>
  </section>
</template>
