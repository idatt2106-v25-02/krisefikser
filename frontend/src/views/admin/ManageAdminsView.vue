<!-- AdminSection.vue -->
<script setup lang="ts">
import AdminLayout from '@/components/admin/AdminLayout.vue';
import { ref, computed } from 'vue';
import { useRouter } from 'vue-router';
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
  CheckCircle
} from 'lucide-vue-next';

// Import shadcn Button component
import { Button } from '@/components/ui/button';

// Import Dialog components
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from '@/components/ui/dialog';

// Import Alert components
import {
  Alert,
  AlertDescription,
  AlertTitle,
} from '@/components/ui/alert';

// Import custom components
import EditHouseholdDialog from '@/components/admin/EditHouseholdDialog.vue';
import AddMemberDialog from '@/components/admin/AddMemberDialog.vue';
import HouseholdList from '@/components/admin/HouseholdList.vue';
import UserSelect from '@/components/admin/UserSelect.vue';

// Import API hooks
import { useGetAllUsers, useDeleteUser, useUpdateUser } from '@/api/generated/user/user';
import {
  useGetAllHouseholdsAdmin,
  useDeleteHousehold,
  useUpdateHousehold,
  useRemoveMemberFromHousehold,
  useAddMemberToHousehold
} from '@/api/generated/household/household';
import type { UserResponse, CreateUser } from '@/api/generated/model';
import type { HouseholdResponse, CreateHouseholdRequest } from '@/api/generated/model';

// Import auth store
// new comment to force pipeline to run
import { useAuthStore } from '@/stores/useAuthStore';

const router = useRouter();
const authStore = useAuthStore();

// State for dialogs
const showInviteDialog = ref(false);
const showSuccessMessage = ref(false);
const showEditHouseholdDialog = ref(false);
const showAddMemberDialog = ref(false);
const selectedUserId = ref('');
const selectedHousehold = ref<HouseholdResponse | null>(null);

// Fetch users using TanStack Query
const { data: usersData, isLoading: isLoadingUsers, refetch: refetchUsers } = useGetAllUsers<UserResponse[]>();

// Delete user mutation
const { mutate: deleteUserMutation } = useDeleteUser({
  mutation: {
    onSuccess: () => {
      refetchUsers();
    }
  }
});

// Update user mutation
const { mutate: updateUserMutation } = useUpdateUser({
  mutation: {
    onSuccess: () => {
      refetchUsers();
    }
  }
});

// Fetch households using TanStack Query
const { data: householdsData, isLoading: isLoadingHouseholds, refetch: refetchHouseholds } = useGetAllHouseholdsAdmin<HouseholdResponse[]>();

// Delete household mutation
const { mutate: deleteHouseholdMutation } = useDeleteHousehold({
  mutation: {
    onSuccess: () => {
      refetchHouseholds();
    }
  }
});

// Update household mutation
const { mutate: updateHouseholdMutation } = useUpdateHousehold({
  mutation: {
    onSuccess: () => {
      refetchHouseholds();
    }
  }
});

// Remove member mutation
const { mutate: removeMemberMutation } = useRemoveMemberFromHousehold({
  mutation: {
    onSuccess: () => {
      refetchHouseholds();
    }
  }
});

// Add member mutation
const { mutate: addMemberMutation } = useAddMemberToHousehold({
  mutation: {
    onSuccess: () => {
      refetchHouseholds();
    }
  }
});

// View options
const viewMode = ref('all'); // 'all', 'admins', 'users', 'households'
const searchQuery = ref('');

// Filter and group users
const filteredUsers = computed(() => {
  if (!usersData.value) return [];
  let result = [...usersData.value];

  // Apply search filter
  if (searchQuery.value) {
    const query = searchQuery.value.toLowerCase();
    result = result.filter(user =>
      (user.firstName?.toLowerCase().includes(query) || false) ||
      (user.lastName?.toLowerCase().includes(query) || false) ||
      (user.email?.toLowerCase().includes(query) || false)
    );
  }

  // Apply role filter
  if (viewMode.value === 'admins') {
    result = result.filter(user => user.roles?.includes('ADMIN'));
  } else if (viewMode.value === 'users') {
    result = result.filter(user => !user.roles?.includes('ADMIN'));
  }

  return result;
});

// Get selected user data
const selectedUser = computed(() => {
  if (!selectedUserId.value || !usersData.value) return null;
  return usersData.value.find(user => user.id === selectedUserId.value) || null;
});

const deleteItem = (id: string) => {
  if (!id) return;
  deleteUserMutation({ userId: id });
};

// Check if a user can be deleted based on current user role
const canDeleteUser = (userRoles?: string[]) => {
  if (!userRoles) return false;
  // Super Admins can delete Admins and Users, but not other Super Admins
  if (authStore.isSuperAdmin) {
    return !userRoles.includes('SUPER_ADMIN');
  }
  // Regular Admins can only delete Users
  else {
    return !userRoles.includes('ADMIN') && !userRoles.includes('SUPER_ADMIN');
  }
};

