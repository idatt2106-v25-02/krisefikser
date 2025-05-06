<script setup>
  import { ref, onMounted } from 'vue'
  import { useRoute, useRouter } from 'vue-router'
  import axios from 'axios'

  const route = useRoute()
  const router = useRouter()

  const token = ref(route.query.token || '')
  const loading = ref(false)
  const error = ref(false)
  const errorMessage = ref('')
  const response = ref('')

  onMounted(async () => {
    if (!token.value) {
      error.value = true
      errorMessage.value = 'Token mangler i URL.'
      return
    }
//{{baseUrl}}/api/auth/verify-email?token={{verificationToken}}
    loading.value = true
    try {

      // Add a small delay to show loading state
      await new Promise(resolve => setTimeout(resolve, 1500))

     await axios.post(`http://localhost:8080/api/auth/verify-email?token=${token.value}`)

      // Hvis OK → redirect til nytt passord-skjema med token
      router.push({ path: '/logg-inn', query: { token: token.value } })
    } catch (err) {
      error.value = true
      errorMessage.value = 'Token er ugyldig eller utløpt.'
      console.error('Error during token verification:', err);
    } finally {
      loading.value = false
    }
  })
  </script>

  <style scoped>
  .token-verifier {
    max-width: 600px;
    margin: auto;
    padding: 1rem;
  }
  .error {
    color: red;
  }
  </style>

<template>
  <div class="token-verifier">
  <p v-if="loading">Validerer token...</p>
  {{token}}
  <p v-if="error" class="error">{{ errorMessage }}</p>
  {{response}}
  </div>
</template>
