<!-- AdminSection.vue -->
<script lang="ts" setup>
import AdminLayout from '@/components/admin/AdminLayout.vue'
import { computed, ref } from 'vue'
import { ShieldCheck } from 'lucide-vue-next'

// Import shadcn components
import { Button } from '@/components/ui/button'
import { Dialog, DialogContent, DialogDescription, DialogFooter, DialogHeader, DialogTitle } from '@/components/ui/dialog'
import { Alert, AlertDescription, AlertTitle } from '@/components/ui/alert'

// Import our components
import PageHeader from '@/components/ui/page-header/PageHeader.vue'
import SearchBar from '@/components/ui/search/SearchBar.vue'
import ContentCard from '@/components/ui/card/ContentCard.vue'
import HouseholdList from '@/components/admin/users/HouseholdList.vue'
import UserSelect from '@/components/admin/users/UserSelect.vue'
import AdminActionsHeader from '@/components/admin/users/AdminActionsHeader.vue'
import ViewModeFilters from '@/components/admin/users/ViewModeFilters.vue'
import UserTable from '@/components/admin/users/UserTable.vue'

// Import API hooks
import { useDeleteUser, useGetAllUsers, useUpdateUser } from '@/api/generated/user/user'
import { useGetAllHouseholdsAdmin } from '@/api/generated/household/household'
import type { CreateUser, HouseholdResponse, UserResponse } from '@/api/generated/model'
import { useAuthStore } from '@/stores/auth/useAuthStore'

const authStore = useAuthStore()

// State
const showInviteDialog = ref(false)
const showSuccessMessage = ref(false)
const selectedUserId = ref('')
const viewMode = ref('all')
const searchQuery = ref('')

// Fetch data
const {
  data: usersData,
  isLoading: isLoadingUsers,
  refetch: refetchUsers,
} = useGetAllUsers<UserResponse[]>()

const { mutate: deleteUserMutation } = useDeleteUser({
  mutation: {
    onSuccess: () => {
      refetchUsers()
    },
  },
})

const { mutate: updateUserMutation } = useUpdateUser({
  mutation: {
    onSuccess: () => {
      refetchUsers()
    },
  },
})

const {
  data: householdsData,
  isLoading: isLoadingHouseholds,
  error: householdsError,
} = useGetAllHouseholdsAdmin<HouseholdResponse[]>({
  query: {
    enabled: computed(() => authStore.isAdmin),
    retry: false,
  },
})

// Computed
const filteredUsers = computed(() => {
  if (!usersData.value) return []
  let result = [...usersData.value]

  if (searchQuery.value) {
    const query = searchQuery.value.toLowerCase()
    result = result.filter(
      (user) =>
        user.firstName?.toLowerCase().includes(query) ||
        false ||
        user.lastName?.toLowerCase().includes(query) ||
        false ||
        user.email?.toLowerCase().includes(query) ||
        false,
    )
  }

  if (viewMode.value === 'admins') {
    result = result.filter(
      (user) => user.roles?.includes('ADMIN') || user.roles?.includes('SUPER_ADMIN'),
    )
  } else if (viewMode.value === 'users') {
    result = result.filter(
      (user) => !user.roles?.includes('ADMIN') && !user.roles?.includes('SUPER_ADMIN'),
    )
  }

  return result
})

const filteredHouseholds = computed(() => {
  if (!householdsData.value) return []
  let result = [...householdsData.value]

  if (searchQuery.value) {
    const query = searchQuery.value.toLowerCase()
    result = result.filter((household) => {
      if (household.name?.toLowerCase().includes(query)) return true
      return household.members?.some(
        (member) =>
          member.user.firstName?.toLowerCase().includes(query) ||
          false ||
          member.user.lastName?.toLowerCase().includes(query) ||
          false ||
          member.user.email?.toLowerCase().includes(query) ||
          false,
      )
    })
  }
  return result
})

// Methods
const sendAdminInvite = () => {
  if (!selectedUserId.value) return

  const user = usersData.value?.find((u) => u.id === selectedUserId.value)
  if (!user) return

  const updateData: CreateUser = {
    email: user.email || '',
    password: '',
    firstName: user.firstName || '',
    lastName: user.lastName || '',
    notifications: user.notifications,
    emailUpdates: user.emailUpdates,
    locationSharing: user.locationSharing,
  }

  updateUserMutation({
    userId: selectedUserId.value,
    data: updateData,
  })

  showInviteDialog.value = false
  showSuccessMessage.value = true

  setTimeout(() => {
    showSuccessMessage.value = false
    selectedUserId.value = ''
  }, 3000)
}

const closeDialogs = () => {
  showInviteDialog.value = false
  selectedUserId.value = ''
}
</script>

<template>
  <AdminLayout>
    <div class="p-6">
      <Dialog :open="showInviteDialog" @update:open="(val) => !val && closeDialogs()">
        <DialogContent class="sm:max-w-md">
          <DialogHeader>
            <DialogTitle>Inviter til admin</DialogTitle>
            <DialogDescription> Velg en bruker å invitere til admin:</DialogDescription>
          </DialogHeader>
          <div class="mt-4">
            <UserSelect v-model="selectedUserId" />
          </div>
          <DialogFooter class="flex gap-2 mt-4">
            <Button class="flex-1" variant="outline" @click="closeDialogs"> Nei</Button>
            <Button
              :disabled="!selectedUserId"
              class="flex-1"
              variant="default"
              @click="sendAdminInvite"
            >
              Ja
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>

      <Alert
        v-if="showSuccessMessage"
        class="fixed bottom-4 right-4 w-96 bg-green-50 border-green-400 text-green-800 shadow-lg z-50"
      >
        <AlertTitle>Invitasjon sendt!</AlertTitle>
        <AlertDescription>
          Brukeren har blitt invitert til å bli admin.
        </AlertDescription>
      </Alert>

      <PageHeader title="Brukere og admins">
        <template #actions>
          <AdminActionsHeader />
        </template>
      </PageHeader>

      <div v-if="authStore.isSuperAdmin" class="bg-blue-50 border-l-4 border-blue-500 p-4 mb-6">
        <div class="flex">
          <ShieldCheck class="h-5 w-5 text-blue-500 mr-2" />
          <div>
            <p class="font-medium text-blue-700">Super Admin rettigheter aktivert</p>
            <p class="text-sm text-blue-600">
              Du har tilgang til å invitere nye admin-brukere og sende ut
              passord-tilbakestillingslinker.
            </p>
          </div>
        </div>
      </div>

      <ContentCard>
        <div class="p-4 flex flex-wrap justify-between items-center border-b">
          <SearchBar v-model="searchQuery" placeholder="Søk brukere..." />
          <ViewModeFilters v-model:viewMode="viewMode" />
        </div>

        <div v-if="viewMode !== 'households'">
          <UserTable
            :users="filteredUsers"
            :is-loading="isLoadingUsers"
            @delete="deleteUserMutation({ userId: $event })"
            @invite="(id) => { selectedUserId = id; showInviteDialog = true }"
          />
        </div>

        <div v-else>
          <div v-if="isLoadingHouseholds" class="p-8 text-center">
            <div class="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-500 mx-auto"></div>
            <p class="mt-2 text-gray-500">Laster husstander...</p>
          </div>
          <div v-else-if="householdsError" class="p-8 text-center">
            <p class="text-red-500">Kunne ikke laste husstander</p>
            <p class="text-sm text-gray-400 mt-2">Sjekk at du har admin-tilgang</p>
          </div>
          <HouseholdList v-else :households="filteredHouseholds" />
        </div>
      </ContentCard>
    </div>
  </AdminLayout>
</template>
