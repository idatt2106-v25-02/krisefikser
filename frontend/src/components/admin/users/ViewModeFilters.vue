<script lang="ts" setup>
import { Button } from '@/components/ui/button'
import { Users, UserCog, Home } from 'lucide-vue-next'

const props = defineProps<{
  viewMode: string
}>()

const emit = defineEmits<{
  (e: 'update:viewMode', value: string): void
}>()
</script>

<template>
  <div class="flex space-x-2">
    <Button
      :class="{ 'bg-blue-50 text-blue-600': viewMode === 'all' }"
      variant="ghost"
      @click="emit('update:viewMode', 'all')"
    >
      <Users class="h-4 w-4 mr-1" />
      Alle brukere
    </Button>
    <Button
      :class="{ 'bg-blue-50 text-blue-600': viewMode === 'admins' }"
      variant="ghost"
      @click="emit('update:viewMode', 'admins')"
    >
      <UserCog class="h-4 w-4 mr-1" />
      Kun admins
    </Button>
    <Button
      :class="{ 'bg-blue-50 text-blue-600': viewMode === 'users' }"
      variant="ghost"
      @click="emit('update:viewMode', 'users')"
    >
      <Users class="h-4 w-4 mr-1" />
      Kun brukere
    </Button>
    <Button
      :class="{ 'bg-blue-50 text-blue-600': viewMode === 'households' }"
      variant="ghost"
      @click="emit('update:viewMode', 'households')"
    >
      <Home class="h-4 w-4 mr-1" />
      Husstander
    </Button>
  </div>
</template>
