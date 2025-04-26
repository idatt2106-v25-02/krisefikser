<!-- AdminSection.vue -->
<script setup lang="ts">
import { ref, computed } from 'vue'
import {
  Mail,
  Trash2,
  ShieldCheck,
  Key,
  Search,
  Users,
  UserCog,
  User,
  Home,
  UserPlus,
  CheckCircle,
} from 'lucide-vue-next'

// Import shadcn Button component
import { Button } from '@/components/ui/button'

// Import Dialog components
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from '@/components/ui/dialog'

// Import Alert components
import { Alert, AlertDescription, AlertTitle } from '@/components/ui/alert'

// Import Accordion components
import {
  Accordion,
  AccordionContent,
  AccordionItem,
  AccordionTrigger,
} from '@/components/ui/accordion'

defineProps({
  isSuperAdmin: {
    type: Boolean,
    default: false,
  },
})

const emit = defineEmits(['navigate'])

// State for dialogs
const showInviteDialog = ref(false)
const showSuccessMessage = ref(false)
const selectedUserId = ref('')

// Mock data for users (admins and regular users)
const allUsers = ref([
  {
    id: '1',
    name: 'Ola Nordmann',
    email: 'ola@example.no',
    role: 'Super Admin',
    lastLogin: '2025-04-23',
    householdId: null,
  },
  {
    id: '2',
    name: 'Kari Hansen',
    email: 'kari@example.no',
    role: 'Admin',
    lastLogin: '2025-04-22',
    householdId: null,
  },
  {
    id: '3',
    name: 'Per Jensen',
    email: 'per@example.no',
    role: 'Admin',
    lastLogin: '2025-04-20',
    householdId: null,
  },
  {
    id: '4',
    name: 'Anne Berg',
    email: 'anne@example.no',
    role: 'User',
    lastLogin: '2025-04-19',
    householdId: 'h1',
  },
  {
    id: '5',
    name: 'Lars Berg',
    email: 'lars@example.no',
    role: 'User',
    lastLogin: '2025-04-18',
    householdId: 'h1',
  },
  {
    id: '6',
    name: 'Emma Solberg',
    email: 'emma@example.no',
    role: 'User',
    lastLogin: '2025-04-17',
    householdId: 'h2',
  },
  {
    id: '7',
    name: 'Thomas Solberg',
    email: 'thomas@example.no',
    role: 'User',
    lastLogin: '2025-04-16',
    householdId: 'h2',
  },
])

// Mock data for households
const households = ref([
  { id: 'h1', name: 'Berg husstand', address: 'Storgata 1, Oslo', members: 2 },
  { id: 'h2', name: 'Solberg husstand', address: 'Lillegata 2, Bergen', members: 2 },
])

// View options
const viewMode = ref('all') // 'all', 'admins', 'users', 'households'
const searchQuery = ref('')

// Filter and group users
const filteredUsers = computed(() => {
  let result = [...allUsers.value]

  // Apply search filter
  if (searchQuery.value) {
    const query = searchQuery.value.toLowerCase()
    result = result.filter(
      (user) => user.name.toLowerCase().includes(query) || user.email.toLowerCase().includes(query),
    )
  }

  // Apply role filter
  if (viewMode.value === 'admins') {
    result = result.filter((user) => user.role === 'Admin' || user.role === 'Super Admin')
  } else if (viewMode.value === 'users') {
    result = result.filter((user) => user.role === 'User')
  }

  return result
})

// Get selected user data
const selectedUser = computed(() => {
  if (!selectedUserId.value) return null
  return allUsers.value.find((user) => user.id === selectedUserId.value) || null
})

// Filter households based on search query
const filteredHouseholds = computed(() => {
  let result = [...households.value]

  if (searchQuery.value) {
    const query = searchQuery.value.toLowerCase()
    result = result.filter(
      (household) =>
        household.name.toLowerCase().includes(query) ||
        household.address.toLowerCase().includes(query) ||
        // Search for household members by name or email
        allUsers.value.some(
          (user) =>
            user.householdId === household.id &&
            (user.name.toLowerCase().includes(query) || user.email.toLowerCase().includes(query)),
        ),
    )
  }

  return result
})

