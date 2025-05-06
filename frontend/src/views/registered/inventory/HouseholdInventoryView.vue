<script setup lang="ts">
import { ref, computed } from 'vue'
import type { FunctionalComponent } from 'vue';
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth/useAuthStore' // For enabling queries
import { useQueryClient } from '@tanstack/vue-query' // For potential invalidation later

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
import HouseholdEmergencySupplies from '@/components/household/HouseholdEmergencySupplies.vue'
import ProductSearch from '@/components/inventory/ProductSearch.vue'
import WaterItemDialog from '@/components/inventory/dialog/WaterItemDialog.vue'
import FoodItemDialog from '@/components/inventory/dialog/FoodItemDialog.vue'
import ChecklistItemDialog from '@/components/inventory/dialog/ChecklistItemDialog.vue'
import MiscItemDialog from '@/components/inventory/dialog/MiscItemDialog.vue'

// Import API hooks and types
import {
  useGetInventorySummary,
  useGetAllFoodItems,
  useGetAllChecklistItems,
  getGetInventorySummaryQueryKey, // For invalidation if needed
  getGetAllFoodItemsQueryKey,     // For invalidation if needed
  getGetAllChecklistItemsQueryKey // For invalidation if needed
} from '@/api/generated/item/item'
import type {
  InventorySummaryResponse,
  FoodItemResponse,
  ChecklistItemResponse
} from '@/api/generated/model' // Assuming DTOs are re-exported from model/index.ts
import { useGetActiveHousehold } from '@/api/generated/household/household'
import type { HouseholdResponse, HouseholdMemberResponse, GuestResponse } from '@/api/generated/model'

// Define types for data structures (will need adjustments based on backend DTOs)
interface InventoryItem {
  id: string; // Comes from FoodItemResponse.id or ChecklistItemResponse.id
  name: string; // Comes from FoodItemResponse.name or ChecklistItemResponse.name
  amount: number; // For food, this might be derived or managed differently. For checklist, not applicable.
  unit: string; // For food, this might be derived or managed differently. For checklist, not applicable.
  type?: string; // For food, can use FoodItemResponse.icon. For checklist, ChecklistItemResponse.icon
  expiryDate?: string | null; // From FoodItemResponse.expirationDate (Instant -> string)
  checked?: boolean; // From ChecklistItemResponse.checked
  kcal?: number; // From FoodItemResponse.kcal
  iconName?: string; // From FoodItemResponse.icon or ChecklistItemResponse.icon (to map to Lucide icon if needed)
}

interface Category {
  id: string; // e.g., 'food', 'water', 'health', 'power', 'comm', 'misc'
  name: string; // e.g., 'Mat', 'Vann', 'Helse & hygiene'
  icon: FunctionalComponent; // Lucide icon component
  current: number; // For food (kcal), water (liters), checklist (checked count)
  target: number; // For food (kcalGoal), water (litersGoal), checklist (total count)
  unit: string; // e.g., 'kcal', 'L', 'stk' (for checklist count)
  items: InventoryItem[];
}

// This will be largely replaced or heavily adapted
// interface Inventory {
//   preparedDays: number;
//   targetDays: number;
//   categories: Category[];
// }

// This might still be useful for the top-level household ID/name if not passed as prop
// interface Household {
//   id: string;
//   name: string;
//   // inventory: Inventory; // This will be reconstructed
// }

interface FormattedCategory {
  current: number;
  target: number;
  unit: string;
}

interface FormattedInventory { // For HouseholdEmergencySupplies component
  food: FormattedCategory;
  water: FormattedCategory;
  other: { // Represents checklist items
    current: number; // checkedItems
    target: number; // totalItems
  };
  preparedDays: number; // This is not directly in summary, might need separate logic or be removed
  targetDays: number; // This is not directly in summary, might need separate logic or be removed
}

const router = useRouter()
const authStore = useAuthStore()
const queryClient = useQueryClient() // For later mutations

