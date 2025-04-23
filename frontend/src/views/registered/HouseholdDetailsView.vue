<script setup lang="ts">
import { ref } from 'vue'
import { useForm } from 'vee-validate'
import { toTypedSchema } from '@vee-validate/zod'
import * as z from 'zod'
import { Button } from '@/components/ui/button'
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
} from '@/components/ui/dialog'
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
} from '@/components/ui/dropdown-menu'
import { Input } from '@/components/ui/input'
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select'
import { Form, FormControl, FormField, FormItem, FormLabel, FormMessage } from '@/components/ui/form'

interface ProductType {
  name: string
  unit: string
}

interface InventoryItem {
  name: string
  expiryDate: string
  amount: number
  productType: ProductType
}

interface Member {
  id: string
  name: string
  consumptionFactor: number
  email?: string
}

type MemberFormValues = {
  name: string
  email?: string
  consumptionFactor?: number
}

type ItemFormValues = {
  name: string
  amount: number
  expiryDate: string
  productType: ProductType
}

// Form schemas
const memberFormSchema = toTypedSchema(z.object({
  name: z.string().min(1, 'Navn er påkrevd'),
  email: z.string().email('Ugyldig e-postadresse').optional(),
  consumptionFactor: z.number().min(0.1, 'Må være større enn 0').optional()
}))

const itemFormSchema = toTypedSchema(z.object({
  name: z.string().min(1, 'Navn er påkrevd'),
  amount: z.number().min(0.1, 'Må være større enn 0'),
  expiryDate: z.string().min(1, 'Utløpsdato er påkrevd'),
  productType: z.object({
    name: z.string(),
    unit: z.string()
  })
}))

// Mock API response for a single household
const apiResponse = ref({
  household: {
    id: '1',
    name: 'Familien Hansen',
    address: 'Kongens gate 1, 0153 Oslo',
    members: [
      { id: '1', name: 'Erik Hansen', consumptionFactor: 1, email: 'erik@example.com' },
      { id: '2', name: 'Maria Hansen', consumptionFactor: 1, email: 'maria@example.com' },
      { id: '3', name: 'Lars Hansen', consumptionFactor: 0.5 }
    ] as Member[],
    inventory: [
      {
        name: 'Melk',
        expiryDate: '2024-04-10',
        amount: 2,
        productType: {
          name: 'Meieriprodukter',
          unit: 'liter'
        }
      },
      {
        name: 'Brød',
        expiryDate: '2024-04-05',
        amount: 1,
        productType: {
          name: 'Bakevarer',
          unit: 'stk'
        }
      },
      {
        name: 'Pasta',
        expiryDate: '2025-01-15',
        amount: 3,
        productType: {
          name: 'Tørrvarer',
          unit: 'pakke'
        }
      }
    ]
  }
})

// Mock product types
const productTypes = ref<ProductType[]>([
  { name: 'Meieriprodukter', unit: 'liter' },
  { name: 'Bakevarer', unit: 'stk' },
  { name: 'Tørrvarer', unit: 'pakke' },
  { name: 'Kjøtt', unit: 'gram' },
  { name: 'Grønnsaker', unit: 'kg' }
])

// State for dialogs
const isAddMemberDialogOpen = ref(false)
const isAddItemDialogOpen = ref(false)
const memberMode = ref<'invite' | 'add'>('add')

// Form handling
const { handleSubmit: submitMemberForm } = useForm<MemberFormValues>({
  validationSchema: memberFormSchema
})

const { handleSubmit: submitItemForm } = useForm<ItemFormValues>({
  validationSchema: itemFormSchema
})

// Actions
function onMemberSubmit(values: MemberFormValues) {
  const member: Member = {
    id: Math.random().toString(),
    name: values.name,
    consumptionFactor: memberMode.value === 'invite' ? 1 : (values.consumptionFactor || 1),
    ...(memberMode.value === 'invite' && values.email ? { email: values.email } : {})
  }
  apiResponse.value.household.members.push(member)
  isAddMemberDialogOpen.value = false
}

function onItemSubmit(values: ItemFormValues) {
  const item: InventoryItem = {
    name: values.name,
    amount: values.amount,
    expiryDate: values.expiryDate,
    productType: values.productType
  }
  apiResponse.value.household.inventory.push(item)
  isAddItemDialogOpen.value = false
}

function handleDeleteMember(memberId: string) {
  apiResponse.value.household.members = apiResponse.value.household.members.filter(
    m => m.id !== memberId
  )
}
</script>

