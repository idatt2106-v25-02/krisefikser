<script setup lang="ts">
import { ref } from 'vue'
import PageLayout from '@/components/layout/PageLayout.vue'
import { User } from 'lucide-vue-next'

// Import new components
import PersonalInfo from '@/components/dashboard/PersonalInfo.vue'
import Settings from '@/components/dashboard/Settings.vue'
import Households from '@/components/dashboard/Households.vue'
import Security from '@/components/dashboard/Security.vue'

// Mock user data - in a real app, you would fetch this from your API
const user = ref({
  id: '1',
  name: 'Erik Hansen',
  email: 'erik.hansen@example.com',
  phone: '+47 123 45 678',
  address: 'Kongens gate 1, 0153 Oslo',
  households: [
    { id: '1', name: 'Familien Hansen' },
    { id: '2', name: 'Hytta i Trysil' },
  ],
  preferences: {
    notifications: true,
    emailUpdates: false,
    locationSharing: true,
  },
})

const updateUserInfo = (updatedUser) => {
  user.value = { ...user.value, ...updatedUser }
  // In a real app, you would send the updated user data to your API
}

const updatePreferences = ({ preference, value }) => {
  user.value.preferences[preference] = value
  // In a real app, you would send the updated preferences to your API
}
</script>

<template>
  <PageLayout pageTitle="Min profil" sectionName="Bruker" :iconComponent="User" iconBgColor="blue">
    <div class="grid grid-cols-1 md:grid-cols-3 gap-8">
      <!-- Main profile information -->
      <div class="md:col-span-2">
        <PersonalInfo :user="user" @update:user="updateUserInfo" />

        <!-- User preferences section -->
        <div class="mt-6">
          <Settings :preferences="user.preferences" @update:preferences="updatePreferences" />
        </div>
      </div>

      <!-- Sidebar information -->
      <div class="md:col-span-1">
        <!-- User households -->
        <Households :households="user.households" />

        <!-- Password change card -->
        <Security />
      </div>
    </div>
  </PageLayout>
</template>