// --- Fetching Actual Data ---
const householdId = ref('1'); // Placeholder: This needs to come from route, store, or props

// 1. Fetch Inventory Summary
const {
  data: inventorySummary,
  isLoading: isLoadingSummary,
  isError: isErrorSummary,
} = useGetInventorySummary(
  { // Orval options object for useQuery
    query: {
      enabled: computed(() => authStore.isAuthenticated && !!householdId.value), // Fetch only if authenticated and householdId is available
      // staleTime: 5 * 60 * 1000, // 5 minutes, example
    }
  }
);

// 2. Fetch Food Items
const {
  data: foodItems, // This will be FoodItemResponse[] | undefined
  isLoading: isLoadingFoodItems,
  isError: isErrorFoodItems,
} = useGetAllFoodItems(
  {
    query: {
      enabled: computed(() => authStore.isAuthenticated && !!householdId.value),
    }
  }
);

// 3. Fetch Checklist Items
const {
  data: checklistItems, // This will be ChecklistItemResponse[] | undefined
  isLoading: isLoadingChecklistItems,
  isError: isErrorChecklistItems,
} = useGetAllChecklistItems(
  {
    query: {
      enabled: computed(() => authStore.isAuthenticated && !!householdId.value),
    }
  }
);

// 4. Fetch Household Members and Guests
const { data: household, isLoading: isLoadingHousehold } = useGetActiveHousehold({
  query: {
    enabled: authStore.isAuthenticated,
    refetchOnMount: true,
    refetchOnWindowFocus: true,
  },
})

// --- End Fetching Actual Data ---


// Create a computed property to format data for the HouseholdEmergencySupplies component
const formattedInventory = computed<FormattedInventory>(() => {
  if (!inventorySummary.value) {
    return {
      food: { current: 0, target: 0, unit: 'kcal' },
      water: { current: 0, target: 0, unit: 'L' },
      other: { current: 0, target: 0 },
      preparedDays: 0, // Mocked for now
      targetDays: 7,   // Mocked for now
    };
  }

  return {
    food: {
      current: inventorySummary.value.kcal || 0,
      target: inventorySummary.value.kcalGoal || 0, // Assuming kcalGoal exists
      unit: 'kcal',
    },
    water: {
      current: inventorySummary.value.waterLiters || 0,
      target: inventorySummary.value.waterLitersGoal || 0, // Assuming waterLitersGoal exists
      unit: 'L',
    },
    other: { // Represents checklist items summary
      current: inventorySummary.value.checkedItems || 0,
      target: inventorySummary.value.totalItems || 0,
    },
    preparedDays: 0, // Not in InventorySummaryResponse, needs to be calculated or removed
    targetDays: 7,   // Not in InventorySummaryResponse, needs to be set differently or removed
  };
});

