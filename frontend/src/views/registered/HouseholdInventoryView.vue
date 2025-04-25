<script setup lang="ts">
import { ref, defineAsyncComponent } from 'vue'
import { useRouter } from 'vue-router'
import { Home, ChevronDown, Utensils, Droplet, Ambulance, Zap, Phone, Package, Plus } from 'lucide-vue-next'
import { Button } from '@/components/ui/button'

const AddItemDialog = defineAsyncComponent(() => import('@/components/inventory/ItemDialog.vue'))

interface ProductType {
  id: string
  name: string
  icon: any
}

interface InventoryCategory {
  id: string
  name: string
  icon: any
  items: InventoryItem[]
  current: number
  target: number
  unit: string
}

interface InventoryItem {
  id: string
  name: string
  amount: number
  unit: string
  expiryDate?: string
  type?: string
}

const router = useRouter()

// Mock API response for inventory
const apiResponse = ref({
  household: {
    id: '1',
    name: 'Familien Sysutvikling',
    inventory: {
      preparedDays: 5,
      targetDays: 7,
      categories: [
        {
          id: 'food',
          name: 'Mat',
          icon: Utensils,
          current: 21,
          target: 60,
          unit: 'kg',
          items: [
            { id: '1', name: 'Hermetikk', type: 'hermetikk', amount: 20, unit: 'stk', expiryDate: '2027-12-20' },
            { id: '2', name: 'Tørrvarer', type: 'torrvarer', amount: 4, unit: 'kg', expiryDate: '2026-09-29' },
          ]
        },
        {
          id: 'water',
          name: 'Vann',
          icon: Droplet,
          current: 80,
          target: 160,
          unit: 'L',
          items: [
            { id: '3', name: 'Vann', amount: 20, unit: 'L', expiryDate: null },
          ]
        },
        {
          id: 'health',
          name: 'Helse & hygiene',
          icon: Ambulance,
          current: 7,
          target: 9,
          unit: 'stk',
          items: [
            { id: '4', name: 'Førstehjelpssett', amount: 1, unit: 'stk' },
            { id: '5', name: 'Våtservietter', amount: 3, unit: 'pakke', expiryDate: '2026-05-20' },
          ]
        },
        {
          id: 'power',
          name: 'Lys og strøm',
          icon: Zap,
          current: 4,
          target: 5,
          unit: 'stk',
          items: [
            { id: '6', name: 'Batterier', amount: 24, unit: 'stk' },
            { id: '7', name: 'Stearinlys', amount: 20, unit: 'stk' },
          ]
        },
        {
          id: 'comm',
          name: 'Kommunikasjon',
          icon: Phone,
          current: 3,
          target: 3,
          unit: 'stk',
          items: [
            { id: '8', name: 'DAB-radio', amount: 1, unit: 'stk' },
          ]
        },
        {
          id: 'misc',
          name: 'Diverse',
          icon: Package,
          current: 2,
          target: 5,
          unit: 'stk',
          items: [
            { id: '9', name: 'Fyrstikker', amount: 5, unit: 'eske' },
          ]
        }
      ]
    }
  }
})

// Helper functions
function calculatePercentage(current: number, target: number): number {
  return Math.min(100, Math.round((current / target) * 100))
}

function formatDate(dateString?: string): string {
  if (!dateString) return 'Ingen utløpsdato'
  return new Date(dateString).toLocaleDateString('no-NO', {day: '2-digit', month: '2-digit', year: 'numeric'})
}

function navigateToHousehold() {
  router.push(`/husstand/${apiResponse.value.household.id}`)
}

function openAddItemDialog(categoryId: string, categoryName: string) {
  selectedCategory.value = { id: categoryId, name: categoryName }
  isAddItemDialogOpen.value = true
}

function handleAddItem(newItem: any) {
  if (selectedCategory.value) {
    const category = apiResponse.value.household.inventory.categories.find(
      c => c.id === selectedCategory.value!.id
    )

    if (category) {
      category.items.push(newItem)
    }
  }

  isAddItemDialogOpen.value = false
  selectedCategory.value = null
}

function deleteItem(categoryId: string, itemId: string) {
  const category = apiResponse.value.household.inventory.categories.find(c => c.id === categoryId)
  if (category) {
    category.items = category.items.filter(item => item.id !== itemId)
  }
}

// State for dialogs
const expandedCategories = ref<string[]>(['food']) // Initially expand food category
const isAddItemDialogOpen = ref(false)
const selectedCategory = ref<{id: string, name: string} | null>(null)
</script>

