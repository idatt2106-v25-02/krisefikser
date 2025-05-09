<script lang="ts" setup>
import { ref } from 'vue'
import { type FilterOptions } from '@/components/map/useMap'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Label } from '@/components/ui/label'
import { Switch } from '@/components/ui/switch'
import { Button } from '@/components/ui/button'
import { ChevronDown, ChevronUp, Home, User } from 'lucide-vue-next'

const filterOptions = ref<FilterOptions>({
  eventEnabled: true,
  shelterEnabled: true,
  meetingPointEnabled: true,
  householdMemberEnabled: true,
  homeEnabled: true,
  userEnabled: true,
  otherEnabled: true,
})

const isExpanded = ref(true)
const isLegendExpanded = ref(true)
const userInCrisisZone = ref(false)

const toggleExpanded = () => {
  isExpanded.value = !isExpanded.value
}

const toggleLegendExpanded = () => {
  isLegendExpanded.value = !isLegendExpanded.value
}

type FilterItem = {
  key: keyof FilterOptions
  label: string
}

const filterItems: FilterItem[] = [
  { key: 'eventEnabled', label: 'Hendelser' },
  { key: 'shelterEnabled', label: 'Tilfluktsrom' },
  { key: 'meetingPointEnabled', label: 'Møteplasser' },
  { key: 'householdMemberEnabled', label: 'Husstandsmedlemmer' },
  { key: 'homeEnabled', label: 'Hjem' },
  { key: 'userEnabled', label: 'Min Posisjon' },
  { key: 'otherEnabled', label: 'Andre' },
]

const props = defineProps<{
  filterMarkers: (options: FilterOptions) => void
}>()

const handleToggle = (option: keyof FilterOptions, value: boolean) => {
  filterOptions.value[option] = value
  props.filterMarkers(filterOptions.value)
}
</script>

<template>
  <div class="absolute top-5 right-5 z-10 flex flex-col gap-2.5">
    <!-- Filter Card -->
    <Card
      :class="{
        'card-expanded': isExpanded,
        'card-collapsed': !isExpanded,
        'gap-0': !isExpanded,
        'gap-6': isExpanded,
      }"
      class="w-64 transition-all duration-300"
    >
      <CardHeader class="flex items-center justify-between h-0">
        <CardTitle class="text-md">Kartfilter</CardTitle>
        <Button
          aria-label="Vis/skjul kartfilter"
          class="h-8 w-8 p-0"
          size="sm"
          variant="ghost"
          @click="toggleExpanded"
        >
          <ChevronUp v-if="isExpanded" class="h-4 w-4" />
          <ChevronDown v-else class="h-4 w-4" />
        </Button>
      </CardHeader>
      <div
        :class="{ 'content-expanded': isExpanded, 'content-collapsed': !isExpanded }"
        class="content-wrapper"
      >
        <CardContent class="flex flex-col gap-4 pt-2">
          <div
            v-for="item in filterItems"
            :key="item.key"
            class="flex items-center justify-between"
          >
            <Label :for="item.key" class="text-sm">{{ item.label }}</Label>
            <Switch
              :id="item.key"
              :model-value="filterOptions[item.key] ?? false"
              @update:model-value="(value: boolean) => handleToggle(item.key, value)"
            />
          </div>
        </CardContent>
      </div>
    </Card>

    <!-- Legend Card -->
    <Card
      :class="{
        'card-expanded': isLegendExpanded,
        'card-collapsed': !isLegendExpanded,
        'gap-0': !isLegendExpanded,
        'gap-6': isLegendExpanded,
      }"
      class="w-64 transition-all duration-300"
    >
      <CardHeader class="flex items-center justify-between h-0">
        <CardTitle class="text-md">Tegnforklaring</CardTitle>
        <Button
          aria-label="Vis/skjul tegnforklaring"
          class="h-8 w-8 p-0"
          size="sm"
          variant="ghost"
          @click="toggleLegendExpanded"
        >
          <ChevronUp v-if="isLegendExpanded" class="h-4 w-4" />
          <ChevronDown v-else class="h-4 w-4" />
        </Button>
      </CardHeader>
      <div
        :class="{ 'content-expanded': isLegendExpanded, 'content-collapsed': !isLegendExpanded }"
        class="content-wrapper"
      >
        <CardContent class="flex flex-col gap-4 pt-2">
          <!-- Location Section -->
          <div>
            <h3 class="text-sm font-semibold mb-2">Lokasjon:</h3>
            <div class="flex flex-col gap-2">
              <div class="flex items-center">
                <div
                  class="w-5 h-5 mr-2.5 bg-blue-500 rounded-full flex items-center justify-center"
                >
                  <User class="h-3 w-3 text-white" />
                </div>
                <span class="text-sm">Min posisjon</span>
              </div>
              <div class="flex items-center">
                <div
                  class="w-5 h-5 mr-2.5 bg-red-500 rounded-full flex items-center justify-center"
                >
                  <Home class="h-3 w-3 text-white" />
                </div>
                <span class="text-sm">Hjem</span>
              </div>
            </div>
          </div>

          <!-- Events Section -->
          <div>
            <h3 class="text-sm font-semibold mb-2">Hendelser:</h3>
            <div class="flex flex-col gap-2">
              <div class="flex items-center">
                <div class="w-5 h-5 mr-2.5 bg-[#4CAF50] rounded-full opacity-60"></div>
                <span class="text-sm">Grønt nivå (Informasjon)</span>
              </div>
              <div class="flex items-center">
                <div class="w-5 h-5 mr-2.5 bg-[#FFC107] rounded-full opacity-60"></div>
                <span class="text-sm">Gult nivå (Advarsel)</span>
              </div>
              <div class="flex items-center">
                <div class="w-5 h-5 mr-2.5 bg-[#F44336] rounded-full opacity-60"></div>
                <span class="text-sm">Rødt nivå (Fare)</span>
              </div>
            </div>
          </div>
        </CardContent>
      </div>
    </Card>

    <!-- Crisis Warning -->
    <div
      v-if="userInCrisisZone"
      class="bg-destructive text-destructive-foreground p-2.5 rounded-md font-bold text-center animate-pulse"
    >
      ⚠️ Advarsel: Du er i en krisesone!
    </div>
  </div>
</template>

<style scoped>
.content-wrapper {
  max-height: 0;
  overflow: hidden;
  transition:
    max-height 0.3s ease-in-out,
    opacity 0.2s ease-in-out;
  opacity: 0;
}

.content-expanded {
  max-height: 400px; /* Adjust based on your content size */
  opacity: 1;
}

.content-collapsed {
  max-height: 0;
  opacity: 0;
}

.card-expanded {
  height: auto;
}

.card-collapsed {
  height: auto;
  min-height: 0;
}

@keyframes pulse {
  0%,
  100% {
    opacity: 1;
  }
  50% {
    opacity: 0.6;
  }
}

.animate-pulse {
  animation: pulse 2s cubic-bezier(0.4, 0, 0.6, 1) infinite;
}
</style>