// TODO: This computed property will need significant rework to build categories from fetched data
const displayedCategories = computed<Category[]>(() => {
  const categories: Category[] = [];

  // Food Category
  if (foodItems.value && inventorySummary.value) {
    categories.push({
      id: 'food',
      name: 'Mat',
      icon: Utensils,
      current: inventorySummary.value.kcal || 0,
      target: inventorySummary.value.kcalGoal || 0,
      unit: 'kcal',
      items: foodItems.value.map(fi => ({
        id: fi.id,
        name: fi.name,
        kcal: fi.kcal,
        expiryDate: fi.expirationDate ? new Date(fi.expirationDate).toISOString().split('T')[0] : null,
        iconName: fi.icon,
        // amount & unit for food items are not directly available from FoodItemResponse
        // We might display kcal or a count. For now, leave them out or use placeholder.
        amount: fi.kcal || 0, // Or 1 if each item is a single unit
        unit: 'kcal', // Or 'stk'
      })),
    });
  } else {
     categories.push({ id: 'food', name: 'Mat', icon: Utensils, current: 0, target: 0, unit: 'kcal', items: [] });
  }


  // Water Category (Data primarily from summary)
  if (inventorySummary.value) {
      categories.push({
      id: 'water',
      name: 'Vann',
      icon: Droplet,
      current: inventorySummary.value.waterLiters || 0,
      target: inventorySummary.value.waterLitersGoal || 0,
      unit: 'L',
      items: [
        // Backend doesn't provide individual water items, only total.
        // We can create a conceptual item or leave this empty and rely on summary.
        // { id: 'water-total', name: 'Totalt Vann', amount: inventorySummary.value.waterLiters || 0, unit: 'L'}
      ],
    });
  } else {
    categories.push({ id: 'water', name: 'Vann', icon: Droplet, current: 0, target: 0, unit: 'L', items: [] });
  }

  // Checklist Item Categories (Health, Power, Comm, Misc)
  // This requires a strategy to map checklistItems to these categories.
  // For now, let's create placeholder categories.
  const checklistCategoryMapping: Record<string, { name: string; icon: FunctionalComponent }> = {
    health: { name: 'Helse & hygiene', icon: Ambulance },
    power: { name: 'Lys og strøm', icon: Zap },
    comm: { name: 'Kommunikasjon', icon: Phone },
    misc: { name: 'Diverse', icon: Package },
  };

  if (checklistItems.value) {
    // Example: Group by a hypothetical 'type' or 'icon' property if available on ChecklistItemResponse,
    // or implement a more sophisticated mapping.
    // This is a simplified placeholder.
    const tempChecklistItems: InventoryItem[] = checklistItems.value.map(ci => ({
      id: ci.id,
      name: ci.name,
      checked: ci.checked,
      iconName: ci.icon,
      amount: ci.checked ? 1 : 0, // Represent as 1 if checked, for conceptual count
      unit: 'stk',
    }));

    // For now, lump all checklist items into 'misc' or distribute based on a strategy (e.g. item.icon)
     categories.push({
      id: 'misc',
      name: 'Diverse Sjekkliste',
      icon: Package,
      current: inventorySummary.value?.checkedItems || 0,
      target: inventorySummary.value?.totalItems || 0,
      unit: 'stk',
      items: tempChecklistItems,
    });
    // TODO: Implement proper categorization for Health, Power, Comm based on checklistItems data
  } else {
    Object.entries(checklistCategoryMapping).forEach(([key, val]) => {
       categories.push({ id: key, name: val.name, icon: val.icon, current: 0, target: 0, unit: 'stk', items: [] });
    });
  }


  return categories;
});

const membersAndGuests = computed(() => {
  if (!household.value) return []
  return [
    ...((household.value.members || []).map(m => ({ type: 'member', data: m }))),
    ...((household.value.guests || []).map(g => ({ type: 'guest', data: g })))
  ]
})

// --- Mock data removal and old computed properties ---
// const apiResponse = ref<ApiResponse>({ ... }); // REMOVE THIS MOCK
// The old formattedInventory computed that used apiResponse.value is replaced by the one above.
// --- End mock data removal ---

function navigateToHousehold(): void {
  // TODO: Ensure householdId.value is the actual ID for navigation
  router.push(`/husstand/${householdId.value}`)
}

function openAddItemDialog(categoryId: string, categoryName: string): void {
  selectedCategory.value = { id: categoryId, name: categoryName }
  isAddItemDialogOpen.value = true
}

