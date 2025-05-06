<script setup lang="ts">
import { ref, computed, watch } from 'vue'
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
  getGetInventorySummaryQueryKey,
  getGetAllFoodItemsQueryKey,
  getGetAllChecklistItemsQueryKey,
  useCreateFoodItem,
  useToggleChecklistItem,
  useUpdateFoodItem,
  useDeleteFoodItem,
  useSetWaterAmount
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
      preparedDays: 0,
      targetDays: 7,
    };
  }

  return {
    food: {
      current: inventorySummary.value.kcal ?? 0,
      target: inventorySummary.value.kcalGoal ?? 0,
      unit: 'kcal',
    },
    water: {
      current: inventorySummary.value.waterLiters ?? 0,
      target: inventorySummary.value.waterLitersGoal ?? 0,
      unit: 'L',
    },
    other: {
      current: inventorySummary.value.checkedItems ?? 0,
      target: inventorySummary.value.totalItems ?? 0,
    },
    preparedDays: 0,
    targetDays: 7,
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
      current: inventorySummary.value.kcal ?? 0,
      target: inventorySummary.value.kcalGoal ?? 0,
      unit: 'kcal',
      items: foodItems.value.map((fi: FoodItemResponse) => {
        console.log('HouseholdInventoryView: Mapping food item for display. fi.expirationDate:', fi.expirationDate, 'Type:', typeof fi.expirationDate, 'Item name:', fi.name);
        
        let dateInputForProcessing: string | number | undefined = fi.expirationDate;
        if (typeof fi.expirationDate === 'number') {
          dateInputForProcessing = fi.expirationDate * 1000; // Convert seconds to milliseconds
          console.log('HouseholdInventoryView: Converted numeric timestamp to milliseconds:', dateInputForProcessing, 'Item name:', fi.name);
        }
        
        const processedExpiryDate = dateInputForProcessing ? new Date(dateInputForProcessing).toISOString().split('T')[0] : null;
        console.log('HouseholdInventoryView: Processed expiryDate for display mapping:', processedExpiryDate, 'Item name:', fi.name);
        return {
          id: fi.id,
          name: fi.name,
          kcal: fi.kcal,
          expiryDate: processedExpiryDate,
          iconName: fi.icon,
          amount: fi.kcal ?? 0,
          unit: 'kcal',
        };
      }),
    });
  } else {
    categories.push({ id: 'food', name: 'Mat', icon: Utensils, current: 0, target: 0, unit: 'kcal', items: [] });
  }

  // Water Category
  if (inventorySummary.value) {
    categories.push({
      id: 'water',
      name: 'Vann',
      icon: Droplet,
      current: inventorySummary.value.waterLiters ?? 0,
      target: inventorySummary.value.waterLitersGoal ?? 0,
      unit: 'L',
      items: [],
    });
  } else {
    categories.push({ id: 'water', name: 'Vann', icon: Droplet, current: 0, target: 0, unit: 'L', items: [] });
  }

  // Checklist Item Categories
  const checklistCategoryMapping: Record<string, { name: string; icon: FunctionalComponent }> = {
    health: { name: 'Helse & hygiene', icon: Ambulance },
    power: { name: 'Lys og strøm', icon: Zap },
    comm: { name: 'Kommunikasjon', icon: Phone },
    misc: { name: 'Diverse', icon: Package },
  };

  if (checklistItems.value) {
    // Group items by their category based on icon/type
    const categorizedItems: Record<string, InventoryItem[]> = {
      health: [],
      power: [],
      comm: [],
      misc: [],
    };

    checklistItems.value.forEach((ci: ChecklistItemResponse) => {
      const item: InventoryItem = {
        id: ci.id,
        name: ci.name,
        checked: ci.checked,
        iconName: ci.icon,
        amount: ci.checked ? 1 : 0,
        unit: 'stk',
      };

      // Determine category based on icon/type
      if (ci.icon?.toLowerCase().includes('health') ||
          ci.icon?.toLowerCase().includes('medical') ||
          ci.icon?.toLowerCase().includes('ambulance') ||
          ci.name.toLowerCase().includes('medisin') ||
          ci.name.toLowerCase().includes('førstehjelp') ||
          ci.name.toLowerCase().includes('hygiene')) {
        categorizedItems.health.push(item);
      } else if (ci.icon?.toLowerCase().includes('power') ||
                 ci.icon?.toLowerCase().includes('light') ||
                 ci.icon?.toLowerCase().includes('battery') ||
                 ci.name.toLowerCase().includes('strøm') ||
                 ci.name.toLowerCase().includes('lys') ||
                 ci.name.toLowerCase().includes('batteri')) {
        categorizedItems.power.push(item);
      } else if (ci.icon?.toLowerCase().includes('phone') ||
                 ci.icon?.toLowerCase().includes('radio') ||
                 ci.icon?.toLowerCase().includes('communication') ||
                 ci.name.toLowerCase().includes('telefon') ||
                 ci.name.toLowerCase().includes('radio') ||
                 ci.name.toLowerCase().includes('kommunikasjon')) {
        categorizedItems.comm.push(item);
      } else {
        categorizedItems.misc.push(item);
      }
    });

    // Create categories with their respective items
    Object.entries(checklistCategoryMapping).forEach(([key, val]) => {
      const items = categorizedItems[key];
      if (items.length > 0) {
        categories.push({
          id: key,
          name: val.name,
          icon: val.icon,
          current: items.filter(item => item.checked).length,
          target: items.length,
          unit: 'stk',
          items: items,
        });
      }
    });
  } else {
    // If no items, create empty categories
    Object.entries(checklistCategoryMapping).forEach(([key, val]) => {
      categories.push({ id: key, name: val.name, icon: val.icon, current: 0, target: 0, unit: 'stk', items: [] });
    });
  }

  return categories;
});

