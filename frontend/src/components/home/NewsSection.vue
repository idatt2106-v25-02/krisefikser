<template>
  <section>
    <div class="text-center mb-12">
      <h2 class="text-3xl font-bold text-gray-800 mb-4">Aktuelle nyheter</h2>
      <p class="text-xl text-gray-600 max-w-3xl mx-auto">Hold deg oppdatert på nyheter og hendelser relatert til beredskap.</p>
    </div>

    <div class="grid md:grid-cols-3 gap-8">
      <!-- News Item 1 -->
      <div class="bg-white rounded-lg shadow overflow-hidden">
        <div class="h-48 bg-gray-200"></div>
        <div class="p-6">
          <p class="text-sm text-gray-500 mb-2">22. april 2025</p>
          <h3 class="text-xl font-semibold text-gray-800 mb-2">Ny rapport om beredskap i norske hjem</h3>
          <p class="text-gray-600 mb-4">Myndighetene har utgitt en ny rapport om beredskap i norske hjem. Rapporten viser at mange fortsatt mangler viktige forsyninger.</p>
          <router-link to="/news/1" class="text-blue-600 font-medium hover:underline">Les mer</router-link>
        </div>
      </div>

      <!-- News Item 2 -->
      <div class="bg-white rounded-lg shadow overflow-hidden">
        <div class="h-48 bg-gray-200"></div>
        <div class="p-6">
          <p class="text-sm text-gray-500 mb-2">20. april 2025</p>
          <h3 class="text-xl font-semibold text-gray-800 mb-2">Beredskapsveiledning for sommeren</h3>
          <p class="text-gray-600 mb-4">Sommerens ekstremvær kan skape utfordringer. Lær hvordan du kan forberede deg og din familie på eventuelle hendelser.</p>
          <router-link to="/news/2" class="text-blue-600 font-medium hover:underline">Les mer</router-link>
        </div>
      </div>

      <!-- News Item 3 -->
      <div class="bg-white rounded-lg shadow overflow-hidden">
        <div class="h-48 bg-gray-200"></div>
        <div class="p-6">
          <p class="text-sm text-gray-500 mb-2">18. april 2025</p>
          <h3 class="text-xl font-semibold text-gray-800 mb-2">Nye tilfluktsrom åpnet i Oslo</h3>
          <p class="text-gray-600 mb-4">Oslo kommune har åpnet flere nye tilfluktsrom som del av en større beredskapsplan. Finn ditt nærmeste tilfluktsrom på kartet.</p>
          <router-link to="/news/3" class="text-blue-600 font-medium hover:underline">Les mer</router-link>
        </div>
      </div>
    </div>
  </section>
</template>

<script lang="ts">
export default {
  name: 'NewsSection'
}
</script>