function getDialogComponent(
  categoryId: string
): typeof WaterItemDialog | typeof FoodItemDialog | typeof ChecklistItemDialog | typeof MiscItemDialog | null {
  // This logic might need adjustment based on how categories are now defined from backend data
  switch (categoryId) {
    case 'water':
      return WaterItemDialog; // Water might not have "items" to add in the same way now
    case 'food':
      return FoodItemDialog;
    // For checklist items, we'll need a generic dialog or specific ones if types are distinguishable
    case 'health':
    case 'power':
    case 'comm':
    case 'misc': // Assuming 'misc' now covers all checklist items, or specific logic needed
      return ChecklistItemDialog; // Or MiscItemDialog if more appropriate
    default:
      // If we have dynamically created categories from checklist items, this needs to be smarter
      // For now, if it's a checklist-derived category, use ChecklistItemDialog
      if (displayedCategories.value.find(c => c.id === categoryId && c.unit === 'stk')) {
        return ChecklistItemDialog;
      }
      return null;
  }
}

// TODO: Update handleAddItem, deleteItem, startEdit, saveEdit to use mutation hooks
function handleAddItem(newItem: InventoryItem): void {
  // This needs to call a backend mutation, e.g., useCreateFoodItem or a checklist item creation hook
  console.log('Adding item (TODO: backend integration):', newItem, 'to category:', selectedCategory.value);

  // Optimistic update example (manual cache update or refetch on success)
  // if (selectedCategory.value) {
  //   const category = displayedCategories.value.find(c => c.id === selectedCategory.value!.id);
  //   if (category) {
  //     category.items.push(newItem); // This won't persist without backend
  //   }
  // }

  isAddItemDialogOpen.value = false
  selectedCategory.value = null
}

function deleteItem(categoryId: string, itemId: string): void {
  // This needs to call useDeleteFoodItem or a checklist item deletion hook
  console.log(`Deleting item with ID ${itemId} from category ${categoryId} (TODO: backend integration)`);
}

const originalCategoriesState = ref<string[]>([])
const expandedCategories = ref<string[]>([])
const isAddItemDialogOpen = ref(false)
const selectedCategory = ref<{ id: string; name: string } | null>(null)
const isSearchActive = ref(false)

function jumpToItem(categoryId: string, itemId: string): void {
  expandedCategories.value = [categoryId]
  setTimeout(() => {
    const itemElement = document.getElementById(`item-${itemId}`)
    if (itemElement) {
      itemElement.scrollIntoView({ behavior: 'smooth', block: 'center' })
      itemElement.classList.add('bg-blue-100')
      setTimeout(() => {
        itemElement.classList.remove('bg-blue-100')
        itemElement.classList.add('bg-blue-50')
        setTimeout(() => { itemElement.classList.remove('bg-blue-50') }, 1000)
      }, 1000)
    }
  }, 100)
}

function handleSearchChanged(isActive: boolean): void {
  isSearchActive.value = isActive;
  if (isActive && !originalCategoriesState.value.length) {
    originalCategoriesState.value = [...expandedCategories.value];
  }
  if (!isActive) {
    expandedCategories.value = [...originalCategoriesState.value];
    originalCategoriesState.value = [];
  }
}

const editingItemId = ref<string | null>(null)
const editingName = ref('')
const editingAmount = ref<number | null>(null) // For food, this might be kcal or a new concept

function startEdit(item: InventoryItem): void {
  editingItemId.value = item.id
  editingName.value = item.name
  // For food, if we edit kcal directly:
  editingAmount.value = item.kcal ?? (item.amount || 0) ; // Adapt based on what's being edited
}

function cancelEdit(): void {
  editingItemId.value = null
  editingName.value = ''
  editingAmount.value = null
}

function saveEdit(category: Category, item: InventoryItem): void {
  // This needs to call useUpdateFoodItem or a checklist item update hook
  console.log('Saving edit (TODO: backend integration):', item, 'in category:', category);
  // const idx = category.items.findIndex((i) => i.id === item.id)
  // if (idx !== -1) {
    // category.items[idx].name = editingName.value
    // category.items[idx].amount = editingAmount.value as number // Adapt for kcal etc.
  // }
  cancelEdit()
}
</script>

