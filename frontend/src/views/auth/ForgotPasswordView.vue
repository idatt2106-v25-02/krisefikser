<script setup lang="ts">
import { ref } from 'vue'
import { useForm } from 'vee-validate'
import { toTypedSchema } from '@vee-validate/zod'
import * as z from 'zod'
import { Mail, ArrowRight, CheckCircle } from 'lucide-vue-next'

import { Button } from '@/components/ui/button'
import { FormControl, FormField, FormItem, FormLabel, FormMessage } from '@/components/ui/form'
import { Input } from '@/components/ui/input'
import { Card, CardContent, CardDescription, CardFooter, CardHeader, CardTitle } from '@/components/ui/card'

// Skjema for glemt passord
const forgotPasswordSchema = z.object({
  email: z.string().email('Vennligst skriv inn en gyldig e-postadresse')
})

// Sett opp skjema-validering
const { handleSubmit, meta } = useForm({
  validationSchema: toTypedSchema(forgotPasswordSchema),
})

// Spor tilstanden for skjema-innsending
const isSubmitted = ref(false)
const isLoading = ref(false)
const userEmail = ref('')

const onSubmit = handleSubmit((values) => {
  isLoading.value = true

  // Simuler API-kall
  setTimeout(() => {
    userEmail.value = values.email
    isSubmitted.value = true
    isLoading.value = false
  }, 1500)
})
</script>

<template>
  <div class="min-h-screen flex items-center justify-center bg-white p-4">
    <Card class="w-full max-w-md border border-gray-200 shadow-sm">
      <CardHeader class="space-y-1">
        <CardTitle class="text-2xl font-bold text-center">Glemt Passord</CardTitle>
        <CardDescription class="text-center text-gray-500" v-if="!isSubmitted">
          Skriv inn e-postadressen din, så sender vi deg en lenke for å tilbakestille passordet ditt.
        </CardDescription>
      </CardHeader>

      <CardContent>
        <!-- Suksess-tilstand -->
        <div v-if="isSubmitted" class="space-y-4">
          <Alert class="bg-green-50 border-green-200">
            <CheckCircle class="h-5 w-5 text-green-500" />
            <AlertDescription class="mt-2">
              Vi har sendt en lenke for å tilbakestille passordet til <span class="font-medium">{{ userEmail }}</span>.
              Vennligst sjekk e-posten din og følg instruksjonene.
            </AlertDescription>
          </Alert>

        <Button class="w-full mt-4 bg-blue-600 text-white hover:bg-blue-700" @click="isSubmitted = false">
            Prøv en annen e-post
          </Button>
        </div>

        <!-- Skjema-tilstand -->
        <form v-else @submit="onSubmit" class="space-y-4">
          <FormField v-slot="{ componentField }" name="email">
            <FormItem>
              <FormLabel class="block text-sm font-medium text-gray-700">E-postadresse</FormLabel>
              <FormControl>
                <div class="relative">
                  <Mail class="absolute left-3 top-1/2 -translate-y-1/2 h-5 w-5 text-gray-400" />
                  <Input
                    type="email"
                    placeholder="navn@eksempel.com"
                    class="w-full pl-10 pr-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
                    v-bind="componentField"
                  />
                </div>
              </FormControl>
              <FormMessage class="text-sm text-red-500" />
            </FormItem>
          </FormField>

          <Button
            type="submit"
            class="w-full bg-blue-600 hover:bg-blue-700 disabled:bg-gray-400 disabled:cursor-not-allowed text-white py-2 rounded-md text-sm font-medium"
            :disabled="!meta.valid || isLoading"
          >
            <span v-if="!isLoading">Tilbakestill Passord</span>
            <span v-else class="flex items-center justify-center">
              <svg class="animate-spin -ml-1 mr-2 h-4 w-4 text-white" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
                <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
              </svg>
              Behandler...
            </span>
            <ArrowRight class="ml-2 h-4 w-4" v-if="!isLoading" />
          </Button>
        </form>
      </CardContent>

      <CardFooter class="flex justify-center">
        <div class="text-sm text-center">
          <span class="text-gray-600">Husker du passordet ditt?</span>
          <a href="/logg-inn" class="ml-1 text-blue-600 hover:underline">Tilbake til innlogging</a>
        </div>
      </CardFooter>
    </Card>
  </div>
</template>
