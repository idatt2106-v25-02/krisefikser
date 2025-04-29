<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
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
import { useGetActiveHousehold, useLeaveHousehold, useDeleteHousehold, useJoinHousehold } from '@/api/generated/household/household'
import { useAuthStore } from '@/stores/useAuthStore'
import { Badge } from '@/components/ui/badge'
import type { HouseholdResponse, HouseholdMemberDto, UserDto } from '@/api/generated/model'

interface Member {
  id: string
  name: string
  consumptionFactor: number
}

interface MeetingPlace {
  id: string
  name: string
  latitude: number
  longitude: number
  type: 'primary' | 'secondary'
  address: string
  description?: string
}

interface InventoryItem {
  id: string
  name: string
  amount: number
  expiryDate: string
  category: 'food' | 'water' | 'health' | 'power' | 'comm' | 'misc'
}

interface InventorySummary {
  current: number
  target: number
  unit?: string
}

interface ExtendedHouseholdResponse extends HouseholdResponse {
  meetingPlaces?: MeetingPlace[]
  inventory?: {
    food?: InventorySummary
    water?: InventorySummary
    other?: InventorySummary
    preparedDays?: number
    targetDays?: number
  }
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

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()

const householdId = computed(() => route.params.id as string)

// Get household data
const { data: household, isLoading: isLoadingHousehold, refetch: refetchHousehold } = useGetActiveHousehold<ExtendedHouseholdResponse>({
  query: {
    enabled: authStore.isAuthenticated,
    refetchOnMount: true,
    refetchOnWindowFocus: true
  }
})

const meetingPlaces = computed(() => household.value?.meetingPlaces || [])

const mapCenter = computed(() => {
  if (!household.value?.meetingPlaces?.length) {
    return { lat: 59.9139, lng: 10.7522 } // Default to Oslo
  }
  const primaryPlace = household.value.meetingPlaces.find(p => p.type === 'primary')
  if (primaryPlace) {
    return { lat: primaryPlace.latitude, lng: primaryPlace.longitude }
  }
  return {
    lat: household.value.meetingPlaces[0].latitude,
    lng: household.value.meetingPlaces[0].longitude
  }
})

// Mutations
const { mutate: leaveHousehold } = useLeaveHousehold({
  mutation: {
    onSuccess: () => {
      router.push('/husstand')
    }
  }
})

const { mutate: deleteHousehold } = useDeleteHousehold({
  mutation: {
    onSuccess: () => {
      router.push('/husstand')
    }
  }
})

const { mutate: addMember } = useJoinHousehold({
  mutation: {
    onSuccess: () => {
      refetchHousehold()
      isAddMemberDialogOpen.value = false
    }
  }
})

const { mutate: removeMember } = useLeaveHousehold({
  mutation: {
    onSuccess: () => {
      refetchHousehold()
    }
  }
})

// State for dialogs
const isAddMemberDialogOpen = ref(false)
const isMeetingMapDialogOpen = ref(false)
const memberMode = ref<'invite' | 'add'>('invite')
const mapRef = ref<InstanceType<typeof HouseholdMeetingMap> | null>(null)
const selectedMeetingPlace = ref<MeetingPlace | null>(null)

// Form handling
const { handleSubmit: submitMemberForm } = useForm<MemberFormValues>({
  validationSchema: memberFormSchema
})

// Actions
function onMemberSubmit(values: MemberFormValues) {
  if (!household.value) return

  addMember({
    data: {
      householdId: household.value.id
    }
  })
}

function onRemoveMember(userId?: string) {
  if (!userId) return
  removeMember({
    data: {
      householdId: household.value?.id
    }
  })
}

function handleDeleteMember(memberId: string) {
  // TODO: Implement delete member API call
  console.log('Delete member:', memberId)
}

function navigateToInventory() {
  router.push(`/husstand/${household.value?.id}/beredskapslager`)
}

function handleLeaveHousehold() {
  if (!household.value) return
  if (confirm('Er du sikker på at du vil forlate denne husstanden?')) {
    leaveHousehold({
      data: {
        householdId: household.value.id
      }
    })
  }
}

function handleDeleteHousehold() {
  if (!household.value) return
  if (confirm('Er du sikker på at du vil slette denne husstanden? Dette kan ikke angres.') && household.value.id) {
    deleteHousehold({
      id: household.value.id
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
  <div class="max-w-8xl mx-auto px-20 py-8">
    <div v-if="isLoadingHousehold" class="flex justify-center items-center h-64">
      <div class="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-500"></div>
    </div>

    <div v-else-if="household">
      <!-- Household header -->
      <div class="mb-8">
        <h1 class="text-3xl font-bold text-gray-900 mb-1">{{ household.name }}</h1>
        <div class="flex items-center text-gray-600 mb-6">
          <MapPin class="h-4 w-4 text-gray-400 mr-1" />
          <span>{{ household.address }}</span>
          <ExternalLink class="h-4 w-4 ml-1 text-gray-400 cursor-pointer" />
        </div>
      </div>

      <!-- Members Section -->
      <div class="mb-12">
        <h2 class="text-2xl font-semibold text-gray-800 mb-4">Medlemmer</h2>

        <div class="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 gap-2 mb-4">
          <template v-for="member in household.members" :key="member.user?.id">
            <div class="flex items-center justify-between p-4 bg-white rounded-lg shadow">
              <div class="flex items-center space-x-3">
                <div class="w-10 h-10 rounded-full bg-gray-200 flex items-center justify-center">
                  <span class="text-gray-600">{{ member.user?.firstName?.[0] || '?' }}</span>
                </div>
                <div>
                  <div class="font-medium">{{ member.user?.firstName }} {{ member.user?.lastName }}</div>
                  <div class="text-sm text-gray-500">{{ member.user?.email }}</div>
                </div>
              </div>
              <div class="flex items-center space-x-2">
                <Badge :variant="member.status === 'ACCEPTED' ? 'default' : 'secondary'">
                  {{ member.status }}
                </Badge>
                <Button
                  v-if="member.user?.id !== authStore.currentUser?.id"
                  variant="ghost"
                  size="icon"
                  @click="onRemoveMember(member.user?.id)"
                >
                  <UserMinus class="h-4 w-4" />
                </Button>
              </div>
            </div>
          </template>

          <!-- Add member button -->
          <button
            class="flex items-center justify-center bg-white border border-dashed border-gray-300 rounded-lg py-2 px-4 text-gray-500 hover:bg-gray-50"
            @click="isAddMemberDialogOpen = true"
          >
            <div class="h-8 w-8 border border-dashed border-gray-300 rounded-full flex items-center justify-center mr-3">
              <span class="text-lg">+</span>
            </div>
            <span>Legg til medlem</span>
          </button>
        </div>
      </div>

      <!-- Meeting Places Section -->
      <div class="mb-12">
        <h2 class="text-2xl font-semibold text-gray-800 mb-4">Møteplasser ved krise</h2>

        <div class="bg-white border border-gray-200 rounded-lg overflow-hidden">
          <div class="p-5 border-b">
            <div class="flex justify-between items-center">
              <div>
                <h3 class="font-semibold text-gray-800 mb-1">
                  Dine møteplasser ved krisesituasjoner
                </h3>
                <p class="text-sm text-gray-600">
                  Ved en krisesituasjon skal du møte opp på en av disse møteplassene.
                </p>
              </div>
              <Button
                variant="outline"
                size="sm"
                class="flex items-center gap-1"
                @click="isMeetingMapDialogOpen = true"
              >
                <MapIcon class="h-4 w-4" />
                <span>Vis i kart</span>
              </Button>
            </div>
          </div>

          <div>
            <div
              v-for="(place, index) in household?.meetingPlaces || []"
              :key="place.id"
              :class="['p-4 flex items-start', index < (household?.meetingPlaces?.length || 0) - 1 ? 'border-b border-gray-100' : '']"
            >
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
                <p v-if="place.description" class="text-sm text-gray-600 mt-1">{{ place.description }}</p>
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

      <!-- Emergency Supplies Summary Section -->
      <div class="mb-12">
        <h2 class="text-2xl font-semibold text-gray-800 mb-4">Beredskapslager</h2>

        <!-- Summary boxes -->
        <div class="bg-white border border-gray-200 rounded-lg p-6 mb-6">
          <div class="grid grid-cols-3 gap-4 mb-8">
            <div>
              <div class="text-sm text-gray-500 mb-1">Mat</div>
              <div class="text-lg text-blue-600 font-semibold">
                {{ household.inventory?.food?.current || 0 }}/{{ household.inventory?.food?.target || 0 }} {{ household.inventory?.food?.unit || '' }}
              </div>
            </div>
            <div>
              <div class="text-sm text-gray-500 mb-1">Vann</div>
              <div class="text-lg text-blue-600 font-semibold">
                {{ household.inventory?.water?.current || 0 }}/{{ household.inventory?.water?.target || 0 }} {{ household.inventory?.water?.unit || '' }}
              </div>
            </div>
            <div>
              <div class="text-sm text-gray-500 mb-1">Annet</div>
              <div class="text-lg text-blue-600 font-semibold">
                {{ household.inventory?.other?.current || 0 }}/{{ household.inventory?.other?.target || 0 }}
              </div>
            </div>
          </div>

          <!-- Days prepared -->
          <div class="mb-2">
            <div class="flex justify-between mb-1">
              <span class="text-sm text-gray-600">Dager forberedt</span>
              <span class="text-sm font-medium">{{ household.inventory?.preparedDays || 0 }}</span>
            </div>
            <div class="h-2 bg-gray-200 rounded-full overflow-hidden">
              <div
                class="h-full bg-blue-500 rounded-full"
                :style="`width: ${((household.inventory?.preparedDays || 0) / (household.inventory?.targetDays || 1)) * 100}%`"
              ></div>
            </div>
            <div class="mt-1 text-xs text-gray-500">
              Norske myndigheter anbefaler at du har nok forsyninger tilregnet {{ household.inventory?.targetDays || 7 }} dager.
            </div>
          </div>

          <button
            @click="navigateToInventory"
            class="w-full mt-4 bg-blue-600 hover:bg-blue-700 text-white py-2 px-4 rounded-md transition-colors duration-200 font-medium flex items-center justify-center"
          >
            Vis detaljer
          </button>
        </div>
      </div>

      <!-- Action buttons -->
      <div class="flex flex-col space-y-3">
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
        <DialogContent class="sm:max-w-3xl h-auto">
          <DialogHeader>
            <DialogTitle>Møteplasser ved krise</DialogTitle>
            <DialogDescription>
              Kartet viser dine møteplasser ved krisesituasjoner. Klikk på markørene for mer informasjon og veibeskrivelse.
            </DialogDescription>
          </DialogHeader>

          <div class="py-4">
            <HouseholdMeetingMap
              ref="mapRef"
              :meeting-places="household.meetingPlaces"
              :household-position="household.position"
              @meeting-place-selected="handleMeetingPlaceSelected"
            />

            <div class="mt-4 flex gap-3">
              <div v-for="place in household.meetingPlaces" :key="place.id"
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

    <div v-else class="text-center py-12">
      <p class="text-gray-500">Kunne ikke laste husstandsdata. Vennligst prøv igjen senere.</p>
    </div>
  </div>
</template>

<style scoped>
:deep(.meeting-map-dialog) {
  max-width: 800px;
  width: 90vw;
}
</style>
