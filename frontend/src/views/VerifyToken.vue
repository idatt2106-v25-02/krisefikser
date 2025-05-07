<script setup lang="ts">
  import { ref, onMounted } from 'vue'
  import { useRoute, useRouter } from 'vue-router'
  import axios from 'axios'

  const route = useRoute()
  const router = useRouter()

  const token = ref(route.query.token || '')
  const loading = ref(false)
  const error = ref(false)
  const errorMessage = ref('')
  const verificationSuccess = ref(false)

  const redirectToLogin = () => {
    router.push({ path: '/logg-inn', query: { token: token.value } })
  }

  onMounted(async () => {
    if (!token.value) {
      error.value = true
      errorMessage.value = 'Token mangler i URL. Sjekk lenken eller prøv å be om en ny.'
      return
    }

    loading.value = true
    error.value = false
    verificationSuccess.value = false

    try {
      await new Promise(resolve => setTimeout(resolve, 1500))

      await axios.post(`http://localhost:8080/api/auth/verify-email?token=${token.value}`)

      verificationSuccess.value = true
    } catch (err: unknown) {
      error.value = true
      if (axios.isAxiosError(err) && err.response?.data?.message) {
        errorMessage.value = err.response.data.message;
      } else if (err instanceof Error && err.message.includes('Network Error')) {
        errorMessage.value = 'Nettverksfeil. Kunne ikke koble til serveren. Prøv igjen senere.';
      } else {
        errorMessage.value = 'Token er ugyldig, utløpt, eller noe gikk galt. Prøv igjen eller be om en ny bekreftelseslenke.'
      }
      console.error('Error during token verification:', err);
    } finally {
      loading.value = false
    }
  })
</script>

<template>
  <div class="min-h-screen flex flex-col items-center justify-center bg-gray-50 p-4 text-gray-800">
    <div class="bg-white shadow-2xl rounded-xl p-8 md:p-12 w-full max-w-lg text-center space-y-6">
      <img src="@/assets/logo.svg" alt="Company Logo" class="mx-auto h-16 w-auto mb-6" />
      <h1 class="text-3xl font-bold text-gray-900">E-postbekreftelse</h1>

      <div v-if="loading" class="space-y-4 py-4">
        <div class="animate-spin rounded-full h-12 w-12 border-t-4 border-b-4 border-blue-600 mx-auto"></div>
        <p class="text-lg font-medium text-blue-700">Validerer din token...</p>
        <p class="text-sm text-gray-500">Vennligst vent et øyeblikk, vi sjekker detaljene dine.</p>
      </div>

      <div v-if="!loading && error" class="space-y-5 py-4">
        <svg class="w-16 h-16 text-red-500 mx-auto" fill="none" viewBox="0 0 24 24" stroke-width="1.5" stroke="currentColor">
          <path stroke-linecap="round" stroke-linejoin="round" d="M12 9v3.75m0-10.036A11.959 11.959 0 013.598 6 11.99 11.99 0 003 9.75c0 5.592 3.824 10.29 9 11.622 5.176-1.332 9-6.03 9-11.622 0-1.31-.21-2.57-.598-3.75h-.152c-3.196 0-6.1-1.249-8.25-3.286zm0 13.036h.008v.008H12v-.008z" />
        </svg>
        <p class="text-2xl font-semibold text-red-700">Verifisering feilet</p>
        <p class="text-gray-600 px-4">{{ errorMessage }}</p>
        <div class="pt-4 space-y-3 sm:space-y-0 sm:flex sm:space-x-3 justify-center">
          <button @click="router.push('/registrer')" class="w-full sm:w-auto bg-red-600 hover:bg-red-700 text-white font-semibold py-3 px-6 rounded-lg shadow-md transition duration-150 ease-in-out">
            Prøv å registrere på nytt
          </button>
          <button @click="router.push('/logg-inn')" class="w-full sm:w-auto bg-gray-200 hover:bg-gray-300 text-gray-800 font-semibold py-3 px-6 rounded-lg shadow-md transition duration-150 ease-in-out">
            Gå til innlogging
          </button>
        </div>
      </div>

      <div v-if="!loading && !error && verificationSuccess" class="space-y-5 py-4">
        <svg class="w-16 h-16 text-green-500 mx-auto" fill="none" viewBox="0 0 24 24" stroke-width="1.5" stroke="currentColor">
          <path stroke-linecap="round" stroke-linejoin="round" d="M9 12.75L11.25 15 15 9.75M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
        </svg>
        <p class="text-2xl font-semibold text-green-700">E-post verifisert!</p>
        <p class="text-gray-600 px-4">Din e-postadresse er nå bekreftet. Du kan fortsette til innlogging.</p>
        <div class="pt-4">
          <button @click="redirectToLogin" class="w-full max-w-xs mx-auto bg-green-600 hover:bg-green-700 text-white font-semibold py-3 px-6 rounded-lg shadow-md transition duration-150 ease-in-out">
            Fortsett til innlogging
          </button>
        </div>
      </div>
    </div>
    <footer class="mt-10 text-center">
      <p class="text-sm text-gray-500">&copy; {{ new Date().getFullYear() }} Krisefikser™. Alle rettigheter reservert.</p>
    </footer>
  </div>
</template>

<style scoped>
/* Tailwind CSS is handling the styling. This block can be removed or used for highly specific styles not easily achievable with utility classes. */
/* For example:
@import url('https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap');

.min-h-screen {
  font-family: 'Inter', sans-serif;
}
*/
</style>
