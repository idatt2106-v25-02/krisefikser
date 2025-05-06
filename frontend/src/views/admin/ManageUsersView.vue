<!-- OtherSection.vue -->
<script setup lang="ts">
import AdminLayout from '@/components/admin/AdminLayout.vue';
import UserManagementComponent from '@/components/dashboard/UserManagement.vue';
import { computed } from 'vue';

// Import the same API hooks as AdminSection
import { useGetAllUsers, useDeleteUser, useUpdateUser } from '@/api/generated/user/user';
import type { UserResponse } from '@/api/generated/model';

const props = defineProps({
  isSuperAdmin: {
    type: Boolean,
    default: false
  }
});

const emit = defineEmits(['navigate']);

// Fetch all users using the same hook as AdminSection
const { data: allUsersData, isLoading: isLoadingUsers, refetch: refetchUsers } = useGetAllUsers<UserResponse[]>();

// Filter users to only include those with the regular user role (not admin or super_admin)
const filteredUsersData = computed(() => {
  if (!allUsersData.value) return [];

  return allUsersData.value.filter(user => {
    // Only include users that don't have ADMIN or SUPER_ADMIN roles
    return !user.roles?.includes('ADMIN') && !user.roles?.includes('SUPER_ADMIN');
  });
});

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

// Handle navigation
const handleNavigate = (path: string) => {
  emit('navigate', path);
};
</script>

<template>
  <AdminLayout>
    <UserManagementComponent
      :isSuperAdmin="isSuperAdmin"
      :usersData="filteredUsersData"
      :householdsData="[]"
      :isLoadingUsers="isLoadingUsers"
      :isLoadingHouseholds="false"
      pageTitle="Vanlige brukere"
      :showHouseholdTab="false"
      @navigate="handleNavigate"
      @deleteUser="deleteUserMutation"
      @updateUser="updateUserMutation"
      @refetchUsers="refetchUsers"
    />
  </AdminLayout>
</template>