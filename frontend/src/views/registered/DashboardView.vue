<script setup lang="ts">
import PageLayout from '@/components/layout/PageLayout.vue'
import { User } from 'lucide-vue-next'
import { useAuthStore } from '@/stores/useAuthStore'
import { useMe } from '@/api/generated/authentication/authentication'

// Import new components
import PersonalInfo from '@/components/dashboard/PersonalInfo.vue'
import Settings from '@/components/dashboard/Settings.vue'
import Households from '@/components/dashboard/Households.vue'
import Security from '@/components/dashboard/Security.vue'

// Get auth store
const authStore = useAuthStore()

// Get current user data
const { data: currentUser, isLoading: isLoadingUser } = useMe({
  query: {
    enabled: authStore.isAuthenticated,
    refetchOnMount: true,
    refetchOnWindowFocus: true,
  },
})
</script>

<template>
  <PageLayout pageTitle="Min profil" sectionName="Bruker" :iconComponent="User" iconBgColor="blue">
    <div v-if="isLoadingUser" class="flex justify-center items-center h-64">
      <div class="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-500"></div>
    </div>

    <div v-else-if="currentUser" class="grid grid-cols-1 md:grid-cols-3 gap-8">
      <!-- Main profile information -->
      <div class="md:col-span-2">
        <PersonalInfo />

        <!-- User preferences section -->
        <div class="mt-6">
          <Settings />
        </div>
      </div>

      <!-- Sidebar information -->
      <div class="md:col-span-1">
        <!-- User households -->
        <Households />

        <!-- Password change card -->
        <Security />
      </div>
    </div>

    <div v-else class="text-center py-12">
      <p class="text-gray-500">Kunne ikke laste brukerdata. Vennligst prøv igjen senere.</p>
    </div>
  </PageLayout>
</template>
