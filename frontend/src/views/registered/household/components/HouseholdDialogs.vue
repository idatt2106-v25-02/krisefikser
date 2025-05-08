<script setup lang="ts">
import { ref, computed } from 'vue'
import { Button } from '@/components/ui/button'
import Dialog from '@/components/ui/dialog/Dialog.vue'
import DialogContent from '@/components/ui/dialog/DialogContent.vue'
import DialogDescription from '@/components/ui/dialog/DialogDescription.vue'
import DialogHeader from '@/components/ui/dialog/DialogHeader.vue'
import DialogTitle from '@/components/ui/dialog/DialogTitle.vue'
import DialogFooter from '@/components/ui/dialog/DialogFooter.vue'
import { Input } from '@/components/ui/input'
import {
  Form,
  FormControl,
  FormField,
  FormItem,
  FormLabel,
  FormMessage,
} from '@/components/ui/form'
import { useForm } from 'vee-validate'
import { toTypedSchema } from '@vee-validate/zod'
import * as z from 'zod'
import { CheckCircle } from 'lucide-vue-next'
import type { HouseholdResponse } from '@/api/generated/model'
import HouseholdMeetingMap from '@/components/household/HouseholdMeetingMap.vue'
import PreparednessInfoDialog from '@/components/inventory/info/PreparednessInfoDialog.vue'
import { useAuthStore } from '@/stores/auth/useAuthStore'
import { useToast } from '@/components/ui/toast/use-toast'
import {
  useGetAllUserHouseholds,
  useGetActiveHousehold,
  useUpdateActiveHousehold,
  useSetActiveHousehold,
  useAddGuestToHousehold,
} from '@/api/generated/household/household.ts'
import { useCreateInvite } from '@/api/generated/household-invite-controller/household-invite-controller.ts'

interface MeetingPlace {
  id: string
  name: string
  address: string
  latitude: number
  longitude: number
  description?: string
  type: 'primary' | 'secondary'
  targetDays: number
  position: [number, number]
}

interface ExtendedHouseholdResponse extends HouseholdResponse {
  meetingPlaces?: MeetingPlace[]
}

type MemberFormValues = {
  name: string
  email?: string
  consumptionFactor?: number
}

type HouseholdFormValues = {
  name: string
  address: string
  postalCode: string
  city: string
}

// Props now only receive trigger states
const props = defineProps<{
  isEditDialogOpen: boolean
  isAddMemberDialogOpen: boolean
  isMeetingMapDialogOpen: boolean
  isChangeHouseholdDialogOpen: boolean
  isPreparednessInfoDialogOpen: boolean
}>()

const emit = defineEmits<{
  (e: 'update:isEditDialogOpen', value: boolean): void
  (e: 'update:isAddMemberDialogOpen', value: boolean): void
  (e: 'update:isMeetingMapDialogOpen', value: boolean): void
  (e: 'update:isChangeHouseholdDialogOpen', value: boolean): void
  (e: 'update:isPreparednessInfoDialogOpen', value: boolean): void
  (e: 'householdUpdated'): void
  (e: 'memberAdded'): void
  (e: 'activeHouseholdChanged'): void
  (e: 'meetingPlaceSelected', place: MeetingPlace): void
}>()

// Internal state management
const memberMode = ref<'invite' | 'add'>('invite')
const mapRef = ref<InstanceType<typeof HouseholdMeetingMap> | null>(null)
const selectedMeetingPlace = ref<MeetingPlace | null>(null)
const { toast } = useToast()
const authStore = useAuthStore()

// API hooks
const { data: allHouseholds } = useGetAllUserHouseholds({
  query: {
    enabled: computed(() => authStore.isAuthenticated),
  },
})

const { data: household } = useGetActiveHousehold<ExtendedHouseholdResponse>({
  query: {
    enabled: computed(() => authStore.isAuthenticated),
  },
})