const membersAndGuests = computed(() => {
  if (!household.value) return [];
  return [
    ...((household.value.members ?? []).map(m => ({ type: 'member' as const, data: m }))),
    ...((household.value.guests ?? []).map(g => ({ type: 'guest' as const, data: g })))
  ];
});

// --- Mock data removal and old computed properties ---
// const apiResponse = ref<ApiResponse>({ ... }); // REMOVE THIS MOCK
// The old formattedInventory computed that used apiResponse.value is replaced by the one above.
// --- End mock data removal ---

function navigateToHousehold(): void {
  if (household.value?.id) {
    router.push(`/husstand/${household.value.id}`);
  }
}

function openAddItemDialog(categoryId: string, categoryName: string): void {
  console.log('openAddItemDialog called with:', categoryId, categoryName);
  selectedCategory.value = { id: categoryId, name: categoryName };
  isAddItemDialogOpen.value = true;
  console.log('selectedCategory:', selectedCategory.value);
  console.log('isAddItemDialogOpen:', isAddItemDialogOpen.value);
}

function getDialogComponent(
  categoryId: string
): typeof WaterItemDialog | typeof FoodItemDialog | typeof ChecklistItemDialog | typeof MiscItemDialog | null {
  console.log('getDialogComponent called with:', categoryId);
  switch (categoryId) {
    case 'water':
      return WaterItemDialog;
    case 'food':
      return FoodItemDialog;
    case 'health':
    case 'power':
    case 'comm':
    case 'misc':
      return ChecklistItemDialog;
    default:
      if (displayedCategories.value.find(c => c.id === categoryId && c.unit === 'stk')) {
        return ChecklistItemDialog;
      }
      return null;
  }
}

// Add mutation hooks
const createFoodItem = useCreateFoodItem({
  mutation: {
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: getGetAllFoodItemsQueryKey() });
      queryClient.invalidateQueries({ queryKey: getGetInventorySummaryQueryKey() });
    },
  },
});

const toggleChecklistItem = useToggleChecklistItem({
  mutation: {
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: getGetAllChecklistItemsQueryKey() });
      queryClient.invalidateQueries({ queryKey: getGetInventorySummaryQueryKey() });
    },
  },
});

const updateFoodItem = useUpdateFoodItem({
  mutation: {
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: getGetAllFoodItemsQueryKey() });
      queryClient.invalidateQueries({ queryKey: getGetInventorySummaryQueryKey() });
    },
  },
});

const deleteFoodItemMutation = useDeleteFoodItem({
  mutation: {
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: getGetAllFoodItemsQueryKey() });
      queryClient.invalidateQueries({ queryKey: getGetInventorySummaryQueryKey() });
    },
  },
});

