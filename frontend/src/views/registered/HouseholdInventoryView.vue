<script setup lang="ts">
import { ref, defineAsyncComponent, computed } from 'vue'
import { useRouter } from 'vue-router'
import {
  Home,
  ChevronDown,
  Utensils,
  Droplet,
  Ambulance,
  Zap,
  Phone,
  Package,
  Plus,
} from 'lucide-vue-next'
import { Button } from '@/components/ui/button'
import HouseholdEmergencySupplies from '@/components/household/HouseholdEmergencySupplies.vue'
import ProductSearch from '@/components/inventory/ProductSearch.vue' // Import the new search component

const AddItemDialog = defineAsyncComponent(() => import('@/components/inventory/ItemDialog.vue'))

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
            {
              id: '1',
              name: 'Hermetikk',
              type: 'hermetikk',
              amount: 20,
              unit: 'stk',
              expiryDate: '2027-12-20',
            },
            {
              id: '2',
              name: 'Tørrvarer',
              type: 'torrvarer',
              amount: 4,
              unit: 'kg',
              expiryDate: '2026-09-29',
            },
          ],
        },
        {
          id: 'water',
          name: 'Vann',
          icon: Droplet,
          current: 80,
          target: 160,
          unit: 'L',
          items: [{ id: '3', name: 'Vann', amount: 20, unit: 'L', expiryDate: null }],
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
          ],
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
          ],
        },
        {
          id: 'comm',
          name: 'Kommunikasjon',
          icon: Phone,
          current: 3,
          target: 3,
          unit: 'stk',
          items: [{ id: '8', name: 'DAB-radio', amount: 1, unit: 'stk' }],
        },
        {
          id: 'misc',
          name: 'Diverse',
          icon: Package,
          current: 2,
          target: 5,
          unit: 'stk',
          items: [{ id: '9', name: 'Fyrstikker', amount: 5, unit: 'eske' }],
        },
      ],
    },
  },
})

// Create a computed property to format data for the HouseholdEmergencySupplies component
const formattedInventory = computed(() => {
  const categories = apiResponse.value.household.inventory.categories

  // Find food, water, and other categories
  const foodCategory = categories.find((cat) => cat.id === 'food') || {
    current: 0,
    target: 0,
    unit: 'kg',
  }
  const waterCategory = categories.find((cat) => cat.id === 'water') || {
    current: 0,
    target: 0,
    unit: 'L',
  }

  // Sum up all other categories for the "other" metric
  const otherCategories = categories.filter((cat) => cat.id !== 'food' && cat.id !== 'water')
  const otherCurrent = otherCategories.reduce((sum, cat) => sum + cat.current, 0)
  const otherTarget = otherCategories.reduce((sum, cat) => sum + cat.target, 0)

  return {
    food: {
      current: foodCategory.current,
      target: foodCategory.target,
      unit: foodCategory.unit,
    },
    water: {
      current: waterCategory.current,
      target: waterCategory.target,
      unit: waterCategory.unit,
    },
    other: {
      current: otherCurrent,
      target: otherTarget,
    },
    preparedDays: apiResponse.value.household.inventory.preparedDays,
    targetDays: apiResponse.value.household.inventory.targetDays,
  }
})

function navigateToHousehold() {
  router.push(`/husstand/${apiResponse.value.household.id}`)
}

function openAddItemDialog(categoryId: string, categoryName: string) {
  selectedCategory.value = { id: categoryId, name: categoryName }
  isAddItemDialogOpen.value = true
}

// eslint-disable-next-line @typescript-eslint/no-explicit-any
function handleAddItem(newItem: any) {
  if (selectedCategory.value) {
    const category = apiResponse.value.household.inventory.categories.find(
      (c) => c.id === selectedCategory.value!.id,
    )

    if (category) {
      category.items.push(newItem)
    }
  }

  isAddItemDialogOpen.value = false
  selectedCategory.value = null
}

// eslint-disable-next-line @typescript-eslint/no-unused-vars
function deleteItem(categoryId: string, itemId: string) {
  // eslint-disable-next-line @typescript-eslint/no-unused-vars
  const category = apiResponse.value.household.inventory.categories.find((c) => c.id === categoryId)
  // if (category) {
  //   category.items = category.items.filter(item => item.id !== itemId)
  // }
}

// State for dialogs
const originalCategoriesState = ref<string[]>([]) // Store the original state
const expandedCategories = ref<string[]>([]) // Start with all categories closed
const isAddItemDialogOpen = ref(false)
const selectedCategory = ref<{ id: string; name: string } | null>(null)
const isSearchActive = ref(false)

