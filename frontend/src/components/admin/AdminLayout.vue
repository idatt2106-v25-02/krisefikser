<script setup lang="ts">
import { useRouter } from 'vue-router'
import { Map, BookOpen, Users, Home, FileText } from 'lucide-vue-next'
import { Button } from '@/components/ui/button'

const router = useRouter()

const navigateToMap = () => {
  router.push('/admin/kart')
}

const navigateToDashboard = () => {
  router.push('/admin')
}

const navigateToScenarios = () => {
  router.push('/admin/scenarios')
}

const navigateToArticles = () => {
  router.push('/admin/artikler')
}

const navigateToUsers = () => {
  router.push('/admin/brukere')
}
</script>

<template>
  <div class="flex h-screen bg-gray-50">
    <!-- Sidebar -->
    <div class="w-64 bg-white shadow-md">
      <div class="p-4 border-b mt-2">
        <h1 class="text-xl font-bold text-blue-600">Admin Dashboard</h1>
      </div>
      <nav class="mt-4">
        <Button
          @click="navigateToDashboard"
          variant="ghost"
          :class="[
            'flex items-center w-full px-4 py-3 text-left justify-start rounded-none',
            router.currentRoute.value.path === '/admin'
              ? 'bg-blue-50 text-blue-600 border-r-4 border-blue-600'
              : 'text-gray-700',
          ]"
        >
          <Home class="h-5 w-5" />
          <span class="ml-2">Dashboard</span>
        </Button>

        <Button
          @click="navigateToMap"
          variant="ghost"
          :class="[
            'flex items-center w-full px-4 py-3 text-left justify-start rounded-none',
            router.currentRoute.value.path === '/admin/kart'
              ? 'bg-blue-50 text-blue-600 border-r-4 border-blue-600'
              : 'text-gray-700',
          ]"
        >
          <Map class="h-5 w-5" />
          <span class="ml-2">Kart administrasjon</span>
        </Button>

        <Button
          @click="navigateToScenarios"
          variant="ghost"
          :class="[
            'flex items-center w-full px-4 py-3 text-left justify-start rounded-none',
            router.currentRoute.value.path === '/admin/scenarios'
              ? 'bg-blue-50 text-blue-600 border-r-4 border-blue-600'
              : 'text-gray-700',
          ]"
        >
          <BookOpen class="h-5 w-5" />
          <span class="ml-2">Scenarioer</span>
        </Button>

        <Button
          @click="navigateToArticles"
          variant="ghost"
          :class="[
            'flex items-center w-full px-4 py-3 text-left justify-start rounded-none',
            router.currentRoute.value.path === '/admin/artikler'
              ? 'bg-blue-50 text-blue-600 border-r-4 border-blue-600'
              : 'text-gray-700',
          ]"
        >
          <FileText class="h-5 w-5" />
          <span class="ml-2">Artikler</span>
        </Button>

        <Button
          @click="navigateToUsers"
          variant="ghost"
          :class="[
            'flex items-center w-full px-4 py-3 text-left justify-start rounded-none',
            router.currentRoute.value.path === '/admin/brukere'
              ? 'bg-blue-50 text-blue-600 border-r-4 border-blue-600'
              : 'text-gray-700',
          ]"
        >
          <Users class="h-5 w-5" />
          <span class="ml-2">Brukere</span>
        </Button>
      </nav>
    </div>

    <!-- Main Content -->
    <div class="flex-1 overflow-auto">
      <!-- Page Content -->
      <div class="p-6">
        <slot></slot>
      </div>
    </div>
  </div>
</template>