// Check if a user can be invited to be admin
const canInviteUser = (userRoles?: string[]) => {
  if (!userRoles) return false;
  // Only regular users can be invited to be admin
  return !userRoles.includes('ADMIN') && !userRoles.includes('SUPER_ADMIN');
};

const sendPasswordResetLink = () => {
  router.push('/admin/reset-passord-link');
};

// Open admin invite dialog for specific user
const openInviteDialog = (userId: string) => {
  selectedUserId.value = userId;
  showInviteDialog.value = true;
};

// Send admin invitation
const sendAdminInvite = () => {
  if (!selectedUserId.value) return;

  // Find the selected user
  const user = usersData.value?.find(u => u.id === selectedUserId.value);
  if (!user) return;

  // Update user to add admin role
  const updateData: CreateUser = {
    email: user.email || '',
    password: '', // Password is required but we don't want to change it
    firstName: user.firstName || '',
    lastName: user.lastName || '',
    notifications: user.notifications,
    emailUpdates: user.emailUpdates,
    locationSharing: user.locationSharing
  };

  updateUserMutation({
    userId: selectedUserId.value,
    data: updateData
  });

  // Close the confirmation dialog
  showInviteDialog.value = false;

  // Show success message
  showSuccessMessage.value = true;

  // Hide success message after 3 seconds
  setTimeout(() => {
    showSuccessMessage.value = false;
    selectedUserId.value = '';
  }, 3000);
};

// Close dialogs
const closeDialogs = () => {
  showInviteDialog.value = false;
  selectedUserId.value = '';
};

// Handle household actions
const handleEditHousehold = (household: HouseholdResponse) => {
  selectedHousehold.value = household;
  showEditHouseholdDialog.value = true;
};

const handleAddMember = (household: HouseholdResponse) => {
  selectedHousehold.value = household;
  showAddMemberDialog.value = true;
};

const handleDeleteHousehold = (id: string) => {
  deleteHouseholdMutation({ id });
};

const handleRemoveMember = (data: { householdId: string; userId: string }) => {
  if (!data.userId) return;
  removeMemberMutation(data);
};

const handleUpdateHousehold = (data: { id: string; data: CreateHouseholdRequest }) => {
  updateHouseholdMutation(data);
};

const handleAddMemberToHousehold = (data: { householdId: string; userId: string }) => {
  addMemberMutation(data);
};

const getRoleClass = (roles?: string[]) => {
  if (!roles) return 'bg-gray-100 text-gray-800';
  if (roles.includes('SUPER_ADMIN')) return 'bg-purple-100 text-purple-800';
  if (roles.includes('ADMIN')) return 'bg-blue-100 text-blue-800';
  return 'bg-green-100 text-green-800';
};

const getRoleDisplay = (roles?: string[]) => {
  if (!roles) return 'User';
  if (roles.includes('SUPER_ADMIN')) return 'Super Admin';
  if (roles.includes('ADMIN')) return 'Admin';
  return 'User';
};
</script>