// Jump to a search result
function jumpToItem(categoryId: string, itemId: string) {
  // First expand only the category containing the item
  expandedCategories.value = [categoryId]

  // Small delay to ensure DOM is updated
  setTimeout(() => {
    const itemElement = document.getElementById(`item-${itemId}`)
    if (itemElement) {
      // Use scrollIntoView with block: "center" to position the item in the middle of the viewport
      itemElement.scrollIntoView({
        behavior: 'smooth',
        block: 'center' // Position in the middle instead of at the top
      })

      // Add highlight effect
      itemElement.classList.add('bg-blue-100')
      setTimeout(() => {
        itemElement.classList.remove('bg-blue-100')
        itemElement.classList.add('bg-blue-50')
        setTimeout(() => {
          itemElement.classList.remove('bg-blue-50')
        }, 1000)
      }, 1000)
    }
  }, 100)
}

// Handle search state change from the search component
function handleSearchChanged(isActive: boolean, results: any[]) {
  isSearchActive.value = isActive;

  // When search starts, store current state
  if (isActive && !originalCategoriesState.value.length) {
    originalCategoriesState.value = [...expandedCategories.value];
  }

  // When search ends, restore original state
  if (!isActive) {
    expandedCategories.value = [...originalCategoriesState.value];
    originalCategoriesState.value = [];
  }
}
</script>

