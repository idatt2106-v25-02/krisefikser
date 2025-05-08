<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useForm } from 'vee-validate'
import { toTypedSchema } from '@vee-validate/zod'
import * as z from 'zod'
import { Mail, ArrowLeft, CheckCircle, ShieldCheck } from 'lucide-vue-next'
import AdminLayout from '@/components/admin/AdminLayout.vue'
import { useRequestPasswordReset } from '@/api/generated/authentication/authentication'
import { toast } from 'vue-sonner'

import { Button } from '@/components/ui/button'
import { FormControl, FormField, FormItem, FormLabel, FormMessage } from '@/components/ui/form'
import { Input } from '@/components/ui/input'

// Schema for the email form
const emailSchema = z.object({
  email: z.string().email('Vennligst skriv inn en gyldig e-postadresse')
})

// Set up form validation
const { handleSubmit, meta } = useForm({
  validationSchema: toTypedSchema(emailSchema),
})

const router = useRouter()
const isSubmitted = ref(false)
const isLoading = ref(false)
const userEmail = ref('')

const requestAdminPasswordResetMutation = useRequestPasswordReset()

const onSubmit = handleSubmit(async (values) => {
  isLoading.value = true

  try {
    await requestAdminPasswordResetMutation.mutateAsync({
      data: {
        email: values.email
      }
    })

    userEmail.value = values.email
    isSubmitted.value = true
    toast('Suksess', {
      description: 'Passord-tilbakestillingslink er sendt til admin-brukeren'
    })
  } catch (error: any) {
    console.error('Failed to request admin password reset:', error)
    toast('Feil', {
      description: error.response?.data?.message || 'Kunne ikke sende passord-tilbakestillingslink'
    })
  } finally {
    isLoading.value = false
  }
})

const goBack = () => {
  router.push('/admin')
}
</script>

<template>
  <AdminLayout>
    <div class="min-h-screen bg-gray-50 py-12">
      <div class="container mx-auto px-4">
        <!-- Back button -->
        <button @click="goBack" class="mb-6 text-blue-600 hover:text-blue-800 flex items-center">
          <ArrowLeft class="h-4 w-4 mr-1" />
          Tilbake til dashboard
        </button>

        <!-- Page header -->
        <div class="flex items-center mb-8">
          <ShieldCheck class="h-7 w-7 text-blue-600 mr-3" />
          <h1 class="text-2xl font-bold text-gray-800">Send passord-tilbakestillingslink</h1>
        </div>

        <!-- Super Admin notice -->
        <div class="bg-blue-50 border-l-4 border-blue-500 p-4 mb-6 max-w-md">
          <div class="flex">
            <div>
              <p class="font-medium text-blue-700">Super Admin funksjonalitet</p>
              <p class="text-sm text-blue-600">Du kan sende en ny passord-tilbakestillingslink til en annen admin.</p>
            </div>
          </div>
        </div>

        <div class="max-w-md mx-auto bg-white rounded-xl shadow-sm border border-gray-200 overflow-hidden">
          <div v-if="isSubmitted" class="p-6">
            <div class="text-center mb-4">
              <div class="inline-flex items-center justify-center h-12 w-12 rounded-full bg-green-100 mb-4">
                <CheckCircle class="h-6 w-6 text-green-600" />
              </div>
              <h2 class="text-xl font-bold text-gray-800 mb-2">Link sendt</h2>
              <p class="text-gray-600">
                En passord-tilbakestillingslink er sendt til
                <span class="font-medium">{{ userEmail }}</span>
              </p>
            </div>

            <div class="flex flex-col space-y-3 mt-6">
              <Button @click="isSubmitted = false" class="bg-blue-600 hover:bg-blue-700">
                Send til en annen admin
              </Button>
              <Button @click="goBack" variant="outline" class="border-gray-300 text-gray-700">
                Tilbake til dashboard
              </Button>
            </div>
          </div>

          <form v-else @submit="onSubmit" class="p-6">
            <h2 class="text-lg font-medium text-gray-800 mb-4">Send tilbakestillingslink til admin</h2>

            <div class="space-y-4">
              <FormField v-slot="{ componentField }" name="email">
                <FormItem>
                  <FormLabel class="block text-sm font-medium text-gray-700 mb-1">E-postadresse</FormLabel>
                  <FormControl>
                    <div class="relative">
                      <Mail class="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-500 h-4 w-4" />
                      <Input
                        type="email"
                        placeholder="admin@eksempel.no"
                        class="w-full pl-10 pr-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
                        v-bind="componentField"
                      />
                    </div>
                  </FormControl>
                  <FormMessage class="text-sm text-red-500" />
                </FormItem>
              </FormField>

              <p class="text-sm text-gray-500 italic">
                Mottakeren vil få en e-post med en link for å sette et nytt passord.
              </p>

              <Button
                type="submit"
                class="w-full bg-blue-600 hover:bg-blue-700 disabled:bg-gray-400 disabled:cursor-not-allowed"
                :disabled="!meta.valid || isLoading"
              >
                <span v-if="!isLoading">Send link</span>
                <span v-else class="flex items-center justify-center">
                  <svg class="animate-spin -ml-1 mr-2 h-4 w-4 text-white" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                    <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
                    <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                  </svg>
                  Sender...
                </span>
              </Button>
            </div>
          </form>
        </div>
      </div>
    </div>
  </AdminLayout>
</template>
