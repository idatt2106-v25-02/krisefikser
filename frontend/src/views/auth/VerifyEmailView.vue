<template>
  <div class="min-h-screen flex items-center justify-center bg-gray-50 py-12 px-4 sm:px-6 lg:px-8">
    <div class="max-w-md w-full space-y-8 p-10 bg-white shadow-lg rounded-xl">
      <div>
        <h2 class="mt-6 text-center text-3xl font-extrabold text-gray-900">
          Bekreft din e-postadresse
        </h2>
        <p class="mt-2 text-center text-sm text-gray-600">
          En e-post er sendt til din registrerte e-postadresse.
        </p>
        <p class="mt-2 text-center text-sm text-gray-600">
          Vennligst sjekk innboksen din (og spam-mappen) og klikk på bekreftelseslenken for å fullføre registreringen.
        </p>
      </div>
      <div class="mt-8 text-center">
        <router-link
          to="/logg-inn"
          class="font-medium text-blue-600 hover:text-blue-500"
        >
          Tilbake til innlogging
        </router-link>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
// Ingen spesifikk logikk trengs for denne visningen foreløpig
</script>

<style scoped>
/* Ingen spesifikke stiler trengs utover Tailwind */
</style>