<template>
  <div class="bg-gray-50 min-h-screen">
    <div class="max-w-8xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      <div class="bg-white rounded-lg shadow-sm border border-gray-200 p-6 mb-6">
        <div class="flex items-center text-base text-gray-500 mb-3">
          <button @click="navigateToHousehold" class="hover:text-blue-600 flex items-center">
            <Home class="h-5 w-5 mr-2" />
            {{ household?.name }}
          </button>
          <span class="mx-2">/</span>
          <span class="text-gray-800">Beredskapslager</span>
        </div>
        <div class="flex items-center justify-between">
          <h1 class="text-4xl font-bold text-gray-900">Beredskapslager</h1>
        </div>
        <p class="text-lg text-gray-600 mt-4">
          Her kan du se oversikt over beredskapslageret ditt, inkludert matvarer, utstyr og andre viktige ressurser.
        </p>
      </div>

      <!-- Main content area -->
      <div class="grid grid-cols-1 lg:grid-cols-12 gap-6">
        <!-- Left column - Overview -->
        <div class="lg:col-span-4 space-y-6">
          <div class="bg-white rounded-lg shadow-sm border border-gray-200 p-6">
            <div class="flex items-center justify-between mb-4">
              <h2 class="text-2xl font-semibold text-gray-800">Oversikt</h2>
            </div>
            <HouseholdEmergencySupplies
              :inventory="formattedInventory"
              :household-id="householdId"
              :show-details-button="false"
            />
          </div>

          <!-- New: Add a card showing household members -->
          <div class="bg-white rounded-lg shadow-sm border border-gray-200 p-6">
            <h2 class="text-2xl font-semibold text-gray-800 mb-4">Husstandsmedlemmer</h2>
            <p class="text-lg text-gray-600 mb-4">Alle disse personene har tilgang til beredskapslageret:</p>
            <ul class="space-y-4 max-h-40 overflow-y-auto pr-2">
              <li v-for="person in membersAndGuests" :key="person.type === 'member' ? (person.data as HouseholdMemberResponse).user?.id : (person.data as GuestResponse).id" class="flex items-center p-4 bg-gray-50 rounded">
                <div class="h-12 w-12 rounded-full flex items-center justify-center mr-4"
                  :class="person.type === 'member' ? 'bg-blue-100 text-blue-700' : 'bg-green-100 text-green-700'">
                  <span class="font-bold">
                    {{ person.type === 'member' ? ((person.data as HouseholdMemberResponse).user?.firstName?.[0] ?? '?') : ((person.data as GuestResponse).name?.[0]?.toUpperCase() ?? 'G') }}
                  </span>
                </div>
                <span class="text-lg font-medium">
                  {{ person.type === 'member' ? ((person.data as HouseholdMemberResponse).user?.firstName + ' ' + (person.data as HouseholdMemberResponse).user?.lastName) : (person.data as GuestResponse).name }}
                </span>
                <span v-if="person.type === 'member' && (person.data as HouseholdMemberResponse).user?.id === authStore.currentUser?.id" class="ml-3 text-base bg-blue-100 text-blue-800 px-4 py-1.5 rounded">Du</span>
                <span v-else-if="person.type === 'guest'" class="ml-3 text-base bg-green-100 text-green-800 px-4 py-1.5 rounded">Gjest</span>
              </li>
            </ul>
          </div>
        </div>

        <!-- Right column - Products -->
        <div class="lg:col-span-8 space-y-6">
          <!-- Search component in its own card -->
          <ProductSearch
            :categories="displayedCategories"
            @jump-to-item="jumpToItem"
            @search-changed="handleSearchChanged"
          />

          <!-- Products card -->
          <div class="bg-white rounded-lg shadow-sm border border-gray-200 p-6">
            <div class="flex items-center justify-between mb-4">
              <h2 class="text-2xl font-semibold text-gray-800">Produkter</h2>
            </div>

            <!-- Categories -->
            <div class="space-y-3">
              <div
                v-for="category in displayedCategories"
                :key="category.id"
                class="border border-gray-200 rounded-lg overflow-hidden shadow-sm"
              >
                <!-- Category header - all using the same blue style -->
                <div
                  class="flex items-center justify-between p-4 cursor-pointer transition-colors bg-blue-50 hover:bg-blue-100"
                  @click="
                    expandedCategories.includes(category.id)
                      ? (expandedCategories = expandedCategories.filter(
                          (id) => id !== category.id,
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
                    <span class="text-2xl font-medium text-blue-800">{{ category.name }}</span>
                  </div>
                  <div class="flex items-center">
                    <!-- Progress indicator -->
                    <div class="w-20 bg-gray-200 rounded-full h-2.5 mr-4 overflow-hidden">
                      <div
                        class="h-2.5 rounded-full bg-blue-500"
                        :style="`width: ${Math.min(100, (category.current / category.target) * 100)}%`"
                      ></div>
                    </div>
                    <span class="mr-4 text-lg font-medium">
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
                        <template v-if="category.id === 'misc' && editingItemId === item.id && item.amount !== undefined && !item.expiryDate">
                          <input v-model="editingName" class="border rounded px-2 py-1 text-sm mr-2 w-32" />
                          <input v-model.number="editingAmount" type="number" min="0.1" step="0.1" class="border rounded px-2 py-1 text-sm w-16" />
                        </template>
                        <template v-else>
                          <span
                            class="text-xl text-gray-800"
                            :class="{ 'cursor-pointer underline decoration-dotted': category.id === 'misc' && item.amount !== undefined && !item.expiryDate }"
                            @click="category.id === 'misc' && item.amount !== undefined && !item.expiryDate ? startEdit(item) : null"
                          >
                            {{ item.name }}
                          </span>
                        </template>
                      </div>
                      <div class="flex items-center space-x-6">
                        <div class="text-right">
                          <template v-if="category.id === 'misc' && editingItemId === item.id && true && !item.expiryDate">
                            <span class="font-medium px-2 py-1 rounded-full text-sm bg-blue-100 text-blue-800">{{ editingAmount }}</span>
                          </template>
                          <template v-else>
                            <span class="font-medium px-2 py-1 rounded-full text-sm bg-blue-100 text-blue-800">{{ item.amount }} {{ item.unit }}</span>
                          </template>
                        </div>
                        <template v-if="category.id === 'misc' && editingItemId === item.id && true && !item.expiryDate">
                          <button @click="saveEdit(category, item)" class="text-green-600 hover:text-green-800 mr-2">✓</button>
                          <button @click="cancelEdit" class="text-gray-500 hover:text-red-600">✗</button>
                        </template>
                        <template v-if="item.expiryDate">
                          <div class="flex items-center">
                            <span class="text-gray-600 mr-3 text-lg">Utløpsdato:</span>
                            <span
                              :class="[
                                !item.expiryDate
                                  ? 'text-gray-500'
                                  : new Date(item.expiryDate) < new Date()
                                    ? 'text-red-600 font-medium bg-red-50 px-2 py-0.5 rounded'
                                    : new Date(item.expiryDate) < new Date(Date.now() + 30 * 24 * 60 * 60 * 1000)
                                      ? 'text-amber-600 font-medium bg-amber-50 px-2 py-0.5 rounded'
                                      : 'text-blue-600 bg-blue-50 px-2 py-0.5 rounded',
                              ]"
                              class="text-lg"
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
                        </template>
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
                        class="py-4 px-6 rounded-md flex items-center text-lg font-medium w-full justify-center bg-blue-600 hover:bg-blue-700 text-white"
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
      <component
        :is="getDialogComponent(selectedCategory?.id || '')"
        v-if="selectedCategory"
        :is-open="isAddItemDialogOpen"
        :category-id="selectedCategory.id"
        :category-name="selectedCategory.name"
        @close="isAddItemDialogOpen = false"
        @add-item="handleAddItem"
      />
    </div>
  </div>
</template>