// Group users by household for the household view
const groupedHouseholds = computed(() => {
  const householdMap = new Map()

  // Initialize with household data from filtered households
  filteredHouseholds.value.forEach((household) => {
    householdMap.set(household.id, {
      ...household,
      users: [],
    })
  })

  // Add users to their households
  allUsers.value.forEach((user) => {
    if (user.householdId && householdMap.has(user.householdId)) {
      householdMap.get(user.householdId).users.push(user)
    }
  })

  return Array.from(householdMap.values())
})

const deleteItem = (id: string) => {
  console.log(`Deleting user with ID: ${id}`)
  // Implementation would connect to actual backend
  const index = allUsers.value.findIndex((user) => user.id === id)
  if (index !== -1) {
    allUsers.value.splice(index, 1)
  }
}

// Check if a user can be deleted based on current user role
const canDeleteUser = (userRole: string) => {
  // Super Admins can delete Admins and Users, but not other Super Admins
  if (props.isSuperAdmin) {
    return userRole !== 'Super Admin'
  }
  // Regular Admins can only delete Users
  else {
    return userRole === 'User'
  }
}

// Check if a user can be invited to be admin
const canInviteUser = (userRole: string) => {
  // Only regular users can be invited to be admin
  return userRole === 'User'
}

const sendPasswordResetLink = () => {
  emit('navigate', '/admin/reset-passord-link')
}

// Open admin invite dialog for specific user
const openInviteDialog = (userId: string) => {
  selectedUserId.value = userId
  showInviteDialog.value = true
}

// Send admin invitation
const sendAdminInvite = () => {
  if (!selectedUser.value) return

  // In a real app, this would call an API to send the invite
  console.log(`Sending admin invitation to ${selectedUser.value.email}`)

  // Close the confirmation dialog
  showInviteDialog.value = false

  // Show success message
  showSuccessMessage.value = true

  // Hide success message after 3 seconds
  setTimeout(() => {
    showSuccessMessage.value = false
    selectedUserId.value = ''
  }, 3000)
}

// Close dialogs
const closeDialogs = () => {
  showInviteDialog.value = false
  selectedUserId.value = ''
}

const navigateToHousehold = (householdId: string) => {
  emit('navigate', `/admin/husstand/${householdId}`)
}

const getRoleClass = (role: string) => {
  switch (role) {
    case 'Super Admin':
      return 'bg-purple-100 text-purple-800'
    case 'Admin':
      return 'bg-blue-100 text-blue-800'
    case 'User':
      return 'bg-green-100 text-green-800'
    default:
      return 'bg-gray-100 text-gray-800'
  }
}
</script>

