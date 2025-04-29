<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
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
} from '@/components/ui/dialog'
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
} from '@/components/ui/dropdown-menu'
import { Input } from '@/components/ui/input'
import { Form, FormControl, FormField, FormItem, FormLabel, FormMessage } from '@/components/ui/form'
import { MapPin, ExternalLink, Trash, UserMinus, AlertCircle, Map as MapIcon } from 'lucide-vue-next'
import HouseholdMeetingMap from '@/components/household/HouseholdMeetingMap.vue'
import HouseholdEmergencySupplies from '@/components/household/HouseholdEmergencySupplies.vue'


interface Member {
  id: string
  name: string
  consumptionFactor?: number
  email?: string
}

interface MeetingPlace {
  id: string;
  name: string;
  address: string;
  position: [number, number]; // [latitude, longitude]
  description?: string;
  type: 'primary' | 'secondary'; // Primary or secondary meeting place
}

type MemberFormValues = {
  name: string
  email?: string
  consumptionFactor?: number
}

// Form schemas
const memberFormSchema = toTypedSchema(z.object({
  name: z.string().min(1, 'Navn er påkrevd'),
  email: z.string().email('Ugyldig e-postadresse').optional(),
  consumptionFactor: z.number().min(0.1, 'Må være større enn 0').optional()
}))

// Updated mock API response for the active household (Familien Hansen)
const apiResponse = ref({
  household: {
    id: '1',
    name: 'Familien Hansen',
    address: 'Kongens gate 1, 0153 Oslo',
    position: [59.9127, 10.7461] as [number, number], // Oslo coordinates
    members: [
      { id: '1', name: 'Erik Hansen', consumptionFactor: 1 },
      { id: '2', name: 'Maria Hansen', consumptionFactor: 1 },
      { id: '3', name: 'Lars Hansen', consumptionFactor: 1 },
      { id: '4', name: 'Mona Hansen', consumptionFactor: 1 }
    ] as Member[],
    inventory: {
      food: { current: 18, target: 60, unit: 'kg' },
      water: { current: 60, target: 160, unit: 'L' },
      other: { current: 12, target: 22 },
      preparedDays: 3,
      targetDays: 7
    },
    inventoryItems: [
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
      }
    ],
    meetingPlaces: [
      {
        id: 'mp1',
        name: 'Hovedmøteplass: Rådhusplassen',
        address: 'Rådhusplassen 1, 0037 Oslo',
        position: [59.9125, 10.7342],
        description: 'Samlingssted ved krisesituasjoner. Møt opp ved fontenen.',
        type: 'primary'
      },
      {
        id: 'mp2',
        name: 'Alternativ møteplass: Frognerparken',
        address: 'Kirkeveien 20, 0368 Oslo',
        position: [59.9274, 10.7002],
        description: 'Sekundær møteplass ved krisesituasjoner. Oppmøte ved hovedinngangen.',
        type: 'secondary'
      }
    ] as MeetingPlace[]
  }
})

const router = useRouter()

// State for dialogs
const isAddMemberDialogOpen = ref(false)
const isMeetingMapDialogOpen = ref(false)
const memberMode = ref<'invite' | 'add'>('add')
const mapRef = ref<InstanceType<typeof HouseholdMeetingMap> | null>(null)
const selectedMeetingPlace = ref<MeetingPlace | null>(null)

