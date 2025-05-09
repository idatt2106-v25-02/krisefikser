<script setup lang="ts">
import { useRouter } from 'vue-router'
import { useForm } from 'vee-validate'
import { toTypedSchema } from '@vee-validate/zod'
import * as z from 'zod'
import { Mail, ArrowLeft, CheckCircle } from 'lucide-vue-next'
import AdminLayout from '@/components/admin/AdminLayout.vue'
import { useToast } from '@/components/ui/toast/use-toast'
import { useInviteAdmin } from '@/api/generated/authentication/authentication'

import { Button } from '@/components/ui/button'
import { FormControl, FormField, FormItem, FormLabel, FormMessage } from '@/components/ui/form'
import { Input } from '@/components/ui/input'
import {
  Card,
  CardContent,
  CardDescription,
  CardFooter,
  CardHeader,
  CardTitle,
} from '@/components/ui/card'

const emailSchema = z.object({
  email: z.string().email('Vennligst skriv inn en gyldig e-postadresse'),
})

const { handleSubmit, meta,  } = useForm({
  validationSchema: toTypedSchema(emailSchema),
})

const router = useRouter()
const { toast } = useToast()

const {
  mutate: inviteAdminMutation,
  isPending,
  isSuccess,
  variables
} = useInviteAdmin({
  mutation: {
    onError: () => {
      toast({
        title: 'Feil',
        description: 'Kunne ikke sende invitasjon. Vennligst prøv igjen.',
        variant: 'destructive',
      })
    },
  },
})

const onSubmit = handleSubmit(async (values) => {
  inviteAdminMutation({
    data: values,
  })
})

</script>

<template>
  <AdminLayout>
    <div class="min-h-screen flex items-center justify-center bg-white p-4">
      <Card class="w-full max-w-md border border-gray-200 shadow-sm">
        <CardHeader class="space-y-1">
          <CardTitle class="text-2xl font-bold text-center">Inviter ny admin</CardTitle>
          <CardDescription class="text-center text-gray-500">
            Skriv inn e-postadressen til brukeren du ønsker å invitere som admin.
          </CardDescription>
        </CardHeader>

        <CardContent>
          <!-- Success state -->
          <div v-if="isSuccess" class="space-y-4">
            <div class="bg-green-50 border border-green-200 rounded-lg p-4">
              <div class="flex items-center">
                <CheckCircle class="h-5 w-5 text-green-500 mr-2" />
                <div>
                  <p class="font-medium text-green-700">Invitasjon sendt!</p>
                  <p class="text-sm text-green-600">
                    En invitasjon har blitt sendt til
                      <span class="font-medium">{{ variables?.data.email }}</span>.
                  </p>
                </div>
              </div>
            </div>

            <Button
              class="w-full mt-4 bg-blue-600 text-white hover:bg-blue-700"
              @click="router.replace('/admin/invite')"
            >
              Inviter en annen bruker
            </Button>
          </div>

          <!-- Form state -->
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
              :disabled="!meta.valid || isPending"
            >
              <span v-if="!isPending">Send invitasjon</span>
              <span v-else class="flex items-center justify-center">
                <svg
                  class="animate-spin -ml-1 mr-2 h-4 w-4 text-white"
                  xmlns="http://www.w3.org/2000/svg"
                  fill="none"
                  viewBox="0 0 24 24"
                >
                  <circle
                    class="opacity-25"
                    cx="12"
                    cy="12"
                    r="10"
                    stroke="currentColor"
                    stroke-width="4"
                  ></circle>
                  <path
                    class="opacity-75"
                    fill="currentColor"
                    d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"
                  ></path>
                </svg>
                Sender...
              </span>
            </Button>
          </form>
        </CardContent>

        <CardFooter class="flex justify-center">
          <Button variant="ghost" @click="router.push('/admin')" class="text-sm text-gray-600 hover:text-gray-800">
            <ArrowLeft class="h-4 w-4 mr-1" />
            Tilbake til admin dashboard
          </Button>
        </CardFooter>
      </Card>
    </div>
  </AdminLayout>
</template>