<template>
  <div class="bg-gray-50 min-h-screen">
    <div class="max-w-8xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      <div class="bg-white rounded-lg shadow-sm border border-gray-200 p-6 mb-6">
        <div class="flex items-center text-sm text-gray-500 mb-2">
          <button @click="navigateToHousehold" class="hover:text-blue-600 flex items-center">
            <Home class="h-4 w-4 mr-1" />
            {{ apiResponse.household.name }}
          </button>
          <span class="mx-2">/</span>
          <span class="text-gray-800">Beredskapslager</span>
        </div>
        <div class="flex items-center justify-between">
          <h1 class="text-3xl font-bold text-gray-900">Beredskapslager</h1>

        </div>

        <!-- New: Add a clarifying subtitle -->
        <p class="text-gray-600 mt-2">
          Dette beredskapslageret er felles for alle medlemmer i husstanden og kan redigeres av alle husstandsmedlemmer.
        </p>
      </div>

      <!-- Main content area -->
      <div class="grid grid-cols-1 lg:grid-cols-12 gap-6">
        <!-- Left column - Overview -->
        <div class="lg:col-span-4 space-y-6">
          <!-- Enhanced summary card with household context -->
          <div class="bg-white rounded-lg shadow-sm border border-gray-200 p-6">
            <div class="flex items-center justify-between mb-4">
              <h2 class="text-xl font-semibold text-gray-800">Oversikt</h2>
            </div>
            <HouseholdEmergencySupplies
              :inventory="formattedInventory"
              :household-id="apiResponse.household.id"
              :show-details-button="false"
            />
          </div>

          <!-- New: Add a card showing household members -->
          <div class="bg-white rounded-lg shadow-sm border border-gray-200 p-6">
            <h2 class="text-xl font-semibold text-gray-800 mb-4">Husstandsmedlemmer</h2>
            <p class="text-gray-600 mb-3">Alle disse personene har tilgang til beredskapslageret:</p>

            <!-- Mock list of household members -->
            <ul class="space-y-2">
              <li class="flex items-center p-2 bg-gray-50 rounded">
                <div class="h-8 w-8 rounded-full bg-blue-100 flex items-center justify-center mr-2">
                  <span class="text-blue-700 font-bold">TS</span>
                </div>
                <span class="font-medium">Truls Sysutvikling</span>
                <span class="ml-2 text-xs bg-blue-100 text-blue-800 px-2 py-0.5 rounded">Du</span>
              </li>
              <li class="flex items-center p-2 bg-gray-50 rounded">
                <div class="h-8 w-8 rounded-full bg-blue-100 flex items-center justify-center mr-2">
                  <span class="text-blue-700 font-bold">MS</span>
                </div>
                <span class="font-medium">Mona Sysutvikling</span>
              </li>
            </ul>
          </div>
        </div>

        <!-- Right column - Products -->
        <div class="lg:col-span-8 space-y-6">
          <!-- Search component in its own card -->
          <ProductSearch
            :categories="apiResponse.household.inventory.categories"
            @jump-to-item="jumpToItem"
            @search-changed="handleSearchChanged"
          />

          <!-- Products card -->
          <div class="bg-white rounded-lg shadow-sm border border-gray-200 p-6">
            <div class="flex items-center justify-between mb-4">
              <h2 class="text-xl font-semibold text-gray-800">Produkter</h2>
            </div>

            <!-- Categories -->
            <div class="space-y-3">
              <div
                v-for="category in apiResponse.household.inventory.categories"
                :key="category.id"
                class="border border-gray-200 rounded-lg overflow-hidden shadow-sm"
              >
                <!-- Category header - all using the same blue style -->
                <div
                  class="flex items-center justify-between p-4 cursor-pointer transition-colors bg-blue-50 hover:bg-blue-100"
                  @click="
                    expandedCategories.includes(category.id)
                      ? (expandedCategories = expandedCategories.filter(
                          (id: any) => id !== category.id,
                        ))
                      : expandedCategories.push(category.id)
                  "
                >
                  <div class="flex items-center">
                    <div
                      class="flex-shrink-0 h-8 w-8 rounded-full flex items-center justify-center mr-3 bg-blue-100 text-blue-700"
                    >
                      <component :is="category.icon" class="h-5 w-5" />
                    </div>
                    <span class="text-lg font-medium text-blue-800">{{ category.name }}</span>
                  </div>
                  <div class="flex items-center">
                    <!-- Progress indicator -->
                    <div class="w-20 bg-gray-200 rounded-full h-2.5 mr-4 overflow-hidden">
                      <div
                        class="h-2.5 rounded-full bg-blue-500"
                        :style="`width: ${Math.min(100, (category.current / category.target) * 100)}%`"
                      ></div>
                    </div>
                    <span class="mr-4 text-sm font-medium">
                      {{ category.current }} / {{ category.target }} {{ category.unit }}
                    </span>
                    <ChevronDown
                      class="h-5 w-5 text-gray-500 transition-transform duration-200"
                      :class="{ 'transform rotate-180': expandedCategories.includes(category.id) }"
                    />
                  </div>
                </div>

                <!-- Category items -->
                <div v-if="expandedCategories.includes(category.id)">
                  <div class="divide-y divide-gray-100">
                    <!-- Individual items -->
                    <div
                      v-for="item in category.items"
                      :key="item.id"
                      :id="`item-${item.id}`"
                      class="flex items-center justify-between py-3 px-4 hover:bg-gray-50 transition-colors duration-200"
                    >
                      <div class="flex items-center">
                        <div class="flex-shrink-0 w-2 h-10 rounded mr-4 bg-blue-300"></div>
                        <span class="text-gray-800">{{ item.name }}</span>
                      </div>

                      <div class="flex items-center space-x-6">
                        <div class="text-right">
                          <span
                            class="font-medium px-2 py-1 rounded-full text-sm bg-blue-100 text-blue-800"
                          >
                            {{ item.amount }} {{ item.unit }}
                          </span>
                        </div>

                        <div class="flex items-center">
                          <span class="text-gray-600 mr-2 text-sm">Utløpsdato:</span>
                          <span
                            :class="[
                              !item.expiryDate
                                ? 'text-gray-500'
                                : new Date(item.expiryDate) < new Date()
                                  ? 'text-red-600 font-medium bg-red-50 px-2 py-0.5 rounded'
                                  : new Date(item.expiryDate) <
                                      new Date(Date.now() + 30 * 24 * 60 * 60 * 1000)
                                    ? 'text-amber-600 font-medium bg-amber-50 px-2 py-0.5 rounded'
                                    : 'text-blue-600 bg-blue-50 px-2 py-0.5 rounded',
                            ]"
                            class="text-sm"
                          >
                            {{
                              item.expiryDate
                                ? new Date(item.expiryDate).toLocaleDateString('no-NO', {
                                  day: '2-digit',
                                  month: '2-digit',
                                  year: 'numeric',
                                })
                                : 'Ingen dato'
                            }}
                          </span>
                        </div>

                        <button
                          class="text-red-500 hover:text-red-700 p-1 rounded-full hover:bg-red-50"
                          @click="deleteItem(category.id, item.id)"
                        >
                          <svg
                            xmlns="http://www.w3.org/2000/svg"
                            width="18"
                            height="18"
                            viewBox="0 0 24 24"
                            fill="none"
                            stroke="currentColor"
                            stroke-width="2"
                            stroke-linecap="round"
                            stroke-linejoin="round"
                          >
                            <path d="M3 6h18"></path>
                            <path d="M19 6v14c0 1-1 2-2 2H7c-1 0-2-1-2-2V6"></path>
                            <path d="M8 6V4c0-1 1-2 2-2h4c1 0 2 1 2 2v2"></path>
                          </svg>
                        </button>
                      </div>
                    </div>

                    <!-- Add item button -->
                    <div class="p-3 bg-blue-50">
                      <button
                        @click="openAddItemDialog(category.id, category.name)"
                        class="py-2 px-4 rounded-md flex items-center text-sm font-medium w-full justify-center bg-blue-600 hover:bg-blue-700 text-white"
                      >
                        <Plus class="mr-2 h-4 w-4" /> Legg til vare
                      </button>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
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
  </div>
</template>