// Add mutation hook for setting water amount
const setWaterAmount = useSetWaterAmount({
  mutation: {
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: getGetInventorySummaryQueryKey() });
    },
  },
});

// Update the handleAddItem function
async function handleAddItem(newItem: InventoryItem): Promise<void> {
  try {
    console.log('HouseholdInventoryView: handleAddItem received newItem:', newItem);
    console.log('HouseholdInventoryView: newItem.expiryDate value:', newItem.expiryDate);
    console.log('HouseholdInventoryView: Type of newItem.expiryDate:', typeof newItem.expiryDate);

    if (!household.value?.id) return;

    if (selectedCategory.value?.id === 'food') {
      const payload = {
        data: {
          name: newItem.name,
          kcal: newItem.kcal ?? 0,
          expirationDate: newItem.expiryDate ? new Date(newItem.expiryDate).toISOString() : undefined,
          icon: newItem.iconName ?? 'utensils'
        }
      };
      console.log('HouseholdInventoryView: Attempting to parse date for payload. Input to new Date():', newItem.expiryDate);
      if (newItem.expiryDate) {
        const dateObject = new Date(newItem.expiryDate);
        console.log('HouseholdInventoryView: Date object created:', dateObject);
        console.log('HouseholdInventoryView: dateObject.toISOString():', dateObject.toISOString());
      }
      console.log('mutating with payload:', payload);
      await createFoodItem.mutateAsync(payload);
    } else {
      // For checklist items (health, power, comm, misc)
      await toggleChecklistItem.mutateAsync({
        id: newItem.id
      });
    }

    isAddItemDialogOpen.value = false;
    selectedCategory.value = null;
  } catch (error) {
    console.error('Error adding item:', error);
    // TODO: Add proper error handling/notification
  }
}

// Update the saveEdit function
async function saveEdit(category: Category, item: InventoryItem): Promise<void> {
  try {
    if (category.id === 'food') {
      await updateFoodItem.mutateAsync({
        id: item.id,
        data: {
          name: editingName.value,
          kcal: editingAmount.value ?? 0,
          expirationDate: item.expiryDate ? new Date(item.expiryDate).toISOString() : undefined,
          icon: item.iconName ?? 'utensils'
        }
      });
    } else {
      // For checklist items
      await toggleChecklistItem.mutateAsync({
        id: item.id
      });
    }
    cancelEdit();
  } catch (error) {
    console.error('Error updating item:', error);
  }
}

// Update the deleteItem function
async function deleteItem(categoryId: string, itemId: string): Promise<void> {
  try {
    if (categoryId === 'food') {
      // This function will now handle the confirmation and deletion directly
      // So this console.warn and return can be removed or adapted
      // For now, let promptDeleteFoodItem handle it entirely
      console.warn("Direct deletion of food item attempted via deleteItem, should use promptDeleteFoodItem or refactor.");
      return;
    } else {
      // For checklist items, we'll use the same toggle mutation
      await toggleChecklistItem.mutateAsync({ id: itemId });
    }
  } catch (error) {
    console.error('Error deleting item:', error);
  }
}

function saveEditWater() {
  if (editingWaterAmount.value !== null) {
    setWaterAmount.mutate({ amount: editingWaterAmount.value });
  }
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
  editingAmount.value = item.kcal ?? (item.amount ?? 0)
}

function cancelEdit(): void {
  editingItemId.value = null
  editingName.value = ''
  editingAmount.value = null
}

// Add a ref for editing water amount
const editingWaterAmount = ref<number | null>(null)

function startEditWater(currentAmount: number) {
  editingWaterAmount.value = currentAmount
}

// When expanding the water category, initialize the editingWaterAmount
watch(expandedCategories, (newVal) => {
  const waterCategory = displayedCategories.value.find(c => c.id === 'water')
  if (newVal.includes('water') && waterCategory) {
    editingWaterAmount.value = waterCategory.current
  }
})