<template>
  <div class="max-w-8xl mx-auto px-20 py-8">
    <!-- Header with breadcrumb -->
    <div class="mb-8">
      <div class="flex items-center text-sm text-gray-500 mb-2">
        <button @click="navigateToHousehold" class="hover:text-blue-600 flex items-center">
          <Home class="h-4 w-4 mr-1" />
          {{ apiResponse.household.name }}
        </button>
        <span class="mx-2">/</span>
        <span class="text-gray-800">Beredskapslager</span>
      </div>
      <h1 class="text-3xl font-bold text-gray-900 mb-6">Beredskapslager</h1>
    </div>

    <!-- Summary section -->
    <div class="bg-white border border-gray-200 rounded-lg p-6 mb-8">
      <div class="grid grid-cols-3 gap-4 mb-8">
        <div v-for="category in apiResponse.household.inventory.categories.slice(0, 3)" :key="category.id">
          <div class="text-sm text-gray-500 mb-1">{{ category.name }}</div>
          <div class="text-lg text-blue-600 font-semibold">
            {{ category.current }}/{{ category.target }} {{ category.unit }}
          </div>
        </div>
      </div>

      <!-- Days prepared -->
      <div>
        <div class="flex justify-between mb-1">
          <span class="text-sm text-gray-600">Dager forberedt</span>
          <span class="text-sm font-medium">{{ apiResponse.household.inventory.preparedDays }}</span>
        </div>
        <div class="h-2 bg-gray-200 rounded-full overflow-hidden">
          <div
            class="h-full bg-blue-500 rounded-full"
            :style="`width: ${(apiResponse.household.inventory.preparedDays / apiResponse.household.inventory.targetDays) * 100}%`"
          ></div>
        </div>
        <div class="mt-1 text-xs text-gray-500">
          Norske myndigheter anbefaler at du har nok forsyninger tilregnet {{ apiResponse.household.inventory.targetDays }} dager.
        </div>
      </div>
    </div>

    <!-- Products section - Figma style -->
    <div class="space-y-4">
      <h2 class="text-3xl font-bold text-gray-900 mb-4">Produkter</h2>

      <!-- Mat category -->
      <div v-for="category in apiResponse.household.inventory.categories" :key="category.id" class="border-b border-gray-200">
        <!-- Category header -->
        <div class="flex items-center justify-between py-4 cursor-pointer" @click="expandedCategories.includes(category.id) ? expandedCategories = expandedCategories.filter(id => id !== category.id) : expandedCategories.push(category.id)">
          <div class="flex items-center">
            <component :is="category.icon" class="h-6 w-6 mr-3 text-gray-800" />
            <span class="text-2xl text-gray-800">{{ category.name }}</span>
          </div>
          <ChevronDown
            class="h-6 w-6 text-gray-500 transition-transform duration-200"
            :class="{ 'transform rotate-180': expandedCategories.includes(category.id) }"
          />
        </div>

        <!-- Category items -->
        <div v-if="expandedCategories.includes(category.id)" class="pb-4">
          <!-- Individual items -->
          <div
            v-for="item in category.items"
            :key="item.id"
            class="border-t border-gray-100 flex items-center justify-between py-4 px-4"
          >
            <div class="flex items-center">
              <div class="flex-shrink-0 w-6 mr-3">
                <!-- Here you could add custom icons for different product types if needed -->
              </div>
              <span class="text-gray-800">{{ item.name }}</span>
            </div>

            <div class="flex items-center space-x-6">
              <div class="text-right w-20">
                <span class="font-medium text-gray-800">{{ item.amount }} {{ item.unit }}</span>
              </div>

              <div class="flex w-48 items-center">
                <span class="text-gray-600 mr-2">Dato:</span>
                <span
                  :class="[
                    item.expiryDate && new Date(item.expiryDate) < new Date(Date.now() + 30*24*60*60*1000)
                      ? 'text-amber-600 font-medium'
                      : 'text-gray-800'
                  ]"
                >
                  {{ item.expiryDate ? new Date(item.expiryDate).toLocaleDateString('no-NO', {day: '2-digit', month: '2-digit', year: 'numeric'}) : 'Ingen dato' }}
                </span>
              </div>

              <button
                class="text-red-500 hover:text-red-700"
                @click="deleteItem(category.id, item.id)"
              >
                <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                  <path d="M3 6h18"></path>
                  <path d="M19 6v14c0 1-1 2-2 2H7c-1 0-2-1-2-2V6"></path>
                  <path d="M8 6V4c0-1 1-2 2-2h4c1 0 2 1 2 2v2"></path>
                </svg>
              </button>
            </div>
          </div>

          <!-- Add item button -->
          <div class="mt-4 px-4">
            <button
              @click="openAddItemDialog(category.id, category.name)"
              class="bg-blue-600 hover:bg-blue-700 text-white py-2 px-6 rounded-md flex items-center text-sm font-medium"
            >
              <Plus class="mr-2 h-4 w-4" /> Legg til vare
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- Floating action button -->
    <div class="fixed bottom-6 right-6">
      <Button class="h-14 w-14 rounded-full shadow-lg">
        <span class="text-2xl">+</span>
      </Button>
    </div>

    <!-- Add Item Dialog -->
    <AddItemDialog
      :is-open="isAddItemDialogOpen"
      :category-id="selectedCategory?.id"
      :category-name="selectedCategory?.name"
      @close="isAddItemDialogOpen = false"
      @add-item="handleAddItem"
    />
  </div>
</template>