const { mutateAsync: updateActiveHousehold } = useUpdateActiveHousehold({
  mutation: {
    onSuccess: () => {
      emit('update:isEditDialogOpen', false)
      emit('householdUpdated')
      toast({
        title: 'Husstand oppdatert',
        description: 'Husstandsinformasjonen ble oppdatert.',
      })
    },
    onError: (error: unknown) => {
      const err = error as Error
      toast({
        title: 'Feil',
        description: err.message || 'Kunne ikke oppdatere husstand.',
        variant: 'destructive',
      })
    },
  },
})

const { mutate: setActiveHousehold, isPending: isSettingActiveHousehold } = useSetActiveHousehold({
  mutation: {
    onSuccess: () => {
      emit('update:isChangeHouseholdDialogOpen', false)
      emit('activeHouseholdChanged')
      toast({
        title: 'Husstand oppdatert',
        description: 'Aktiv husstand er endret',
      })
    },
    onError: (error: unknown) => {
      const err = error as Error
      toast({
        title: 'Feil',
        description: err.message || 'Kunne ikke sette aktiv husstand',
        variant: 'destructive',
      })
    },
  },
})

const { mutate: createInvite } = useCreateInvite({
  mutation: {
    onSuccess: () => {
      emit('update:isAddMemberDialogOpen', false)
      emit('memberAdded')
      toast({
        title: 'Invitasjon sendt',
        description: 'Invitasjonen har blitt sendt til brukeren.',
      })
    },
    onError: (error: unknown) => {
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
    onSuccess: () => {
      emit('update:isAddMemberDialogOpen', false)
      emit('memberAdded')
      toast({
        title: 'Gjest lagt til',
        description: 'Gjesten har blitt lagt til i husstanden.',
      })
    },
    onError: (error: unknown) => {
      const err = error as Error
      toast({
        title: 'Feil',
        description: err.message || 'Kunne ikke legge til gjest',
        variant: 'destructive',
      })
    },
  },
})

// Form schemas
const memberFormSchema = toTypedSchema(
  z.object({
    name: z.string().min(1, 'Navn er påkrevd'),
    email: z.string().email('Ugyldig e-postadresse').optional(),
    consumptionFactor: z.number().min(0.1, 'Må være større enn 0').optional(),
  }),
)

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

const { resetForm } = useForm<MemberFormValues>({
  validationSchema: memberFormSchema,
})

// Event handlers
function handleModeChange(mode: 'invite' | 'add') {
  memberMode.value = mode
  resetForm()
}

function handleMeetingPlaceSelected(place: MeetingPlace) {
  selectedMeetingPlace.value = place
  emit('meetingPlaceSelected', place)
}

function handleHouseholdSubmit(values: Record<string, any>) {
  if (!household.value?.id) return

  updateActiveHousehold({
    data: {
      name: values.name,
      address: values.address,
      postalCode: values.postalCode,
      city: values.city,
      latitude: household.value.latitude,
      longitude: household.value.longitude,
    },
  })
}

function handleMemberSubmit(values: Record<string, any>) {
  if (!household.value?.id) return

  if (values.email) {
    createInvite({
      data: {
        householdId: household.value.id,
        invitedEmail: values.email,
      },
    })
  } else {
    if (!values.name || values.consumptionFactor === undefined) {
      toast({
        title: 'Feil',
        description: 'Navn og forbruksfaktor er påkrevd for å legge til gjest.',
        variant: 'destructive',
      })
      return
    }
    addGuest({
      data: {
        name: values.name,
        icon: 'default_guest_icon.png',
        consumptionMultiplier: values.consumptionFactor,
      },
    })
  }
}

function handleChangeActiveHousehold(householdId: string) {
  setActiveHousehold({ data: { householdId } })
}
</script>

<template>
  <!-- Edit Household Dialog -->
  <Dialog
    :open="isEditDialogOpen"
    @update:open="$emit('update:isEditDialogOpen', $event)"
  >
    <DialogContent class="sm:max-w-[525px]">
      <DialogHeader>
        <DialogTitle>Endre husstandsinformasjon</DialogTitle>
        <DialogDescription>
          Oppdater informasjonen om din husstand. Klikk lagre når du er ferdig.
        </DialogDescription>
      </DialogHeader>

      <Form
        v-if="household"
        :validation-schema="householdFormSchema"
        :initial-values="{
          name: household.name,
          address: household.address,
          postalCode: household.postalCode || '',
          city: household.city || '',
        }"
        @submit="handleHouseholdSubmit"
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
          <Button type="button" variant="outline" @click="$emit('update:isEditDialogOpen', false)">
            Avbryt
          </Button>
          <Button type="submit">Lagre endringer</Button>
        </DialogFooter>
      </Form>
    </DialogContent>
  </Dialog>

  <!-- Add Member Dialog -->
  <Dialog
    :open="isAddMemberDialogOpen"
    @update:open="$emit('update:isAddMemberDialogOpen', $event)"
  >
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

        <Form
          v-slot="{ handleSubmit: _handleSubmit }"
          :validation-schema="memberFormSchema"
        >
          <form @submit.prevent="_handleSubmit(handleMemberSubmit)" class="space-y-4">
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
              <Button
                type="button"
                variant="outline"
                @click="$emit('update:isAddMemberDialogOpen', false)"
              >
                Avbryt
              </Button>
              <Button
                type="submit"
                :disabled="isAddingGuest"
              >
                {{ memberMode === 'invite' ? 'Send invitasjon' : 'Legg til' }}
              </Button>
            </DialogFooter>
          </form>
        </Form>
      </div>
    </DialogContent>
  </Dialog>

  <!-- Change Household Dialog -->
  <Dialog
    :open="isChangeHouseholdDialogOpen"
    @update:open="$emit('update:isChangeHouseholdDialogOpen', $event)"
  >
    <DialogContent class="sm:max-w-[425px]">
      <DialogHeader>
        <DialogTitle>Bytt aktiv husstand</DialogTitle>
        <DialogDescription>
          Velg hvilken husstand du vil gjøre aktiv.
        </DialogDescription>
      </DialogHeader>

      <div class="py-4">
        <div v-if="allHouseholds" class="space-y-4">
          <div
            v-for="h in allHouseholds"
            :key="h.id"
            class="flex items-center justify-between p-4 rounded-lg border"
            :class="{
              'bg-primary/5 border-primary': household?.id === h.id,
              'hover:bg-muted/50 cursor-pointer': household?.id !== h.id,
            }"
            @click="household?.id !== h.id && handleChangeActiveHousehold(h.id)"
          >
            <div class="flex items-center gap-2">
              <CheckCircle
                v-if="household?.id === h.id"
                class="h-5 w-5 text-primary"
              />
              <span>{{ h.name }}</span>
            </div>
          </div>
        </div>
      </div>

      <DialogFooter>
        <Button
          type="button"
          variant="outline"
          @click="$emit('update:isChangeHouseholdDialogOpen', false)"
        >
          Lukk
        </Button>
      </DialogFooter>
    </DialogContent>
  </Dialog>

  <!-- Meeting Map Dialog -->
  <Dialog
    :open="isMeetingMapDialogOpen"
    @update:open="$emit('update:isMeetingMapDialogOpen', $event)"
    class="meeting-map-dialog"
  >
    <DialogContent>
      <DialogHeader>
        <DialogTitle>Møtepunkter</DialogTitle>
      </DialogHeader>

      <HouseholdMeetingMap
        v-if="household"
        ref="mapRef"
        :household-latitude="household.latitude"
        :household-longitude="household.longitude"
        :meeting-places="(household.meetingPlaces ?? []).map(place => ({
          ...place,
          position: [place.latitude, place.longitude]
        }))"
        @place-selected="handleMeetingPlaceSelected"
      />
    </DialogContent>
  </Dialog>

  <!-- Preparedness Info Dialog -->
  <PreparednessInfoDialog
    :is-open="isPreparednessInfoDialogOpen"
    @close="$emit('update:isPreparednessInfoDialogOpen', false)"
  />
</template>

<style scoped>
:deep(.meeting-map-dialog) {
  max-width: 1250px;
  width: 95vw;
}
</style>