<template>
  <AdminLayout>
    <div class="p-6">
      <!-- Admin Invite Dialog -->
      <Dialog :open="showInviteDialog" @update:open="val => !val && closeDialogs()">
        <DialogContent class="sm:max-w-md">
          <DialogHeader>
            <DialogTitle>Inviter til admin</DialogTitle>
            <DialogDescription>
              Velg en bruker å invitere til admin:
            </DialogDescription>
          </DialogHeader>
          <div class="mt-4">
            <UserSelect v-model="selectedUserId" />
          </div>
          <DialogFooter class="flex gap-2 mt-4">
            <Button
              variant="outline"
              @click="closeDialogs"
              class="flex-1"
            >
              Nei
            </Button>
            <Button
              variant="default"
              @click="sendAdminInvite"
              class="flex-1"
              :disabled="!selectedUserId"
            >
              Ja
            </Button>
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
          {{ selectedUser?.firstName }} {{ selectedUser?.lastName }} har blitt invitert til å bli admin.
        </AlertDescription>
      </Alert>

      <div class="flex justify-between items-center mb-6">
        <h2 class="text-2xl font-bold text-gray-800">Brukere og admins</h2>

        <!-- Only Super Admin can invite new admins -->
        <div v-if="!authStore.isSuperAdmin" class="text-sm text-gray-500 italic flex items-center">
          <ShieldCheck class="h-4 w-4 mr-1 text-gray-400" />
          Kun Super Admin kan administrere brukere
        </div>

        <div v-else class="flex space-x-3">
          <Button
            variant="default"
            class="flex items-center bg-blue-600 hover:bg-blue-700 text-white"
            @click="router.push('/admin/invite')"
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
      <div v-if="authStore.isSuperAdmin" class="bg-blue-50 border-l-4 border-blue-500 p-4 mb-6">
        <div class="flex">
          <ShieldCheck class="h-5 w-5 text-blue-500 mr-2" />
          <div>
            <p class="font-medium text-blue-700">Super Admin rettigheter aktivert</p>
            <p class="text-sm text-blue-600">Du har tilgang til å invitere nye admin-brukere og sende ut passord-tilbakestillingslinker.</p>
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
              :class="{'bg-blue-50 text-blue-600': viewMode === 'all'}"
              @click="viewMode = 'all'"
            >
              <Users class="h-4 w-4 mr-1" />
              Alle brukere
            </Button>
            <Button
              variant="ghost"
              :class="{'bg-blue-50 text-blue-600': viewMode === 'admins'}"
              @click="viewMode = 'admins'"
            >
              <UserCog class="h-4 w-4 mr-1" />
              Kun admins
            </Button>
            <Button
              variant="ghost"
              :class="{'bg-blue-50 text-blue-600': viewMode === 'users'}"
              @click="viewMode = 'users'"
            >
              <User class="h-4 w-4 mr-1" />
              Kun brukere
            </Button>
            <Button
              variant="ghost"
              :class="{'bg-blue-50 text-blue-600': viewMode === 'households'}"
              @click="viewMode = 'households'"
            >
              <Home class="h-4 w-4 mr-1" />
              Husstander
            </Button>
          </div>
        </div>

        <!-- Loading state -->
        <div v-if="isLoadingUsers" class="p-8 text-center">
          <div class="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-500 mx-auto"></div>
          <p class="mt-2 text-gray-500">Laster brukere...</p>
        </div>

        <!-- User list view (default) -->
        <div v-else-if="viewMode !== 'households'">
          <table class="w-full">
            <thead class="bg-gray-50 text-xs text-gray-700 uppercase">
            <tr>
              <th class="px-4 py-3 text-left">Navn</th>
              <th class="px-4 py-3 text-left">E-post</th>
              <th class="px-4 py-3 text-left">Rolle</th>
              <th class="px-4 py-3 text-center">Handlinger</th>
            </tr>
            </thead>
            <tbody class="divide-y">
            <tr v-for="user in filteredUsers" :key="user.id" class="hover:bg-gray-50">
              <td class="px-4 py-3 font-medium text-gray-800">{{ user.firstName }} {{ user.lastName }}</td>
              <td class="px-4 py-3 text-gray-700">{{ user.email }}</td>
              <td class="px-4 py-3">
                <span :class="`px-2 py-1 rounded-full text-xs font-medium ${getRoleClass(user.roles)}`">
                  {{ getRoleDisplay(user.roles) }}
                </span>
              </td>
              <td class="px-4 py-3">
                <div class="flex justify-center space-x-2">
                  <!-- Mail icon - only for regular users if Super Admin -->
                  <Button
                    v-if="authStore.isSuperAdmin && canInviteUser(user.roles)"
                    variant="ghost"
                    size="icon"
                    class="text-blue-600 hover:text-blue-800 p-1 h-auto"
                    @click="openInviteDialog(user.id || '')"
                    title="Inviter til Admin"
                  >
                    <Mail class="h-4 w-4" />
                  </Button>

                  <!-- Delete button with permission check -->
                  <Button
                    v-if="canDeleteUser(user.roles)"
                    variant="ghost"
                    size="icon"
                    class="text-red-600 hover:text-red-800 p-1 h-auto"
                    @click="deleteItem(user.id || '')"
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

        <!-- Household view -->
        <div v-else>
          <div v-if="isLoadingHouseholds" class="p-8 text-center">
            <div class="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-500 mx-auto"></div>
            <p class="mt-2 text-gray-500">Laster husstander...</p>
          </div>
          <HouseholdList
            v-else
            :households="householdsData || []"
            @edit="handleEditHousehold"
            @addMember="handleAddMember"
            @delete="handleDeleteHousehold"
            @removeMember="handleRemoveMember"
          />
        </div>
      </div>

      <!-- Edit Household Dialog -->
      <EditHouseholdDialog
        v-if="selectedHousehold"
        :household="selectedHousehold"
        :open="showEditHouseholdDialog"
        @update:open="val => showEditHouseholdDialog = val"
        @save="handleUpdateHousehold"
      />

      <!-- Add Member Dialog -->
      <AddMemberDialog
        v-if="selectedHousehold"
        :household="selectedHousehold"
        :open="showAddMemberDialog"
        @update:open="val => showAddMemberDialog = val"
        @add="handleAddMemberToHousehold"
      />
    </div>
  </AdminLayout>
</template>
