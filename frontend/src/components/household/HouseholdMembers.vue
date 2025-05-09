<script setup lang="ts">
import { computed, ref } from 'vue'
import { Button } from '@/components/ui/button'
import { UserMinus } from 'lucide-vue-next'
import { ConfirmationDialog } from '@/components/ui/confirmation-dialog'
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
} from '@/components/ui/dropdown-menu'
import { useToast } from '@/components/ui/toast/use-toast'
import { useQueryClient } from '@tanstack/vue-query'
import {
  getGetActiveHouseholdQueryKey,
  useLeaveHousehold,
  useRemoveGuestFromHousehold,
} from '@/api/generated/household/household'
import type { HouseholdMemberResponse, GuestResponse } from '@/api/generated/model'

const props = defineProps<{
  householdId: string
  members: HouseholdMemberResponse[]
  guests: GuestResponse[]
  currentUserId: string
}>()

const emit = defineEmits<{
  (e: 'addMember'): void
  (e: 'refresh'): void
}>()

const { toast } = useToast()
const queryClient = useQueryClient()
const memberGuestTab = ref<'alle' | 'medlemmer' | 'gjester'>('alle')
const isRemovingGuest = ref(false)
const showRemoveMemberDialog = ref(false)
const showRemoveGuestDialog = ref(false)
const memberToRemove = ref<string | undefined | null>(null)
const guestToRemove = ref<string | null>(null)

const filteredPeople = computed(() => {
  if (memberGuestTab.value === 'alle') {
    return [
      ...props.members.map((m) => ({ type: 'member' as const, data: m })),
      ...props.guests.map((g) => ({ type: 'guest' as const, data: g })),
    ]
  } else if (memberGuestTab.value === 'medlemmer') {
    return props.members.map((m) => ({ type: 'member' as const, data: m }))
  } else {
    return props.guests.map((g) => ({ type: 'guest' as const, data: g }))
  }
})

// Mutations
const { mutate: leaveHousehold } = useLeaveHousehold({
  mutation: {
    onSuccess: () => {
      toast({
        title: 'Medlem fjernet',
        description: 'Medlemmet er fjernet fra husstanden.',
      })
      emit('refresh')
    },
    onError: (error: unknown) => {
      toast({
        title: 'Feil',
        description: (error as Error)?.message || 'Kunne ikke fjerne medlem',
        variant: 'destructive',
      })
    },
  },
})

const { mutate: removeGuest } = useRemoveGuestFromHousehold({
  mutation: {
    onSuccess: () => {
      isRemovingGuest.value = false
      toast({
        title: 'Gjest fjernet',
        description: 'Gjesten er fjernet fra husstanden.',
      })
      emit('refresh')
      queryClient.invalidateQueries({ queryKey: getGetActiveHouseholdQueryKey() })
    },
    onError: (error: unknown) => {
      isRemovingGuest.value = false
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

function handleRemoveMember(userId: string | undefined) {
  if (!props.householdId || !userId) return
  memberToRemove.value = userId
  showRemoveMemberDialog.value = true
}

function confirmRemoveMember() {
  if (!props.householdId || !memberToRemove.value) return
  leaveHousehold({
    data: {
      householdId: props.householdId,
    },
  })
  showRemoveMemberDialog.value = false
  memberToRemove.value = null
}

function handleRemoveGuest(guestId: string) {
  guestToRemove.value = guestId
  showRemoveGuestDialog.value = true
}

function confirmRemoveGuest() {
  if (!guestToRemove.value) return
  isRemovingGuest.value = true
  removeGuest({ guestId: guestToRemove.value })
  showRemoveGuestDialog.value = false
  guestToRemove.value = null
}
</script>

<template>
  <div class="bg-white rounded-lg shadow-sm border border-gray-200 p-6 mb-6">
    <div class="flex justify-between items-center mb-5">
      <h2 class="text-xl font-semibold text-gray-800">Medlemmer og gjester</h2>
      <Button
        variant="outline"
        size="sm"
        @click="$emit('addMember')"
        class="flex items-center gap-1"
      >
        <span class="text-md">+</span>
        <span>Legg til</span>
      </Button>
    </div>

    <div class="flex gap-2 mb-4">
      <Button
        :variant="memberGuestTab === 'alle' ? 'default' : 'outline'"
        size="sm"
        @click="memberGuestTab = 'alle'"
      >
        Alle
      </Button>
      <Button
        :variant="memberGuestTab === 'medlemmer' ? 'default' : 'outline'"
        size="sm"
        @click="memberGuestTab = 'medlemmer'"
      >
        Medlemmer
      </Button>
      <Button
        :variant="memberGuestTab === 'gjester' ? 'default' : 'outline'"
        size="sm"
        @click="memberGuestTab = 'gjester'"
      >
        Gjester
      </Button>
    </div>

    <div
      class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4 max-h-[32rem] overflow-y-auto pr-2"
    >
      <div
        v-for="person in filteredPeople"
        :key="person.type === 'member' ? person.data.user?.id : person.data.id"
        class="bg-gray-50 rounded-lg border border-gray-200 overflow-hidden hover:shadow-md transition-shadow"
      >
        <div class="p-4">
          <div class="flex justify-between items-start">
            <div class="flex items-center mb-3">
              <div
                v-if="person.type === 'member'"
                class="h-10 w-10 bg-blue-100 rounded-full flex items-center justify-center text-blue-600 mr-3 flex-shrink-0"
              >
                <span class="text-md font-medium">
                  {{ person.data.user?.firstName?.[0] ?? '?' }}
                </span>
              </div>
              <div
                v-else
                class="h-10 w-10 bg-green-100 rounded-full flex items-center justify-center text-green-600 mr-3 flex-shrink-0"
              >
                <span class="text-md font-medium">
                  {{ person.data.name?.[0]?.toUpperCase() ?? 'G' }}
                </span>
              </div>
              <h3 class="text-md font-bold text-gray-900">
                {{
                  person.type === 'member'
                    ? person.data.user?.firstName + ' ' + person.data.user?.lastName
                    : person.data.name
                }}
              </h3>
              <span
                class="ml-2 text-xs"
                :class="person.type === 'member' ? 'text-gray-400' : 'text-green-500'"
              >
                {{ person.type === 'member' ? 'Medlem' : 'Gjest' }}
              </span>
            </div>

            <DropdownMenu v-if="person.type === 'member' && person.data.user?.id !== currentUserId">
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
                  @click="handleRemoveMember(person.data.user?.id)"
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
                  @click="handleRemoveGuest(person.data.id)"
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
                {{ person.data.user?.email ?? '' }}
              </div>
            </template>
            <template v-else> Forbruksfaktor: {{ person.data.consumptionMultiplier }} </template>
          </div>
        </div>
      </div>
    </div>

    <!-- Remove Member Confirmation -->
    <ConfirmationDialog
      :is-open="showRemoveMemberDialog"
      title="Fjern medlem"
      description="Er du sikker på at du vil fjerne dette medlemmet?"
      confirm-text="Fjern"
      variant="destructive"
      @confirm="confirmRemoveMember"
      @cancel="showRemoveMemberDialog = false"
    />

    <!-- Remove Guest Confirmation -->
    <ConfirmationDialog
      :is-open="showRemoveGuestDialog"
      title="Fjern gjest"
      description="Er du sikker på at du vil fjerne denne gjesten?"
      confirm-text="Fjern"
      variant="destructive"
      @confirm="confirmRemoveGuest"
      @cancel="showRemoveGuestDialog = false"
    />
  </div>
</template>
