<script setup lang="ts">
import { Button } from '@/components/ui/button'
import { MapPin, Edit, RefreshCw, BookText } from 'lucide-vue-next'
import type { HouseholdResponse } from '@/api/generated/model'

defineProps<{
  household: HouseholdResponse
  allHouseholdsCount: number
}>()

defineEmits<{
  (e: 'edit'): void
  (e: 'changeHousehold'): void
  (e: 'showLocation'): void
}>()
</script>

<template>
  <div class="bg-white rounded-lg shadow-sm border border-gray-200 p-6 mb-6">
    <div class="flex flex-col md:flex-row md:items-start md:justify-between">
      <div>
        <h1 class="text-4xl font-bold text-gray-900 mb-1">{{ household.name }}</h1>
        <div class="text-gray-600">
          <div class="flex items-center">
            <span
              class="flex items-center cursor-pointer group"
              @click="$emit('showLocation')"
              title="Vis på kart"
            >
              <MapPin
                class="h-4 w-4 mr-1 flex-shrink-0 text-gray-400 group-hover:text-blue-600 transition-colors"
              />
              <span
                class="transition-colors group-hover:text-blue-600 group-hover:underline text-gray-600"
              >
                {{ household.address }}, {{ household.postalCode }} {{ household.city }}
              </span>
            </span>
          </div>
        </div>
      </div>
      <div class="mt-4 md:mt-0 flex flex-col items-end space-y-2">
        <div class="flex flex-row space-x-2">
          <router-link
            to="/husstand/refleksjoner"
            class="inline-flex items-center justify-center rounded-md text-sm font-medium ring-offset-background transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 disabled:pointer-events-none disabled:opacity-50 border border-input bg-background hover:bg-accent hover:text-accent-foreground h-9 px-4 py-2"
          >
            <BookText class="h-4 w-4 mr-2" />
            Husstandens Refleksjoner
          </router-link>
          <Button variant="outline" class="flex items-center" size="sm" @click="$emit('edit')">
            <Edit class="h-4 w-4 mr-2" />
            Endre informasjon
          </Button>
        </div>
        <Button
          variant="outline"
          size="sm"
          @click="$emit('changeHousehold')"
          class="flex items-center"
          :disabled="allHouseholdsCount <= 1"
        >
          <RefreshCw class="h-4 w-4 mr-2" />
          Bytt husstand
        </Button>
      </div>
    </div>
  </div>
</template>
