<!-- AdminDashboard.vue -->
<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'

// Import our component for the main dashboard view
import DashboardSection from '@/components/admin/DashboardSection.vue'
import AdminLayout from '@/components/admin/AdminLayout.vue'

const router = useRouter()
const activeSection = ref('dashboard')

const navigateToMap = () => {
  router.push('/admin/kart')
}
</script>

<template>
  <AdminLayout>
    <!-- Dynamic section content -->
    <DashboardSection
      v-if="activeSection === 'dashboard'"
      @navigate-to-map="navigateToMap"
      @switch-section="activeSection = $event"
    />
  </AdminLayout>
</template>
