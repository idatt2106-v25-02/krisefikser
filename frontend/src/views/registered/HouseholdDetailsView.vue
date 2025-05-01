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
import {
  Form,
  FormControl,
  FormField,
  FormItem,
  FormLabel,
  FormMessage,
} from '@/components/ui/form'
import {
  MapPin,
  ExternalLink,
  Trash,
  UserMinus,
  AlertCircle,
  Map as MapIcon,
} from 'lucide-vue-next'
import HouseholdMeetingMap from '@/components/household/HouseholdMeetingMap.vue'
import HouseholdEmergencySupplies from '@/components/household/HouseholdEmergencySupplies.vue'
import {
  useGetActiveHousehold,
  useLeaveHousehold,
  useDeleteHousehold,
  useJoinHousehold,
} from '@/api/generated/household/household'
import { useAuthStore } from '@/stores/useAuthStore'
import { Badge } from '@/components/ui/badge'
import type { HouseholdResponse } from '@/api/generated/model'

interface MeetingPlace {
  id: string
  name: string
  address: string
  latitude: number
  longitude: number
  description?: string
  type: 'primary' | 'secondary'
}

interface Inventory {
  food: { current: number; target: number; unit: string }
  water: { current: number; target: number; unit: string }
  other: { current: number; target: number }
  preparedDays: number
  targetDays: number
}

type MemberFormValues = {
  name: string
  email?: string
  consumptionFactor?: number
}

interface ExtendedHouseholdResponse extends HouseholdResponse {
  meetingPlaces?: MeetingPlace[]
  inventory?: Inventory
  inventoryItems?: Array<{
    name: string
    expiryDate: string
    amount: number
    productType: {
      name: string
      unit: string
    }
  }>
}

// Form schemas
const memberFormSchema = toTypedSchema(
  z.object({
    name: z.string().min(1, 'Navn er påkrevd'),
    email: z.string().email('Ugyldig e-postadresse').optional(),
    consumptionFactor: z.number().min(0.1, 'Må være større enn 0').optional(),
  }),
)

const router = useRouter()
const authStore = useAuthStore()

// Get household data
const {
  data: household,
  isLoading: isLoadingHousehold,
  refetch: refetchHousehold,
} = useGetActiveHousehold<ExtendedHouseholdResponse>({
  query: {
    enabled: authStore.isAuthenticated,
    refetchOnMount: true,
    refetchOnWindowFocus: true,
  },
})

// Mutations
const { mutate: leaveHousehold } = useLeaveHousehold({
  mutation: {
    onSuccess: () => {
      router.push('/husstand')
    },
  },
})

const { mutate: deleteHousehold } = useDeleteHousehold({
  mutation: {
    onSuccess: () => {
      router.push('/husstand')
    },
  },
})

const { mutate: addMember } = useJoinHousehold({
  mutation: {
    onSuccess: () => {
      refetchHousehold()
      isAddMemberDialogOpen.value = false
    },
  },
})

const { mutate: removeMember } = useLeaveHousehold({
  mutation: {
    onSuccess: () => {
      refetchHousehold()
    },
  },
})

// State for dialogs
const isAddMemberDialogOpen = ref(false)
const isMeetingMapDialogOpen = ref(false)
const memberMode = ref<'invite' | 'add'>('add')
const mapRef = ref<InstanceType<typeof HouseholdMeetingMap> | null>(null)
const selectedMeetingPlace = ref<MeetingPlace | null>(null)

// Form handling
const { handleSubmit: submitMemberForm } = useForm<MemberFormValues>({
  validationSchema: memberFormSchema,
})

// Actions
function onMemberSubmit() {
  if (!household.value) return

  addMember({
    data: {
      householdId: household.value.id,
    },
  })
}

function onRemoveMember(userId?: string) {
  if (!userId) return
  if (!household.value) return
  removeMember({
    data: {
      householdId: household.value.id,
    },
  })
}

function navigateToInventory() {
  router.push(`/husstand/${household.value?.id}/beredskapslager`)
}

function handleLeaveHousehold() {
  if (!household.value) return
  if (confirm('Er du sikker på at du vil forlate denne husstanden?')) {
    leaveHousehold({
      data: {
        householdId: household.value.id,
      },
    })
  }
}

