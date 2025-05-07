<script setup lang="ts">
import { ref, computed, watchEffect } from 'vue'
import router from '@/router'
import { useForm } from 'vee-validate'
import { toTypedSchema } from '@vee-validate/zod'
import * as z from 'zod'
import { Button } from '@/components/ui/button'
import Dialog from '@/components/ui/dialog/Dialog.vue'
import DialogContent from '@/components/ui/dialog/DialogContent.vue'
import DialogDescription from '@/components/ui/dialog/DialogDescription.vue'
import DialogHeader from '@/components/ui/dialog/DialogHeader.vue'
import DialogTitle from '@/components/ui/dialog/DialogTitle.vue'
import DialogFooter from '@/components/ui/dialog/DialogFooter.vue'
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
  Edit,
  Info,
  RefreshCw,
  CheckCircle,
  BookText,
} from 'lucide-vue-next'
import HouseholdMeetingMap from '@/components/household/HouseholdMeetingMap.vue'
import HouseholdEmergencySupplies from '@/components/household/HouseholdEmergencySupplies.vue'
import {
  useGetAllUserHouseholds,
  useGetActiveHousehold,
  useLeaveHousehold,
  useDeleteHousehold,
  useJoinHousehold,
  useAddGuestToHousehold,
  useRemoveGuestFromHousehold,
  useUpdateActiveHousehold,
  getGetActiveHouseholdQueryKey,
  useSetActiveHousehold,
} from '@/api/generated/household/household.ts'
import {
  useCreateInvite,
  useGetPendingInvitesForUser,
  useAcceptInvite,
  useDeclineInvite,
  useGetPendingInvitesForHousehold,
} from '@/api/generated/household-invite-controller/household-invite-controller.ts'
import { useAuthStore } from '@/stores/auth/useAuthStore.ts'
import type {
  HouseholdResponse,
  GuestResponse,
  HouseholdMemberResponse,
  CreateHouseholdRequest,
} from '@/api/generated/model'
import { useToast } from '@/components/ui/toast/use-toast.ts'
import InvitedPendingList from '@/components/household/InvitedPendingList.vue'
import { useQueryClient } from '@tanstack/vue-query'
import { useGetInventorySummary, getGetInventorySummaryQueryKey } from '@/api/generated/item/item'
import PreparednessInfoDialog from '@/components/inventory/info/PreparednessInfoDialog.vue'

