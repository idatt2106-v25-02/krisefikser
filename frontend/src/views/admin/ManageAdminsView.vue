<!-- AdminSection.vue -->
<script setup lang="ts">
import AdminLayout from '@/components/admin/AdminLayout.vue';
import UserManagementComponent from '@/components/dashboard/UserManagement.vue';

// Import API hooks
import { useGetAllUsers, useDeleteUser, useUpdateUser } from '@/api/generated/user/user';
import {
  useGetAllHouseholdsAdmin,
  useDeleteHousehold,
  useUpdateHousehold,
  useRemoveMemberFromHousehold,
  useAddMemberToHousehold
} from '@/api/generated/household/household';
import type { UserResponse } from '@/api/generated/model';
import type { HouseholdResponse } from '@/api/generated/model';

const props = defineProps({
  isSuperAdmin: {
    type: Boolean,
    default: false
  }
});

const emit = defineEmits(['navigate']);

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

// Handle navigation
const handleNavigate = (path: string) => {
  emit('navigate', path);
};
</script>

<template>
  <AdminLayout>
    <UserManagementComponent
      :isSuperAdmin="isSuperAdmin"
      :usersData="usersData || []"
      :householdsData="householdsData || []"
      :isLoadingUsers="isLoadingUsers"
      :isLoadingHouseholds="isLoadingHouseholds"
      pageTitle="Brukere og admins"
      :showHouseholdTab="true"
      @navigate="handleNavigate"
      @deleteUser="deleteUserMutation"
      @updateUser="updateUserMutation"
      @deleteHousehold="deleteHouseholdMutation"
      @updateHousehold="updateHouseholdMutation"
      @removeMember="removeMemberMutation"
      @addMember="addMemberMutation"
      @refetchUsers="refetchUsers"
      @refetchHouseholds="refetchHouseholds"
    />
  </AdminLayout>
</template>