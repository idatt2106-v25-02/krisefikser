<script setup lang="ts">
import { ref } from 'vue'
import { type FilterOptions } from '@/components/map/useMap'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Label } from '@/components/ui/label'
import { Switch } from '@/components/ui/switch'
import { Button } from '@/components/ui/button'
import { ChevronDown, ChevronUp } from 'lucide-vue-next'

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

const toggleExpanded = () => {
  isExpanded.value = !isExpanded.value
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

console.log(filterOptions.value)

const props = defineProps<{
  filterMarkers: (options: FilterOptions) => void
}>()

const handleToggle = (option: keyof FilterOptions, value: boolean) => {
  console.log(filterOptions.value)
  filterOptions.value[option] = value
  props.filterMarkers(filterOptions.value)
}
</script>

<template>
  <Card
    class="absolute top-5 right-5 z-10 w-64 transition-all duration-300"
    :class="{ 'card-expanded': isExpanded, 'card-collapsed': !isExpanded }"
  >
    <CardHeader class="pb-2">
      <div class="flex items-center justify-between">
        <CardTitle class="text-lg">Kartfilter</CardTitle>
        <Button
          variant="ghost"
          size="sm"
          class="h-8 w-8 p-0"
          @click="toggleExpanded"
          aria-label="Vis/skjul kartfilter"
        >
          <ChevronUp v-if="isExpanded" class="h-4 w-4" />
          <ChevronDown v-else class="h-4 w-4" />
        </Button>
      </div>
    </CardHeader>
    <div
      class="content-wrapper"
      :class="{ 'content-expanded': isExpanded, 'content-collapsed': !isExpanded }"
    >
      <CardContent class="flex flex-col gap-4">
        <div v-for="item in filterItems" :key="item.key" class="flex items-center justify-between">
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
</style>