interface MeetingPlace {
  id: string
  name: string
  address: string
  latitude: number
  longitude: number
  description?: string
  type: 'primary' | 'secondary'
  targetDays: number
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

// Updated to match CreateHouseholdRequest fields
type HouseholdFormValues = {
  name: string
  address: string
  addressLine2?: string
  postalCode: string
  city: string
  country: string
  description?: string
}

// Extend ExtendedHouseholdResponse without redefining guests
interface ExtendedHouseholdResponse extends HouseholdResponse {
  meetingPlaces?: MeetingPlace[]
  inventory?: Inventory
  addressLine2?: string
  postalCode: string
  city: string
  country?: string
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

// Updated form schema with complete address fields
const householdFormSchema = toTypedSchema(
  z.object({
    name: z.string().min(1, 'Navn på husstanden er påkrevd'),
    address: z.string().min(1, 'Adresse er påkrevd'),
    postalCode: z.string().refine((val) => /^\d{4}$/.test(val), {
      message: 'Postnummer må bestå av nøyaktig 4 siffer.',
    }),
    city: z
      .string()
      .min(1, 'By/sted er påkrevd')
      .max(50, 'By/sted kan ikke være lenger enn 50 tegn')
      .refine((val) => /^[a-zA-ZæøåÆØÅ -]+$/.test(val), {
        message: 'By/sted kan kun inneholde bokstaver, bindestrek og mellomrom.',
      }),
  }),
)

const authStore = useAuthStore()
const { toast } = useToast()
const queryClient = useQueryClient()

// Get households data
const {
  data: allHouseholds,
  isLoading: isLoadingAllHouseholds,
  refetch: refetchAllHouseholds,
} = useGetAllUserHouseholds({
  query: {
    enabled: authStore.isAuthenticated,
    refetchOnMount: true,
    refetchOnWindowFocus: true,
  },
})

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

// Fetch Inventory Summary specifically for the preview
const { data: inventorySummary } = useGetInventorySummary({
  query: {
    enabled: computed(() => authStore.isAuthenticated && !!household.value?.id),
  },
})

// Computed property to format inventory data for the preview, similar to HouseholdInventoryView
const inventoryPreviewData = computed<Inventory>(() => {
  const DAYS_GOAL = 7

  if (!inventorySummary.value) {
    // Fallback if summary isn't loaded - might use household.inventory as a rough estimate or just zeros
    // For consistency, if summary is crucial, show loading or default to all zeros until summary loads.
    return {
      food: {
        current: household.value?.inventory?.food?.current || 0,
        target: household.value?.inventory?.food?.target || 0,
        unit: 'kcal',
      },
      water: {
        current: household.value?.inventory?.water?.current || 0,
        target: household.value?.inventory?.water?.target || 0,
        unit: 'L',
      },
      other: {
        current: household.value?.inventory?.other?.current || 0,
        target: household.value?.inventory?.other?.target || 0,
      },
      preparedDays: 0, // Default until summary loads and calculates
      targetDays: DAYS_GOAL,
    }
  }

  const summary = inventorySummary.value

  const dailyKcalNeeded = summary.kcalGoal > 0 ? summary.kcalGoal / DAYS_GOAL : 0
  const effectiveFoodDays =
    summary.kcalGoal === 0 && summary.kcal > 0
      ? DAYS_GOAL
      : dailyKcalNeeded > 0
        ? summary.kcal / dailyKcalNeeded
        : 0

  const dailyWaterNeeded = summary.waterLitersGoal > 0 ? summary.waterLitersGoal / DAYS_GOAL : 0
  const effectiveWaterDays =
    summary.waterLitersGoal === 0 && summary.waterLiters > 0
      ? DAYS_GOAL
      : dailyWaterNeeded > 0
        ? summary.waterLiters / dailyWaterNeeded
        : 0

  let derivedPreparedDays
  if (summary.kcalGoal === 0 && summary.waterLitersGoal === 0) {
    derivedPreparedDays = summary.kcal > 0 || summary.waterLiters > 0 ? DAYS_GOAL : 0
  } else if (summary.kcalGoal === 0) {
    derivedPreparedDays = summary.kcal > 0 ? Math.floor(effectiveWaterDays) : 0
  } else if (summary.waterLitersGoal === 0) {
    derivedPreparedDays = summary.waterLiters > 0 ? Math.floor(effectiveFoodDays) : 0
  } else {
    derivedPreparedDays = Math.floor(Math.min(effectiveFoodDays, effectiveWaterDays))
  }

  let finalPreparedDays = Math.min(derivedPreparedDays, DAYS_GOAL)
  if (finalPreparedDays < 0) finalPreparedDays = 0

  return {
    food: {
      current: summary.kcal ?? 0,
      target: summary.kcalGoal ?? 0,
      unit: 'kcal',
    },
    water: {
      current: summary.waterLiters ?? 0,
      target: summary.waterLitersGoal ?? 0,
      unit: 'L',
    },
    other: {
      current: summary.checkedItems ?? 0,
      target: summary.totalItems ?? 0,
    },
    preparedDays: finalPreparedDays,
    targetDays: DAYS_GOAL,
  }
})

watchEffect(() => {
  if (household.value) {
    console.log(
      '[HouseholdDetailsView] household data changed (from watchEffect):',
      JSON.parse(JSON.stringify(household.value)),
    )
  }
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

const { mutate: _addMember } = useJoinHousehold({
  mutation: {
    onSuccess: () => {
      refetchHousehold()
      queryClient.invalidateQueries({ queryKey: getGetInventorySummaryQueryKey() })
      isAddMemberDialogOpen.value = false
    },
  },
})

const { mutate: removeMember } = useLeaveHousehold({
  mutation: {
    onSuccess: () => {
      refetchHousehold()
      queryClient.invalidateQueries({ queryKey: getGetInventorySummaryQueryKey() })
    },
  },
})

const { mutate: createInvite } = useCreateInvite({
  mutation: {
    onSuccess: () => {
      toast({
        title: 'Invitasjon sendt',
        description: 'Invitasjonen har blitt sendt til brukeren.',
      })
      isAddMemberDialogOpen.value = false
      resetForm()
      refetchHouseholdInvites()
    },
    onError: (error) => {
      const err = error as Error
      toast({
        title: 'Feil',
        description: err.message || 'Kunne ikke sende invitasjon',
        variant: 'destructive',
      })
    },
  },
})

const { mutate: addGuest, isPending: isAddingGuest } = useAddGuestToHousehold({
  mutation: {
    onSuccess: (response) => {
      console.log('Add Guest onSuccess:', response)
      toast({
        title: 'Gjest lagt til',
        description: 'Gjesten har blitt lagt til i husstanden.',
      })
      refetchHousehold()
      queryClient.invalidateQueries({ queryKey: getGetInventorySummaryQueryKey() })
      isAddMemberDialogOpen.value = false
      resetForm()
    },
    onError: (error: unknown) => {
      console.error('Add Guest onError:', error)
      const errorMessage =
        (error as { response?: { data?: { message?: string } } })?.response?.data?.message ||
        (error as Error)?.message ||
        'Kunne ikke legge til gjest.'

      toast({
        title: 'Feil ved tillegging av gjest',
        description: errorMessage,
        variant: 'destructive',
      })
    },
  },
})

const { mutate: removeGuest, isPending: isRemovingGuest } = useRemoveGuestFromHousehold({
  mutation: {
    onSuccess: () => {
      toast({
        title: 'Gjest fjernet',
        description: 'Gjesten er fjernet fra husstanden.',
      })
      refetchHousehold()
      queryClient.invalidateQueries({ queryKey: getGetInventorySummaryQueryKey() })
    },
    onError: (error: unknown) => {
      const errorMessage =
        (error as { response?: { data?: { message?: string } } })?.response?.data?.message ||
        (error as Error)?.message ||
        'Kunne ikke fjerne gjest.'
      toast({
        title: 'Feil ved fjerning av gjest',
        description: errorMessage,
        variant: 'destructive',
      })
    },
  },
})

// State for dialogs
const isAddMemberDialogOpen = ref(false)
const isEditHouseholdDialogOpen = ref(false)
const isMeetingMapDialogOpen = ref(false)
const isChangeHouseholdDialogOpen = ref(false)
const memberMode = ref<'invite' | 'add'>('invite')
const mapRef = ref<InstanceType<typeof HouseholdMeetingMap> | null>(null)
const selectedMeetingPlace = ref<MeetingPlace | null>(null)
const isPreparednessInfoDialogOpen = ref(false)

// Form handling
const { resetForm } = useForm<MemberFormValues>({
  validationSchema: memberFormSchema,
})

// If submitHouseholdForm is not used elsewhere, this entire useForm call can be removed.
// For now, let's assume it might be used by another form or was intended for future use.
// const { handleSubmit: submitHouseholdForm } = useForm<HouseholdFormValues>({
//   validationSchema: householdFormSchema,
//   initialValues: {
//     name: household.value?.name ?? '',
//     address: household.value?.address ?? '',
//     postalCode: household.value?.postalCode ?? '',
//     city: household.value?.city ?? '',
//   },
// });

// Actions
// function onMemberSubmit(values: MemberFormValues) { // Assuming this was for the addMember form
//   if (!household.value) return;
//   addMember({
//     data: {
//       householdId: household.value.id ?? '',
// Ensure that if addMember expects other properties from MemberFormValues, they are included here
//     },
//   });
// }

const { mutateAsync: updateActiveHousehold } = useUpdateActiveHousehold({
  mutation: {
    onSuccess: (updatedHouseholdData) => {
      console.log('useUpdateActiveHousehold onSuccess. Response:', updatedHouseholdData)
      queryClient.setQueryData(getGetActiveHouseholdQueryKey(), updatedHouseholdData)
      isEditHouseholdDialogOpen.value = false
      toast({
        title: 'Husstand oppdatert',
        description: 'Husstandsinformasjonen ble oppdatert.',
      })
    },
    onError: (error) => {
      console.error('useUpdateActiveHousehold onError. Error:', error)
      toast({
        title: 'Feil',
        description: (error as unknown as Error)?.message || 'Kunne ikke oppdatere husstand.',
        variant: 'destructive',
      })
    },
  },
})

function onHouseholdSubmit(formData: Record<string, unknown>) {
  // Cast the received formData to HouseholdFormValues
  const values = formData as HouseholdFormValues

  const householdData: CreateHouseholdRequest = {
    name: values.name,
    address: values.address,
    postalCode: values.postalCode,
    city: values.city,
    latitude: household.value?.latitude ?? 0,
    longitude: household.value?.longitude ?? 0,
  }
  updateActiveHousehold({ data: householdData })
}

function onRemoveMember(userId?: string) {
  if (!userId) return
  if (!household.value) return
  removeMember({
    data: {
      householdId: household.value.id ?? '',
    },
  })
}

function navigateToInventory() {
  router.push('/husstand/beredskapslager')
}

function openEditHouseholdDialog() {
  isEditHouseholdDialogOpen.value = true
}

function handleLeaveHousehold() {
  if (!household.value) return
  if (confirm('Er du sikker på at du vil forlate denne husstanden?')) {
    leaveHousehold({
      data: {
        householdId: household.value.id ?? '',
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

  setTimeout(() => {
    if (mapRef.value) {
      mapRef.value.centerOnMeetingPlace(placeId)
    }
  }, 300)
}

function goToHouseholdLocation() {
  isMeetingMapDialogOpen.value = true
  setTimeout(() => {
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    const map = mapRef.value as any
    if (map && typeof map.centerOnHousehold === 'function') {
      map.centerOnHousehold()
    }
  }, 300)
}

const handleFormSubmit = (values: MemberFormValues) => {
  console.log('handleFormSubmit called with mode:', memberMode.value, 'and values:', values)

  if (!household.value?.id) {
    console.error('Household ID is missing')
    toast({ title: 'Feil', description: 'Husstand ID mangler.', variant: 'destructive' })
    return
  }

  if (memberMode.value === 'invite') {
    console.log('Attempting to send invite for email:', values.email)
    if (!values.email) {
      toast({
        title: 'Feil',
        description: 'E-post er påkrevd for invitasjon.',
        variant: 'destructive',
      })
      return
    }
    createInvite({
      data: {
        householdId: household.value.id,
        invitedEmail: values.email,
      },
    })
  } else {
    if (
      !values.name ||
      values.consumptionFactor === undefined ||
      values.consumptionFactor === null
    ) {
      toast({
        title: 'Feil',
        description: 'Navn og forbruksfaktor er påkrevd for å legge til gjest.',
        variant: 'destructive',
      })
      return
    }
    console.log('Attempting to add guest with data:', {
      name: values.name,
      icon: 'default_guest_icon.png',
      consumptionMultiplier: values.consumptionFactor,
    })
    addGuest({
      data: {
        name: values.name,
        icon: 'default_guest_icon.png',
        consumptionMultiplier: values.consumptionFactor,
      },
    })
  }
}

const handleModeChange = (mode: 'invite' | 'add') => {
  memberMode.value = mode
  resetForm()
}

// Pending invites logic
const { data: pendingInvites, refetch: refetchInvites } = useGetPendingInvitesForUser()
const { mutate: acceptInvite } = useAcceptInvite({
  mutation: {
    onSuccess: () => refetchInvites(),
  },
})
const { mutate: declineInvite } = useDeclineInvite({
  mutation: {
    onSuccess: () => refetchInvites(),
  },
})

// Fetch pending invites for this household
const householdId = computed(() => household.value?.id ?? '')
const { data: householdPendingInvites, refetch: refetchHouseholdInvites } =
  useGetPendingInvitesForHousehold(householdId, {
    query: {
      enabled: computed(() => !!householdId.value),
      refetchOnMount: true,
      refetchOnWindowFocus: true,
    },
  })

function handleRemoveGuest(guestId?: string) {
  if (!guestId) return
  if (confirm('Er du sikker på at du vil fjerne denne gjesten?')) {
    removeGuest({ guestId })
  }
}

// Filter tab state
const memberGuestTab = ref<'alle' | 'medlemmer' | 'gjester'>('alle')

const filteredPeople = computed(() => {
  if (!household.value) return []
  if (memberGuestTab.value === 'alle') {
    // Combine members and guests
    return [
      ...household.value.members.map((m) => ({ type: 'member', data: m })),
      ...household.value.guests.map((g) => ({ type: 'guest', data: g })),
    ]
  } else if (memberGuestTab.value === 'medlemmer') {
    return household.value.members.map((m) => ({ type: 'member', data: m }))
  } else if (memberGuestTab.value === 'gjester') {
    return household.value.guests.map((g) => ({ type: 'guest', data: g }))
  }
  return []
})

// Mutation to set the active household
const { mutate: setActiveHousehold, isPending: isSettingActiveHousehold } = useSetActiveHousehold({
  mutation: {
    onSuccess: () => {
      refetchHousehold()
      refetchAllHouseholds()
      toast({
        title: 'Husstand oppdatert',
        description: 'Aktiv husstand er endret',
      })
      isChangeHouseholdDialogOpen.value = false
    },
    onError: (error: unknown) => {
      toast({
        title: 'Feil',
        description: (error as Error)?.message || 'Kunne ikke sette aktiv husstand',
        variant: 'destructive',
      })
    },
  },
})

function handleChangeActiveHousehold(householdId: string) {
  setActiveHousehold({ data: { householdId } })
}
</script>

<template>
  <div class="bg-gray-50 min-h-screen">
    <div class="max-w-8xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      <!-- Pending Invites Section -->
      <div v-if="pendingInvites && pendingInvites.length" class="mb-6">
        <h2 class="text-xl font-semibold mb-2">Ventende invitasjoner</h2>
        <div
          v-for="invite in pendingInvites"
          :key="invite.id"
          class="bg-yellow-50 border border-yellow-200 rounded-lg p-4 mb-2 flex items-center justify-between"
        >
          <div>
            <div><b>Husstand:</b> {{ invite.household?.name ?? 'Ukjent' }}</div>
            <div>
              <b>Invitert av:</b> {{ invite.createdBy?.firstName }} {{ invite.createdBy?.lastName }}
            </div>
          </div>
          <div class="flex gap-2">
            <button
              class="bg-green-500 text-white px-3 py-1 rounded"
              @click="acceptInvite({ inviteId: invite.id ?? '' })"
            >
              Godta
            </button>
            <button
              class="bg-red-500 text-white px-3 py-1 rounded"
              @click="declineInvite({ inviteId: invite.id ?? '' })"
            >
              Avslå
            </button>
          </div>
        </div>
      </div>

      <div v-if="isLoadingHousehold" class="flex justify-center items-center h-64">
        <div class="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-500"></div>
      </div>

      <div v-else-if="household">
        <!-- Household header with complete address display -->
        <div class="bg-white rounded-lg shadow-sm border border-gray-200 p-6 mb-6">
          <div class="flex flex-col md:flex-row md:items-start md:justify-between">
            <div>
              <h1 class="text-4xl font-bold text-gray-900 mb-1">{{ household.name }}</h1>
              <div class="text-gray-600">
                <div class="flex items-center">
                  <span
                    class="flex items-center cursor-pointer group"
                    @click="goToHouseholdLocation"
                    title="Vis på kart"
                  >
                    <MapPin
                      class="h-4 w-4 mr-1 flex-shrink-0 text-gray-400 group-hover:text-blue-600 transition-colors"
                    />
                    <span
                      class="transition-colors group-hover:text-blue-600 group-hover:underline text-gray-600"
                    >
                      {{ household.address }}, {{ household.postalCode }} {{ household.city }}
                    </span>
                  </span>
                </div>
              </div>
            </div>
            <div
              class="mt-4 md:mt-0 flex flex-col items-end space-y-2"
            >
              <div class="flex flex-row space-x-2">
                <router-link
                  to="/husstand/refleksjoner"
                  class="inline-flex items-center justify-center rounded-md text-sm font-medium ring-offset-background transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 disabled:pointer-events-none disabled:opacity-50 border border-input bg-background hover:bg-accent hover:text-accent-foreground h-9 px-4 py-2"
                >
                  <BookText class="h-4 w-4 mr-2" />
                  Husstandens Refleksjoner
                </router-link>
                <Button
                  variant="outline"
                  class="flex items-center"
                  size="sm"
                  @click="openEditHouseholdDialog"
                >
                  <Edit class="h-4 w-4 mr-2" />
                  Endre informasjon
                </Button>
              </div>
              <Button
                variant="outline"
                size="sm"
                @click="isChangeHouseholdDialogOpen = true"
                class="flex items-center"
                :disabled="!allHouseholds || allHouseholds.length <= 1"
              >
                <RefreshCw class="h-4 w-4 mr-2" />
                Bytt husstand
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
                <h2 class="text-xl font-semibold text-gray-800">Medlemmer og gjester</h2>
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
              <!-- Filter Tabs -->
              <div class="flex gap-2 mb-4">
                <Button
                  :variant="memberGuestTab === 'alle' ? 'default' : 'outline'"
                  size="sm"
                  @click="memberGuestTab = 'alle'"
                  >Alle</Button
                >
                <Button
                  :variant="memberGuestTab === 'medlemmer' ? 'default' : 'outline'"
                  size="sm"
                  @click="memberGuestTab = 'medlemmer'"
                  >Medlemmer</Button
                >
                <Button
                  :variant="memberGuestTab === 'gjester' ? 'default' : 'outline'"
                  size="sm"
                  @click="memberGuestTab = 'gjester'"
                  >Gjester</Button
                >
              </div>
              <div
                class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4 max-h-[32rem] overflow-y-auto pr-2"
              >
                <div
                  v-for="person in filteredPeople"
                  :key="
                    person.type === 'member'
                      ? (person.data as HouseholdMemberResponse).user?.id
                      : (person.data as GuestResponse).id
                  "
                  class="bg-gray-50 rounded-lg border border-gray-200 overflow-hidden hover:shadow-md transition-shadow"
                >
                  <div class="p-4">
                    <div class="flex justify-between items-start">
                      <div class="flex items-center mb-3">
                        <div
                          v-if="person.type === 'member'"
                          class="h-10 w-10 bg-blue-100 rounded-full flex items-center justify-center text-blue-600 mr-3 flex-shrink-0"
                        >
                          <span class="text-md font-medium">{{
                            (person.data as HouseholdMemberResponse).user?.firstName?.[0] ?? '?'
                          }}</span>
                        </div>
                        <div
                          v-else
                          class="h-10 w-10 bg-green-100 rounded-full flex items-center justify-center text-green-600 mr-3 flex-shrink-0"
                        >
                          <span class="text-md font-medium">{{
                            (person.data as GuestResponse).name?.[0]?.toUpperCase() ?? 'G'
                          }}</span>
                        </div>
                        <h3 class="text-md font-bold text-gray-900">
                          {{
                            person.type === 'member'
                              ? (person.data as HouseholdMemberResponse).user?.firstName +
                                ' ' +
                                (person.data as HouseholdMemberResponse).user?.lastName
                              : (person.data as GuestResponse).name
                          }}
                        </h3>
                        <span
                          class="ml-2 text-xs"
                          :class="person.type === 'member' ? 'text-gray-400' : 'text-green-500'"
                        >
                          {{ person.type === 'member' ? 'Medlem' : 'Gjest' }}
                        </span>
                      </div>
                      <DropdownMenu
                        v-if="
                          person.type === 'member' &&
                          (person.data as HouseholdMemberResponse).user?.id !==
                            authStore.currentUser?.id
                        "
                      >
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
                            @click="
                              onRemoveMember((person.data as HouseholdMemberResponse).user?.id)
                            "
                            class="text-red-600"
                          >
                            <UserMinus class="h-4 w-4 mr-2" />
                            Fjern medlem
                          </DropdownMenuItem>
                        </DropdownMenuContent>
                      </DropdownMenu>
                      <DropdownMenu v-else-if="person.type === 'guest'">
                        <DropdownMenuTrigger as-child>
                          <Button variant="ghost" size="icon" class="h-8 w-8">
                            <span class="sr-only">Gjestalternativer</span>
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
                            @click="handleRemoveGuest((person.data as GuestResponse).id)"
                            class="text-red-600"
                            :disabled="isRemovingGuest"
                          >
                            <UserMinus class="h-4 w-4 mr-2" />
                            Fjern gjest
                          </DropdownMenuItem>
                        </DropdownMenuContent>
                      </DropdownMenu>
                    </div>
                    <div class="text-sm text-gray-600">
                      <template v-if="person.type === 'member'">
                        <div class="mt-1 truncate">
                          {{ (person.data as HouseholdMemberResponse).user?.email ?? '' }}
                        </div>
                      </template>
                      <template v-else>
                        Forbruksfaktor: {{ (person.data as GuestResponse).consumptionMultiplier }}
                      </template>
                    </div>
                  </div>
                </div>
              </div>
            </div>

            <!-- Emergency Supplies Section -->
            <div class="bg-white rounded-lg shadow-sm border border-gray-200 p-6">
              <div class="flex justify-between items-center mb-5">
                <h2 class="text-xl font-semibold text-gray-800">Beredskapslager</h2>
                <!-- Group for Info and Se Detaljer buttons -->
                <div class="flex items-center space-x-2">
                  <button
                    @click="isPreparednessInfoDialogOpen = true"
                    class="text-blue-600 hover:text-blue-700 p-1.5 rounded-full hover:bg-blue-50 transition-colors"
                    title="Vis informasjon om beredskapsberegning"
                  >
                    <Info class="h-5 w-5" />
                  </button>
                  <Button
                    variant="outline"
                    size="sm"
                    @click="navigateToInventory"
                    class="flex items-center gap-1"
                  >
                    <span>Se detaljer</span>
                  </Button>
                </div>
              </div>

              <HouseholdEmergencySupplies
                :inventory="inventoryPreviewData"
                :inventory-items="household.inventoryItems || []"
                :show-details-button="false"
                @open-info-dialog="isPreparednessInfoDialogOpen = true"
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

            <!-- Invited (pending and declined) section as its own component -->
            <InvitedPendingList
              :invites="
                (householdPendingInvites || []).filter((i) => i.status === 'PENDING') as any
              "
              :declined-invites="
                (householdPendingInvites || []).filter((i) => i.status === 'DECLINED') as any
              "
            />
          </div>
        </div>

        <!-- Edit Household Dialog -->
        <Dialog v-model:open="isEditHouseholdDialogOpen">
          <DialogContent class="sm:max-w-[525px]">
            <DialogHeader>
              <DialogTitle>Endre husstandsinformasjon</DialogTitle>
              <DialogDescription>
                Oppdater informasjonen om din husstand. Klikk lagre når du er ferdig.
              </DialogDescription>
            </DialogHeader>

            <Form
              :validation-schema="householdFormSchema"
              :initial-values="{
                name: household.name,
                address: household.address,
                postalCode: household.postalCode || '',
                city: household.city || '',
              }"
              @submit="onHouseholdSubmit"
            >
              <div class="grid gap-4 py-4">
                <FormField v-slot="{ componentField }" name="name">
                  <FormItem>
                    <FormLabel>Navn på husstanden</FormLabel>
                    <FormControl>
                      <Input placeholder="F.eks. Familien Hansen" v-bind="componentField" />
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                </FormField>

                <FormField v-slot="{ componentField }" name="address">
                  <FormItem>
                    <FormLabel>Adresse</FormLabel>
                    <FormControl>
                      <Input placeholder="F.eks. Østensjøveien 123" v-bind="componentField" />
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                </FormField>

                <div class="grid grid-cols-2 gap-4">
                  <FormField v-slot="{ componentField }" name="postalCode">
                    <FormItem>
                      <FormLabel>Postnummer</FormLabel>
                      <FormControl>
                        <Input placeholder="F.eks. 0650" v-bind="componentField" />
                      </FormControl>
                      <FormMessage />
                    </FormItem>
                  </FormField>

                  <FormField v-slot="{ componentField }" name="city">
                    <FormItem>
                      <FormLabel>By/sted</FormLabel>
                      <FormControl>
                        <Input placeholder="F.eks. Oslo" v-bind="componentField" />
                      </FormControl>
                      <FormMessage />
                    </FormItem>
                  </FormField>
                </div>
              </div>

              <DialogFooter>
                <Button type="button" variant="outline" @click="isEditHouseholdDialogOpen = false">
                  Avbryt
                </Button>
                <Button type="submit">Lagre endringer</Button>
              </DialogFooter>
            </Form>
          </DialogContent>
        </Dialog>

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
                  @click="handleModeChange('invite')"
                >
                  Inviter via e-post
                </Button>
                <Button
                  :variant="memberMode === 'add' ? 'default' : 'outline'"
                  @click="handleModeChange('add')"
                >
                  Legg til uten konto
                </Button>
              </div>
              <Form v-slot="{ handleSubmit: _handleSubmit }" :validation-schema="memberFormSchema">
                <form @submit.prevent="_handleSubmit(handleFormSubmit as any)" class="space-y-4">
                  <FormField v-slot="{ field, errorMessage }" name="name">
                    <FormItem>
                      <FormLabel>Navn</FormLabel>
                      <FormControl>
                        <Input v-bind="field" />
                      </FormControl>
                      <FormMessage>{{ errorMessage }}</FormMessage>
                    </FormItem>
                  </FormField>
                  <FormField
                    v-if="memberMode === 'invite'"
                    v-slot="{ field, errorMessage }"
                    name="email"
                  >
                    <FormItem>
                      <FormLabel>E-post</FormLabel>
                      <FormControl>
                        <Input v-bind="field" type="email" />
                      </FormControl>
                      <FormMessage>{{ errorMessage }}</FormMessage>
                    </FormItem>
                  </FormField>
                  <FormField
                    v-if="memberMode === 'add'"
                    v-slot="{ field, errorMessage }"
                    name="consumptionFactor"
                  >
                    <FormItem>
                      <FormLabel>Forbruksfaktor</FormLabel>
                      <FormControl>
                        <Input v-bind="field" type="number" step="0.1" min="0.1" />
                      </FormControl>
                      <FormMessage>{{ errorMessage }}</FormMessage>
                    </FormItem>
                  </FormField>
                  <DialogFooter>
                    <Button type="submit" :disabled="isAddingGuest && memberMode === 'add'">
                      {{ memberMode === 'invite' ? 'Send invitasjon' : 'Legg til medlem' }}
                    </Button>
                  </DialogFooter>
                </form>
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

        <!-- Preparedness Info Dialog for HouseholdDetailsView -->
        <PreparednessInfoDialog
          :is-open="isPreparednessInfoDialogOpen"
          @close="isPreparednessInfoDialogOpen = false"
        />

        <!-- Change Household Dialog -->
        <Dialog v-model:open="isChangeHouseholdDialogOpen">
          <DialogContent class="sm:max-w-md">
            <DialogHeader>
              <DialogTitle>Bytt aktiv husstand</DialogTitle>
              <DialogDescription>
                Velg hvilken husstand du vil gjøre til din aktive husstand.
              </DialogDescription>
            </DialogHeader>

            <div class="mb-6 max-h-80 overflow-y-auto">
              <div v-if="isLoadingAllHouseholds" class="flex justify-center py-4">
                <div
                  class="animate-spin h-6 w-6 border-2 border-blue-500 rounded-full border-t-transparent"
                ></div>
              </div>
              <div
                v-else-if="!allHouseholds || allHouseholds.length === 0"
                class="text-center text-gray-500 py-4"
              >
                Ingen husstander tilgjengelig
              </div>
              <div
                v-else
                v-for="h in allHouseholds"
                :key="h.id"
                class="border border-gray-200 rounded-lg p-3 mb-2 hover:bg-gray-50 transition"
                :class="{ 'bg-blue-50 border-blue-300': h.id === household?.id }"
              >
                <div class="flex items-center justify-between">
                  <div>
                    <div class="font-medium">{{ h.name }}</div>
                    <div class="text-sm text-gray-500">{{ h.address }}</div>
                  </div>
                  <button
                    v-if="h.id !== household?.id"
                    @click="handleChangeActiveHousehold(h.id)"
                    class="bg-blue-600 hover:bg-blue-700 text-white px-3 py-1 rounded text-sm"
                    :disabled="isSettingActiveHousehold"
                  >
                    Velg
                  </button>
                  <div v-else class="px-3 py-1 text-sm text-blue-600 font-medium flex items-center">
                    <CheckCircle class="h-4 w-4 mr-1" />
                    Aktiv
                  </div>
                </div>
              </div>
            </div>

            <DialogFooter>
              <Button variant="outline" @click="isChangeHouseholdDialogOpen = false"> Lukk </Button>
            </DialogFooter>
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
