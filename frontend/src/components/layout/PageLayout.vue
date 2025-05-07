<script setup lang="ts">
import { Home, ChevronRight } from 'lucide-vue-next'
import { computed, type Component } from 'vue'

type Color = 'blue' | 'yellow' | 'green'

// Define props using Vue's Component type
interface Props {
  pageTitle: string
  sectionName: string
  iconComponent: Component
  iconBgColor?: Color
}

const props = withDefaults(defineProps<Props>(), {
  iconBgColor: 'blue',
})

// Computed properties
const iconBackgroundClass = computed(() => {
  const colors: Record<Color, string> = {
    blue: 'bg-blue-100',
    yellow: 'bg-yellow-100',
    green: 'bg-green-100',
  }
  return colors[props.iconBgColor]
})

const iconTextClass = computed(() => {
  const colors: Record<Color, string> = {
    blue: 'text-blue-600',
    yellow: 'text-yellow-600',
    green: 'text-green-600',
  }
  return colors[props.iconBgColor]
})
</script>
<template>
  <div class="min-h-screen flex flex-col bg-slate-50 overflow-hidden">
    <!-- Page content -->
    <div class="flex-grow container mx-auto px-4 py-8">
      <!-- Breadcrumb navigation -->
      <div class="mb-8">
        <nav class="flex" aria-label="Breadcrumb">
          <ol class="inline-flex items-center space-x-1 md:space-x-3">
            <li class="inline-flex items-center">
              <router-link
                to="/"
                class="inline-flex items-center text-sm font-medium text-gray-700 hover:text-blue-600"
              >
                <Home class="w-4 h-4 mr-2" />
                Hjem
              </router-link>
            </li>
            <li>
              <div class="flex items-center">
                <ChevronRight class="w-4 h-4 text-gray-400" />
                <span class="ml-1 text-sm font-medium text-gray-500 md:ml-2">{{
                  sectionName
                }}</span>
              </div>
            </li>
          </ol>
        </nav>
      </div>

      <!-- Header -->
      <div class="flex items-center mb-8">
        <div :class="[iconBackgroundClass, 'p-3 rounded-full mr-4']">
          <component :is="iconComponent" class="h-8 w-8" :class="iconTextClass" />
        </div>
        <h1 class="text-3xl font-bold text-gray-800">{{ pageTitle }}</h1>
      </div>

      <!-- Content slots -->
      <slot></slot>
    </div>
  </div>
</template>