<template>
  <div class="container mx-auto px-4 py-8">
    <div class="mb-8">
      <h1 class="text-3xl font-bold text-gray-900 mb-2">{{ apiResponse.household.name }}</h1>
      <p class="text-gray-600">{{ apiResponse.household.address }}</p>
    </div>

    <!-- Members Section -->
    <div class="mb-12">
      <div class="flex justify-between items-center mb-4">
        <h2 class="text-2xl font-semibold text-gray-800">Medlemmer</h2>
        <Dialog v-model:open="isAddMemberDialogOpen">
          <DialogTrigger as-child>
            <Button variant="outline">Legg til medlem</Button>
          </DialogTrigger>
          <DialogContent class="sm:max-w-[425px]">
            <DialogHeader>
              <DialogTitle>Legg til medlem</DialogTitle>
              <DialogDescription>
                Velg om du vil invitere en bruker via e-post eller legge til et medlem uten konto.
              </DialogDescription>
            </DialogHeader>
            <div class="grid gap-4 py-4">
              <div class="flex items-center gap-4">
                <Button
                  :variant="memberMode === 'invite' ? 'default' : 'outline'"
                  @click="memberMode = 'invite'"
                >
                  Inviter via e-post
                </Button>
                <Button
                  :variant="memberMode === 'add' ? 'default' : 'outline'"
                  @click="memberMode = 'add'"
                >
                  Legg til uten konto
                </Button>
              </div>
              <Form @submit="submitMemberForm(onMemberSubmit)">
                <FormField name="name">
                  <FormItem>
                    <FormLabel>Navn</FormLabel>
                    <FormControl>
                      <Input placeholder="Skriv inn navn" />
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                </FormField>

                <FormField v-if="memberMode === 'invite'" name="email">
                  <FormItem>
                    <FormLabel>E-post</FormLabel>
                    <FormControl>
                      <Input type="email" placeholder="navn@example.com" />
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                </FormField>

                <FormField v-else name="consumptionFactor">
                  <FormItem>
                    <FormLabel>Forbruksfaktor</FormLabel>
                    <FormControl>
                      <Input
                        type="number"
                        step="0.1"
                        min="0"
                      />
                    </FormControl>
                    <FormMessage>
                      0.5 for halv porsjon, 1 for normal porsjon, osv.
                    </FormMessage>
                  </FormItem>
                </FormField>

                <Button type="submit" class="mt-4">Legg til</Button>
              </Form>
            </div>
          </DialogContent>
        </Dialog>
      </div>

      <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
        <div
          v-for="member in apiResponse.household.members"
          :key="member.id"
          class="bg-white rounded-lg p-4 border border-gray-200 flex justify-between items-center"
        >
          <div>
            <p class="text-gray-800 font-medium">{{ member.name }}</p>
            <p v-if="member.consumptionFactor" class="text-sm text-gray-500">
              Forbruksfaktor: {{ member.consumptionFactor }}
            </p>
            <p v-if="member.email" class="text-sm text-gray-500">
              {{ member.email }}
            </p>
          </div>
          <DropdownMenu>
            <DropdownMenuTrigger as-child>
              <Button variant="ghost" size="icon">
                <span class="sr-only">Åpne meny</span>
                <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="h-4 w-4">
                  <circle cx="12" cy="12" r="1"/><circle cx="12" cy="5" r="1"/><circle cx="12" cy="19" r="1"/>
                </svg>
              </Button>
            </DropdownMenuTrigger>
            <DropdownMenuContent>
              <DropdownMenuItem @click="handleDeleteMember(member.id)">
                Slett
              </DropdownMenuItem>
            </DropdownMenuContent>
          </DropdownMenu>
        </div>
      </div>
    </div>

    <!-- Inventory Section -->
    <div>
      <div class="flex justify-between items-center mb-4">
        <h2 class="text-2xl font-semibold text-gray-800">Inventar</h2>
        <Dialog v-model:open="isAddItemDialogOpen">
          <DialogTrigger as-child>
            <Button variant="outline">Legg til vare</Button>
          </DialogTrigger>
          <DialogContent class="sm:max-w-[425px]">
            <DialogHeader>
              <DialogTitle>Legg til vare</DialogTitle>
              <DialogDescription>
                Fyll ut informasjon om varen du vil legge til.
              </DialogDescription>
            </DialogHeader>
            <div class="grid gap-4 py-4">
              <Form @submit="submitItemForm(onItemSubmit)">
                <FormField name="productType">
                  <FormItem>
                    <FormLabel>Produkttype</FormLabel>
                    <Select>
                      <FormControl>
                        <SelectTrigger>
                          <SelectValue placeholder="Velg produkttype" />
                        </SelectTrigger>
                      </FormControl>
                      <SelectContent>
                        <SelectItem
                          v-for="type in productTypes"
                          :key="type.name"
                          :value="type"
                        >
                          {{ type.name }} ({{ type.unit }})
                        </SelectItem>
                      </SelectContent>
                    </Select>
                    <FormMessage />
                  </FormItem>
                </FormField>

                <FormField name="name">
                  <FormItem>
                    <FormLabel>Navn på vare</FormLabel>
                    <FormControl>
                      <Input placeholder="F.eks. TINE Lettmelk" />
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                </FormField>

                <FormField name="amount">
                  <FormItem>
                    <FormLabel>Mengde</FormLabel>
                    <FormControl>
                      <Input
                        type="number"
                        step="0.1"
                        min="0"
                      />
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                </FormField>

                <FormField name="expiryDate">
                  <FormItem>
                    <FormLabel>Utløpsdato</FormLabel>
                    <FormControl>
                      <Input type="date" />
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                </FormField>

                <Button type="submit" class="mt-4">Legg til</Button>
              </Form>
            </div>
          </DialogContent>
        </Dialog>
      </div>

      <div class="overflow-x-auto">
        <table class="min-w-full bg-white">
          <thead class="bg-gray-50">
            <tr>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Produkt</th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Type</th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Mengde</th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Utløpsdato</th>
            </tr>
          </thead>
          <tbody class="divide-y divide-gray-200">
            <tr
              v-for="item in apiResponse.household.inventory"
              :key="item.name"
              class="hover:bg-gray-50"
            >
              <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900">{{ item.name }}</td>
              <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">{{ item.productType.name }}</td>
              <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">{{ item.amount }} {{ item.productType.unit }}</td>
              <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">{{ item.expiryDate }}</td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  </div>
</template>
