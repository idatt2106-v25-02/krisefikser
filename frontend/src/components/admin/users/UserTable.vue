<script lang="ts" setup>
import { Button } from '@/components/ui/button'
import { Mail, Trash2 } from 'lucide-vue-next'
import DataTable from '@/components/ui/data-table/DataTable.vue'
import type { UserResponse } from '@/api/generated/model'
import { useAuthStore } from '@/stores/auth/useAuthStore'

const authStore = useAuthStore()

const props = defineProps<{
  users: UserResponse[]
  isLoading: boolean
}>()

const emit = defineEmits<{
  (e: 'delete', id: string): void
  (e: 'invite', id: string): void
}>()

const userTableColumns = [
  { key: 'name', label: 'Navn', align: 'left' as const },
  { key: 'email', label: 'E-post', align: 'left' as const },
  { key: 'role', label: 'Rolle', align: 'left' as const },
  { key: 'actions', label: 'Handlinger', align: 'center' as const },
]

const getRoleClass = (roles?: string[]) => {
  if (!roles) return 'bg-gray-100 text-gray-800'
  if (roles.includes('SUPER_ADMIN')) return 'bg-purple-100 text-purple-800'
  if (roles.includes('ADMIN')) return 'bg-blue-100 text-blue-800'
  return 'bg-green-100 text-green-800'
}

const getRoleDisplay = (roles?: string[]) => {
  if (!roles) return 'User'
  if (roles.includes('SUPER_ADMIN')) return 'Super Admin'
  if (roles.includes('ADMIN')) return 'Admin'
  return 'User'
}

const canDeleteUser = (userRoles?: string[]) => {
  if (!userRoles) return false
  if (authStore.isSuperAdmin) {
    return !userRoles.includes('SUPER_ADMIN')
  }
  return !userRoles.includes('ADMIN') && !userRoles.includes('SUPER_ADMIN')
}

const canInviteUser = (userRoles?: string[]) => {
  if (!userRoles) return false
  return !userRoles.includes('ADMIN') && !userRoles.includes('SUPER_ADMIN')
}
</script>

<template>
  <DataTable
    :columns="userTableColumns"
    :is-loading="isLoading"
    empty-message="Ingen brukere funnet"
  >
    <tr v-for="user in users" :key="user.id" class="hover:bg-gray-50">
      <td class="px-4 py-4 whitespace-nowrap text-sm font-medium text-gray-900">
        {{ user.firstName }} {{ user.lastName }}
      </td>
      <td class="px-4 py-4 whitespace-nowrap text-sm text-gray-500">
        {{ user.email }}
      </td>
      <td class="px-4 py-4 whitespace-nowrap text-sm">
        <span :class="`px-2 py-1 rounded-full text-xs font-medium ${getRoleClass(user.roles)}`">
          {{ getRoleDisplay(user.roles) }}
        </span>
      </td>
      <td class="px-6 py-4 whitespace-nowrap text-sm text-center">
        <div class="flex justify-center space-x-2 min-w-[56px]">
          <span v-if="authStore.isSuperAdmin && canInviteUser(user.roles)">
            <Button
              class="text-blue-600 hover:text-blue-800 p-1 h-auto"
              size="icon"
              title="Inviter til Admin"
              variant="ghost"
              @click="emit('invite', user.id || '')"
            >
              <Mail class="h-4 w-4" />
            </Button>
          </span>
          <span v-else class="inline-block w-8"></span>
          <Button
            v-if="canDeleteUser(user.roles)"
            class="text-red-600 hover:text-red-800 p-1 h-auto"
            size="icon"
            title="Slett bruker"
            variant="ghost"
            @click="emit('delete', user.id || '')"
          >
            <Trash2 class="h-4 w-4" />
          </Button>
        </div>
      </td>
    </tr>
  </DataTable>
</template>