<template>
  <div class="p-6">
    <!-- Admin Invite Dialog -->
    <Dialog :open="showInviteDialog" @update:open="(val) => !val && closeDialogs()">
      <DialogContent class="sm:max-w-md">
        <DialogHeader>
          <DialogTitle>Inviter til admin</DialogTitle>
          <DialogDescription v-if="selectedUser">
            Vil du invitere {{ selectedUser.name }} til å bli admin?
          </DialogDescription>
        </DialogHeader>
        <DialogFooter class="flex gap-2 mt-4">
          <Button variant="outline" @click="closeDialogs" class="flex-1"> Nei </Button>
          <Button variant="default" @click="sendAdminInvite" class="flex-1"> Ja </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>

    <!-- Success alert -->
    <Alert
      v-if="showSuccessMessage && selectedUser"
      class="fixed bottom-4 right-4 w-96 bg-green-50 border-green-400 text-green-800 shadow-lg z-50"
    >
      <CheckCircle class="h-4 w-4" />
      <AlertTitle>Invitasjon sendt!</AlertTitle>
      <AlertDescription>
        {{ selectedUser.name }} har blitt invitert til å bli admin.
      </AlertDescription>
    </Alert>

    <div class="flex justify-between items-center mb-6">
      <h2 class="text-2xl font-bold text-gray-800">Brukere og admins</h2>

      <!-- Only Super Admin can invite new admins -->
      <div v-if="!isSuperAdmin" class="text-sm text-gray-500 italic flex items-center">
        <ShieldCheck class="h-4 w-4 mr-1 text-gray-400" />
        Kun Super Admin kan administrere brukere
      </div>

      <div v-else class="flex space-x-3">
        <Button
          variant="default"
          class="flex items-center bg-blue-600 hover:bg-blue-700 text-white"
          @click="emit('navigate', '/admin/invite')"
        >
          <UserPlus class="h-4 w-4 mr-1" />
          Inviter ny admin
        </Button>
        <Button
          variant="default"
          class="flex items-center bg-green-600 hover:bg-green-700 text-white"
          @click="sendPasswordResetLink"
        >
          <Key class="h-4 w-4 mr-1" />
          Send passord-link
        </Button>
      </div>
    </div>

    <!-- Super Admin privileges notice -->
    <div v-if="isSuperAdmin" class="bg-blue-50 border-l-4 border-blue-500 p-4 mb-6">
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

    <!-- Filters and search -->
    <div class="bg-white rounded-lg shadow overflow-hidden mb-6">
      <div class="p-4 flex flex-wrap justify-between items-center border-b">
        <div class="flex items-center rounded-lg bg-gray-100 px-3 py-2 w-full md:w-64">
          <Search class="h-4 w-4 text-gray-500" />
          <input
            v-model="searchQuery"
            type="text"
            placeholder="Søk brukere..."
            class="bg-transparent border-0 outline-none ml-2 text-gray-700 w-full"
          />
        </div>

        <div class="flex space-x-2 mt-4 md:mt-0">
          <Button
            variant="ghost"
            :class="{ 'bg-blue-50 text-blue-600': viewMode === 'all' }"
            @click="viewMode = 'all'"
          >
            <Users class="h-4 w-4 mr-1" />
            Alle brukere
          </Button>
          <Button
            variant="ghost"
            :class="{ 'bg-blue-50 text-blue-600': viewMode === 'admins' }"
            @click="viewMode = 'admins'"
          >
            <UserCog class="h-4 w-4 mr-1" />
            Kun admins
          </Button>
          <Button
            variant="ghost"
            :class="{ 'bg-blue-50 text-blue-600': viewMode === 'users' }"
            @click="viewMode = 'users'"
          >
            <User class="h-4 w-4 mr-1" />
            Kun brukere
          </Button>
          <Button
            variant="ghost"
            :class="{ 'bg-blue-50 text-blue-600': viewMode === 'households' }"
            @click="viewMode = 'households'"
          >
            <Home class="h-4 w-4 mr-1" />
            Husstander
          </Button>
        </div>
      </div>

      <!-- User list view (default) -->
      <div v-if="viewMode !== 'households'">
        <table class="w-full">
          <thead class="bg-gray-50 text-xs text-gray-700 uppercase">
            <tr>
              <th class="px-4 py-3 text-left">Navn</th>
              <th class="px-4 py-3 text-left">E-post</th>
              <th class="px-4 py-3 text-left">Rolle</th>
              <th class="px-4 py-3 text-left">Sist innlogget</th>
              <th class="px-4 py-3 text-center">Handlinger</th>
            </tr>
          </thead>
          <tbody class="divide-y">
            <tr v-for="user in filteredUsers" :key="user.id" class="hover:bg-gray-50">
              <td class="px-4 py-3 font-medium text-gray-800">{{ user.name }}</td>
              <td class="px-4 py-3 text-gray-700">{{ user.email }}</td>
              <td class="px-4 py-3">
                <span
                  :class="`px-2 py-1 rounded-full text-xs font-medium ${getRoleClass(user.role)}`"
                >
                  {{ user.role }}
                </span>
              </td>
              <td class="px-4 py-3 text-gray-700">{{ user.lastLogin }}</td>
              <td class="px-4 py-3">
                <div class="flex justify-center space-x-2">
                  <!-- Mail icon - only for regular users if Super Admin -->
                  <Button
                    v-if="isSuperAdmin && canInviteUser(user.role)"
                    variant="ghost"
                    size="icon"
                    class="text-blue-600 hover:text-blue-800 p-1 h-auto"
                    @click="openInviteDialog(user.id)"
                    title="Inviter til Admin"
                  >
                    <Mail class="h-4 w-4" />
                  </Button>

                  <!-- Only Super Admin can delete users that aren't Super Admin -->
                  <Button
                    v-if="isSuperAdmin && user.role !== 'Super Admin'"
                    variant="ghost"
                    size="icon"
                    class="text-red-600 hover:text-red-800 p-1 h-auto"
                    @click="deleteItem(user.id)"
                  >
                    <Trash2 class="h-4 w-4" />
                  </Button>
                </div>
              </td>
            </tr>
                <!-- Delete button with permission check -->
                <Button
                  v-if="canDeleteUser(user.role)"
                  variant="ghost"
                  size="icon"
                  class="text-red-600 hover:text-red-800 p-1 h-auto"
                  @click="deleteItem(user.id)"
                  title="Slett bruker"
                >
                  <Trash2 class="h-4 w-4" />
                </Button>
              </div>
            </td>
          </tr>
          </tbody>
        </table>
      </div>

      <!-- Household view with accordion -->
      <div v-else class="p-2">
        <div v-if="groupedHouseholds.length === 0" class="text-center py-8 text-gray-500">
          Ingen husstander funnet som matcher søket.
        </div>
        <Accordion v-else type="single" collapsible class="w-full">
          <AccordionItem
            v-for="household in groupedHouseholds"
            :key="household.id"
            :value="household.id"
            class="border border-gray-200 rounded-md mb-2 overflow-hidden"
          >
            <AccordionTrigger class="px-4 py-3 hover:bg-gray-50">
              <div class="flex items-center w-full text-left">
                <div>
                  <div class="font-medium text-gray-800">{{ household.name }}</div>
                  <div class="text-sm text-gray-500">
                    {{ household.address }} - {{ household.members }} medlemmer
                  </div>
                </div>
                <Button
                  variant="outline"
                  size="sm"
                  class="ml-auto mr-4 text-blue-600"
                  @click.stop="navigateToHousehold(household.id)"
                >
                  Se detaljer
                </Button>
              </div>
            </AccordionTrigger>
            <AccordionContent>
              <div class="px-4 py-2 bg-gray-50">
                <table class="w-full">
                  <thead class="bg-gray-100 text-xs text-gray-700">
                    <tr>
                      <th class="px-4 py-2 text-left rounded-l-md">Navn</th>
                      <th class="px-4 py-2 text-left">E-post</th>
                      <th class="px-4 py-2 text-left rounded-r-md">Sist innlogget</th>
                    </tr>
                  </thead>
                  <tbody class="divide-y divide-gray-200">
                    <tr v-for="user in household.users" :key="user.id" class="hover:bg-gray-100">
                      <td class="px-4 py-2 text-sm font-medium text-gray-800">{{ user.name }}</td>
                      <td class="px-4 py-2 text-sm text-gray-700">{{ user.email }}</td>
                      <td class="px-4 py-2 text-sm text-gray-700">{{ user.lastLogin }}</td>
                    </tr>
                  </tbody>
                </table>
              </div>
            </AccordionContent>
          </AccordionItem>
        </Accordion>
      </div>
    </div>
  </div>
</template>