// Updated function for food item deletion confirmation using window.confirm
async function promptDeleteFoodItem(item: InventoryItem): Promise<void> {
  if (window.confirm(`Er du sikker på at du vil slette matvaren "${item.name}"?`)) {
    try {
      await deleteFoodItemMutation.mutateAsync({ id: item.id });
      // Optionally, add a success toast here if desired
    } catch (error) {
      console.error('Error deleting food item after confirmation:', error);
      // Optionally, show an error toast here
    }
  }
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
                    <!-- For water: show inline input and save button -->
                    <template v-if="category.id === 'water'">
                      <div class="flex items-center py-3 px-4">
                        <label class="mr-4 text-lg text-gray-800">Antall liter vann:</label>
                        <input type="number" min="0" step="0.1" v-model.number="editingWaterAmount" class="border rounded px-2 py-1 text-lg w-24 mr-4" />
                        <button @click="saveEditWater" class="bg-blue-600 hover:bg-blue-700 text-white px-4 py-2 rounded text-lg">Lagre</button>
                      </div>
                    </template>
                    <!-- Individual items for other categories -->
                    <template v-else>
                      <div
                        v-for="item in category.items"
                        :key="item.id"
                        :id="`item-${item.id}`"
                        class="flex items-center justify-between py-3 px-4 hover:bg-gray-50 transition-colors duration-200"
                      >
                        <div class="flex items-center">
                          <div class="flex-shrink-0 w-2 h-10 rounded mr-4 bg-blue-300"></div>
                          <template v-if="category.id === 'food' && editingItemId === item.id">
                            <input v-model="editingName" class="border rounded px-2 py-1 text-sm mr-2 w-32" />
                            <input v-model.number="editingAmount" type="number" min="0.1" step="0.1" class="border rounded px-2 py-1 text-sm w-16" />
                          </template>
                          <template v-else-if="category.id === 'food'">
                            <span
                              class="text-xl text-gray-800 cursor-pointer underline decoration-dotted"
                              @click="startEdit(item)"
                            >
                              {{ item.name }}
                            </span>
                          </template>
                          <template v-else-if="category.id === 'water'">
                            <span class="text-xl text-gray-800">{{ item.name }}</span>
                          </template>
                          <template v-else>
                            <!-- Checklist categories: show checkbox -->
                            <input type="checkbox" :checked="item.checked" @change="toggleChecklistItem.mutateAsync({ id: item.id })" class="mr-2" />
                            <span class="text-lg text-gray-800">{{ item.name }}</span>
                          </template>
                        </div>
                        <div class="flex items-center space-x-6">
                          <div class="text-right">
                            <template v-if="category.id === 'food' && editingItemId === item.id">
                              <span class="font-medium px-2 py-1 rounded-full text-sm bg-blue-100 text-blue-800">{{ editingAmount }}</span>
                            </template>
                            <template v-else-if="category.id === 'food'">
                              <span class="font-medium px-2 py-1 rounded-full text-sm bg-blue-100 text-blue-800">{{ item.amount }} {{ item.unit }}</span>
                            </template>
                            <!-- For water and checklist, do not show amount/unit here -->
                          </div>
                          <template v-if="category.id === 'food' && editingItemId === item.id">
                            <button @click="saveEdit(category, item)" class="text-green-600 hover:text-green-800 mr-2">✓</button>
                            <button @click="cancelEdit" class="text-gray-500 hover:text-red-600">✗</button>
                          </template>
                          <template v-if="category.id === 'food' && item.expiryDate">
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
                                      timeZone: 'UTC'
                                    })
                                    : 'Ingen dato'
                                }}
                              </span>
                            </div>
                          </template>
                          <button
                            v-if="category.id === 'food'"
                            class="text-red-500 hover:text-red-700 p-1 rounded-full hover:bg-red-50"
                            @click="promptDeleteFoodItem(item)"
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

                      <!-- Add item button: only for food -->
                      <div v-if="category.id === 'food'" class="p-3 bg-blue-50">
                        <button
                          @click="openAddItemDialog(category.id, category.name)"
                          class="py-4 px-6 rounded-md flex items-center text-lg font-medium w-full justify-center bg-blue-600 hover:bg-blue-700 text-white"
                        >
                          <Plus class="mr-2 h-4 w-4" /> Legg til vare
                        </button>
                      </div>
                    </template>
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

<style>
@keyframes modalShow {
  to {
    transform: scale(1);
    opacity: 1;
  }
}
.animate-modalShow {
  animation: modalShow 0.3s forwards;
}
</style>