function handleDeleteHousehold() {
  if (!household.value) return
  if (
    confirm('Er du sikker på at du vil slette denne husstanden? Dette kan ikke angres.') &&
    household.value.id
  ) {
    deleteHousehold({
      id: household.value.id,
    })
  }
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
      <div v-if="isLoadingHousehold" class="flex justify-center items-center h-64">
        <div class="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-500"></div>
      </div>

      <div v-else-if="household">
        <!-- Household header -->
        <div class="bg-white rounded-lg shadow-sm border border-gray-200 p-6 mb-6">
          <div class="flex flex-col md:flex-row md:items-center md:justify-between">
            <div>
              <h1 class="text-4xl font-bold text-gray-900 mb-1">{{ household.name }}</h1>
              <div class="flex items-center text-gray-600">
                <MapPin class="h-4 w-4 text-gray-400 mr-1" />
                <span>{{ household.address }}</span>
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
                  v-for="member in household.members"
                  :key="member.user?.id"
                  class="bg-gray-50 rounded-lg border border-gray-200 overflow-hidden hover:shadow-md transition-shadow"
                >
                  <div class="p-4">
                    <div class="flex justify-between items-start">
                      <div class="flex items-center mb-3">
                        <div
                          class="h-10 w-10 bg-blue-100 rounded-full flex items-center justify-center text-blue-600 mr-3 flex-shrink-0"
                        >
                          <span class="text-md font-medium">{{
                            member.user?.firstName?.[0] || '?'
                          }}</span>
                        </div>
                        <h3 class="text-md font-bold text-gray-900">
                          {{ member.user?.firstName }} {{ member.user?.lastName }}
                        </h3>
                      </div>
                      <DropdownMenu v-if="member.user?.id !== authStore.currentUser?.id">
                        <DropdownMenuTrigger as-child>
                          <Button variant="ghost" size="icon" class="h-8 w-8">
                            <span class="sr-only">Medlemsalternativer</span>
                            <svg
                              xmlns="http://www.w3.org/2000/svg"
                              width="16"
                              height="16"
                              viewBox="0 0 24 24"
                              fill="none"
                              stroke="currentColor"
                              stroke-width="2"
                              stroke-linecap="round"
                              stroke-linejoin="round"
                              class="text-gray-400"
                            >
                              <circle cx="12" cy="12" r="1" />
                              <circle cx="12" cy="5" r="1" />
                              <circle cx="12" cy="19" r="1" />
                            </svg>
                          </Button>
                        </DropdownMenuTrigger>
                        <DropdownMenuContent align="end">
                          <DropdownMenuItem
                            @click="onRemoveMember(member.user?.id)"
                            class="text-red-600"
                          >
                            <UserMinus class="h-4 w-4 mr-2" />
                            Fjern medlem
                          </DropdownMenuItem>
                        </DropdownMenuContent>
                      </DropdownMenu>
                    </div>

                    <div class="text-sm text-gray-600">
                      <div class="mt-1">
                        <Badge :variant="member.status === 'ACCEPTED' ? 'default' : 'secondary'">
                          {{ member.status }}
                        </Badge>
                      </div>
                      <div class="mt-1 truncate">
                        {{ member.user?.email }}
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
                :inventory="{
                  food: {
                    current: household.inventory?.food?.current || 0,
                    target: household.inventory?.food?.target || 0,
                    unit: household.inventory?.food?.unit || '',
                  },
                  water: {
                    current: household.inventory?.water?.current || 0,
                    target: household.inventory?.water?.target || 0,
                    unit: household.inventory?.water?.unit || '',
                  },
                  other: {
                    current: household.inventory?.other?.current || 0,
                    target: household.inventory?.other?.target || 0,
                  },
                  preparedDays: household.inventory?.preparedDays || 0,
                  targetDays: household.inventory?.targetDays || 7,
                }"
                :inventory-items="household.inventoryItems || []"
                :household-id="household.id || ''"
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
                <div v-for="place in household.meetingPlaces" :key="place.id" class="p-4">
                  <div class="flex items-start">
                    <div class="flex-shrink-0 mr-3">
                      <div
                        :class="[
                          'h-10 w-10 rounded-full flex items-center justify-center',
                          place.type === 'primary' ? 'bg-red-500' : 'bg-orange-400',
                        ]"
                      >
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
                  @click="handleLeaveHousehold"
                  class="w-full bg-white border border-gray-300 text-gray-700 py-2 px-4 rounded-md flex items-center justify-center hover:bg-gray-50"
                >
                  <ExternalLink class="h-4 w-4 mr-2" />
                  Forlat husstand
                </button>

                <button
                  @click="handleDeleteHousehold"
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
                      <Input type="number" step="0.1" min="0" />
                    </FormControl>
                    <FormMessage> 0.5 for halv porsjon, 1 for normal porsjon, osv. </FormMessage>
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
                Kartet viser dine møteplasser ved krisesituasjoner. Klikk på markørene for mer
                informasjon og veibeskrivelse.
              </DialogDescription>
            </DialogHeader>

            <div class="py-6 px-2">
              <HouseholdMeetingMap
                ref="mapRef"
                :meeting-places="
                  household.meetingPlaces?.map((place: any) => ({
                    ...place,
                    position: [place.latitude, place.longitude],
                  })) || []
                "
                :household-position="[household.latitude || 0, household.longitude || 0]"
                @meeting-place-selected="handleMeetingPlaceSelected"
                class="min-h-[625px]"
              />

              <div class="mt-4 flex gap-3">
                <div
                  v-for="place in household.meetingPlaces"
                  :key="place.id"
                  :class="[
                    'px-3 py-2 rounded-md text-sm cursor-pointer border flex-1',
                    place.type === 'primary'
                      ? 'bg-red-50 border-red-200 text-red-800'
                      : 'bg-orange-50 border-orange-200 text-orange-800',
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

      <div v-else class="text-center py-12">
        <p class="text-gray-500">Kunne ikke laste husstandsdata. Vennligst prøv igjen senere.</p>
      </div>
    </div>
  </div>
</template>

<style scoped>
:deep(.meeting-map-dialog) {
  max-width: 1250px;
  width: 95vw;
}
</style>