// Form handling
const { handleSubmit: submitMemberForm } = useForm<MemberFormValues>({
  validationSchema: memberFormSchema
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

function handleDeleteMember(memberId: string) {
  apiResponse.value.household.members = apiResponse.value.household.members.filter(
    m => m.id !== memberId
  )
}

function navigateToInventory() {
  router.push(`/husstand/${apiResponse.value.household.id}/beredskapslager`)
}

function leaveHousehold() {
  // In a real app, this would make an API call
  console.log('Forlat husstand clicked')
}

function deleteHousehold() {
  // In a real app, this would make an API call
  console.log('Slett husstand clicked')
}

function handleMeetingPlaceSelected(place: MeetingPlace) {
  selectedMeetingPlace.value = place
}

function viewMeetingPlace(placeId: string) {
  isMeetingMapDialogOpen.value = true

  // Wait for dialog to open and map to initialize
  setTimeout(() => {
    if (mapRef.value) {
      mapRef.value.centerOnMeetingPlace(placeId)
    }
  }, 300)
}
</script>

<template>
  <div class="bg-gray-50 min-h-screen">
    <div class="max-w-8xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      <!-- Household header -->
      <div class="bg-white rounded-lg shadow-sm border border-gray-200 p-6 mb-6">
        <div class="flex flex-col md:flex-row md:items-center md:justify-between">
          <div>
            <h1 class="text-4xl font-bold text-gray-900 mb-1">{{ apiResponse.household.name }}</h1>
            <div class="flex items-center text-gray-600">
              <MapPin class="h-4 w-4 text-gray-400 mr-1" />
              <span>{{ apiResponse.household.address }}</span>
              <ExternalLink class="h-4 w-4 ml-1 text-gray-400 cursor-pointer" />
            </div>
          </div>
          <div class="mt-4 md:mt-0 space-x-2">
            <Button variant="outline" size="sm" @click="navigateToInventory">
              Endre informasjon
            </Button>
          </div>
        </div>
      </div>

      <!-- Main content grid -->
      <div class="grid grid-cols-1 lg:grid-cols-12 gap-6">
        <!-- Members Section -->
        <div class="lg:col-span-8">
          <div class="bg-white rounded-lg shadow-sm border border-gray-200 p-6 mb-6">
            <div class="flex justify-between items-center mb-5">
              <h2 class="text-xl font-semibold text-gray-800">Medlemmer</h2>
              <Button
                variant="outline"
                size="sm"
                @click="isAddMemberDialogOpen = true"
                class="flex items-center gap-1"
              >
                <span class="text-md">+</span>
                <span>Legg til</span>
              </Button>
            </div>

            <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
              <!-- Member cards -->
              <div
                v-for="member in apiResponse.household.members"
                :key="member.id"
                class="bg-gray-50 rounded-lg border border-gray-200 overflow-hidden hover:shadow-md transition-shadow"
              >
                <div class="p-4">
                  <div class="flex justify-between items-start">
                    <div class="flex items-center mb-3">
                      <div class="h-10 w-10 bg-blue-100 rounded-full flex items-center justify-center text-blue-600 mr-3 flex-shrink-0">
                        <span class="text-md font-medium">{{ member.name.charAt(0) }}</span>
                      </div>
                      <h3 class="text-md font-bold text-gray-900">{{ member.name }}</h3>
                    </div>
                    <DropdownMenu>
                      <DropdownMenuTrigger as-child>
                        <Button variant="ghost" size="icon" class="h-8 w-8">
                          <span class="sr-only">Medlemsalternativer</span>
                          <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="text-gray-400">
                            <circle cx="12" cy="12" r="1"/><circle cx="12" cy="5" r="1"/><circle cx="12" cy="19" r="1"/>
                          </svg>
                        </Button>
                      </DropdownMenuTrigger>
                      <DropdownMenuContent align="end">
                        <DropdownMenuItem @click="handleDeleteMember(member.id)" class="text-red-600">
                          <UserMinus class="h-4 w-4 mr-2" />
                          Fjern medlem
                        </DropdownMenuItem>
                      </DropdownMenuContent>
                    </DropdownMenu>
                  </div>

                  <div class="text-sm text-gray-600">
                    <div v-if="member.consumptionFactor" class="mt-1">
                      Forbruksfaktor: <span class="font-medium">{{ member.consumptionFactor }}</span>
                    </div>
                    <div v-if="member.email" class="mt-1 truncate">
                      {{ member.email }}
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- Emergency Supplies Section -->
          <div class="bg-white rounded-lg shadow-sm border border-gray-200 p-6">
            <div class="flex justify-between items-center mb-5">
              <h2 class="text-xl font-semibold text-gray-800">Beredskapslager</h2>
              <Button
                variant="outline"
                size="sm"
                @click="navigateToInventory"
                class="flex items-center gap-1"
              >
                <span>Se detaljer</span>
              </Button>
            </div>

            <HouseholdEmergencySupplies
              :inventory="apiResponse.household.inventory"
              :inventory-items="apiResponse.household.inventoryItems"
              :household-id="apiResponse.household.id"
              :show-details-button="false"
            />
          </div>
        </div>

        <!-- Sidebar content -->
        <div class="lg:col-span-4 space-y-6">
          <!-- Meeting Places Section -->
          <div class="bg-white rounded-lg shadow-sm border border-gray-200">
            <div class="p-5 border-b">
              <div class="flex justify-between items-center">
                <h2 class="text-xl font-semibold text-gray-800">Møteplasser</h2>
                <Button
                  variant="outline"
                  size="sm"
                  class="flex items-center gap-1"
                  @click="isMeetingMapDialogOpen = true"
                >
                  <MapIcon class="h-4 w-4" />
                  <span>Vis kart</span>
                </Button>
              </div>
            </div>

            <div class="divide-y divide-gray-100">
              <div
                v-for="(place, index) in apiResponse.household.meetingPlaces"
                :key="place.id"
                class="p-4"
              >
                <div class="flex items-start">
                  <div class="flex-shrink-0 mr-3">
                    <div :class="[
                      'h-10 w-10 rounded-full flex items-center justify-center',
                      place.type === 'primary' ? 'bg-red-500' : 'bg-orange-400'
                    ]">
                      <AlertCircle class="h-5 w-5 text-white" />
                    </div>
                  </div>
                  <div class="flex-1">
                    <h4 class="font-medium text-gray-800">{{ place.name }}</h4>
                    <p class="text-sm text-gray-600">{{ place.address }}</p>
                    <button
                      class="mt-2 text-sm text-blue-600 hover:text-blue-800 flex items-center"
                      @click="viewMeetingPlace(place.id)"
                    >
                      <MapIcon class="h-3 w-3 mr-1" />
                      Vis i kart
                    </button>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- Action buttons in a card -->
          <div class="bg-white rounded-lg shadow-sm border border-gray-200 p-6">
            <h2 class="text-xl font-semibold text-gray-800 mb-4">Husstandshandlinger</h2>
            <div class="space-y-3">
              <button
                @click="leaveHousehold"
                class="w-full bg-white border border-gray-300 text-gray-700 py-2 px-4 rounded-md flex items-center justify-center hover:bg-gray-50"
              >
                <ExternalLink class="h-4 w-4 mr-2" />
                Forlat husstand
              </button>

              <button
                @click="deleteHousehold"
                class="w-full bg-red-100 text-red-600 py-2 px-4 rounded-md flex items-center justify-center hover:bg-red-200"
              >
                <Trash class="h-4 w-4 mr-2" />
                Slett husstand
              </button>
            </div>
          </div>
        </div>
      </div>

      <!-- Add Member Dialog -->
      <Dialog v-model:open="isAddMemberDialogOpen">
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

      <!-- Meeting Places Map Dialog -->
      <Dialog v-model:open="isMeetingMapDialogOpen" class="meeting-map-dialog">
        <DialogContent class="sm:max-w-5xl h-auto">
          <DialogHeader>
            <DialogTitle>Møteplasser ved krise</DialogTitle>
            <DialogDescription>
              Kartet viser dine møteplasser ved krisesituasjoner. Klikk på markørene for mer informasjon og veibeskrivelse.
            </DialogDescription>
          </DialogHeader>

          <div class="py-6 px-2">
            <HouseholdMeetingMap
              ref="mapRef"
              :meeting-places="apiResponse.household.meetingPlaces"
              :household-position="apiResponse.household.position"
              @meeting-place-selected="handleMeetingPlaceSelected"
              class="min-h-[625px]"
            />

            <div class="mt-4 flex gap-3">
              <div v-for="place in apiResponse.household.meetingPlaces" :key="place.id"
                   :class="[
                    'px-3 py-2 rounded-md text-sm cursor-pointer border flex-1',
                    place.type === 'primary'
                      ? 'bg-red-50 border-red-200 text-red-800'
                      : 'bg-orange-50 border-orange-200 text-orange-800'
                  ]"
                   @click="viewMeetingPlace(place.id)"
              >
                <div class="font-medium">{{ place.name.split(':')[0] }}</div>
              </div>
            </div>
          </div>
        </DialogContent>
      </Dialog>
    </div>
  </div>
</template>

<style scoped>
:deep(.meeting-map-dialog) {
  max-width: 1250px;
  width: 95vw;
}
</style>
