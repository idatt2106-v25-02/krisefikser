<script setup lang="ts">
import { ref, computed, watch, watchEffect } from 'vue'
import type { FunctionalComponent } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth/useAuthStore' // For enabling queries
import { useQueryClient } from '@tanstack/vue-query' // For potential invalidation later
import {
  Ambulance,
  ChevronDown,
  Droplet,
  Edit,
  Home,
  Info,
  Package,
  Phone,
  Plus,
  Save,
  Trash,
  Utensils,
  XCircle,
  Zap,
} from 'lucide-vue-next'
import HouseholdEmergencySupplies from '@/components/household/HouseholdEmergencySupplies.vue'
import ProductSearch from '@/components/inventory/ProductSearch.vue'
import WaterItemDialog from '@/components/inventory/dialog/WaterItemDialog.vue'
import FoodItemDialog from '@/components/inventory/dialog/FoodItemDialog.vue'
import ChecklistItemDialog from '@/components/inventory/dialog/ChecklistItemDialog.vue'
import MiscItemDialog from '@/components/inventory/dialog/MiscItemDialog.vue'
import PreparednessInfoDialog from '@/components/inventory/info/PreparednessInfoDialog.vue'

// Import API hooks and types
import {
  getGetAllChecklistItemsQueryKey,
  getGetAllFoodItemsQueryKey,
  getGetInventorySummaryQueryKey,
  useCreateFoodItem,
  useDeleteFoodItem,
  useGetAllChecklistItems,
  useGetAllFoodItems,
  useGetInventorySummary,
  useSetWaterAmount,
  useToggleChecklistItem,
  useUpdateFoodItem,
} from '@/api/generated/item/item'
import type {
  ChecklistItemResponse,
  FoodItemResponse,
  GuestResponse,
  HouseholdMemberResponse,
} from '@/api/generated/model'
import { useGetActiveHousehold } from '@/api/generated/household/household'

// Define types for data structures (will need adjustments based on backend DTOs)
interface InventoryItem {
  id: string
  name: string
  amount: number
  unit: string
  type?: string
  expiryDate?: string | null
  checked?: boolean
  kcal?: number
  iconName?: string
}

interface Category {
  id: string
  name: string
  icon: FunctionalComponent
  current: number
  target: number
  unit: string
  items: InventoryItem[]
}

interface FormattedCategory {
  current: number
  target: number
  unit: string
}

interface FormattedInventory {
  food: FormattedCategory
  water: FormattedCategory
  other: {
    current: number
    target: number
  }
  preparedDays: number
  targetDays: number
}

const router = useRouter()
const authStore = useAuthStore()
const queryClient = useQueryClient()

// Get active household
const {
  data: household,
  isError: isErrorHousehold,
  error: errorHousehold,
} = useGetActiveHousehold({
  query: {
    retry: 0,
    enabled: authStore.isAuthenticated,
  },
})

// Add error handling watchEffect
watchEffect(() => {
  if (isErrorHousehold.value) {
    const err = errorHousehold.value as { response?: { status: number } }
    if (err?.response?.status === 404) {
      router.push('/bli-med-eller-opprett-husstand')
    }
  }
})

// Define householdId as computed that depends on the active household
const householdId = computed(() => household.value?.id ?? '')

// 1. Fetch Inventory Summary
const { data: inventorySummary } = useGetInventorySummary({
  // Orval options object for useQuery
  query: {
    enabled: computed(() => authStore.isAuthenticated && !!householdId.value), // Fetch only if authenticated and householdId is available
    // staleTime: 5 * 60 * 1000, // 5 minutes, example
  },
})

// 2. Fetch Food Items
const {
  data: foodItems, // This will be FoodItemResponse[] | undefined
} = useGetAllFoodItems({
  query: {
    enabled: computed(() => authStore.isAuthenticated && !!householdId.value),
  },
})

// 3. Fetch Checklist Items
const {
  data: checklistItems, // This will be ChecklistItemResponse[] | undefined
} = useGetAllChecklistItems({
  query: {
    enabled: computed(() => authStore.isAuthenticated && !!householdId.value),
  },
})

// Create a computed property to format data for the HouseholdEmergencySupplies component
const formattedInventory = computed<FormattedInventory>(() => {
  const DAYS_GOAL = 7 // Align with backend's target days

  if (!inventorySummary.value) {
    return {
      food: { current: 0, target: 0, unit: 'kcal' },
      water: { current: 0, target: 0, unit: 'L' },
      other: { current: 0, target: 0 },
      preparedDays: 0,
      targetDays: DAYS_GOAL,
    }
  }

  const summary = inventorySummary.value
  let foodDays = 0
  const dailyKcalNeeded = summary.kcalGoal > 0 ? summary.kcalGoal / DAYS_GOAL : 0
  if (dailyKcalNeeded > 0) {
    foodDays = summary.kcal / dailyKcalNeeded
  } else if (summary.kcal > 0 && summary.kcalGoal === 0) {
    foodDays = DAYS_GOAL
  }

  let waterDays = 0
  const dailyWaterNeeded = summary.waterLitersGoal > 0 ? summary.waterLitersGoal / DAYS_GOAL : 0
  if (dailyWaterNeeded > 0) {
    waterDays = summary.waterLiters / dailyWaterNeeded
  } else if (summary.waterLiters > 0 && summary.waterLitersGoal === 0) {
    waterDays = DAYS_GOAL
  }

  const calculatedPreparedDays = Math.min(foodDays, waterDays)
  let finalPreparedDays
  if (isFinite(calculatedPreparedDays)) {
    finalPreparedDays = Math.min(Math.floor(calculatedPreparedDays), DAYS_GOAL)
  }

  // Refined logic for when goals might be zero:
  const effectiveFoodDays =
    summary.kcalGoal === 0 && summary.kcal > 0
      ? DAYS_GOAL
      : dailyKcalNeeded > 0
        ? summary.kcal / dailyKcalNeeded
        : 0
  const effectiveWaterDays =
    summary.waterLitersGoal === 0 && summary.waterLiters > 0
      ? DAYS_GOAL
      : dailyWaterNeeded > 0
        ? summary.waterLiters / dailyWaterNeeded
        : 0

  let derivedPreparedDays
  if (summary.kcalGoal === 0 && summary.waterLitersGoal === 0) {
    derivedPreparedDays = summary.kcal > 0 || summary.waterLiters > 0 ? DAYS_GOAL : 0
  } else if (summary.kcalGoal === 0) {
    // Only food goal is 0
    derivedPreparedDays = summary.kcal > 0 ? Math.floor(effectiveWaterDays) : 0
  } else if (summary.waterLitersGoal === 0) {
    // Only water goal is 0
    derivedPreparedDays = summary.waterLiters > 0 ? Math.floor(effectiveFoodDays) : 0
  } else {
    // Both goals are > 0
    derivedPreparedDays = Math.floor(Math.min(effectiveFoodDays, effectiveWaterDays))
  }

  finalPreparedDays = Math.min(derivedPreparedDays, DAYS_GOAL)
  if (finalPreparedDays < 0) finalPreparedDays = 0 // Ensure not negative

  return {
    food: {
      current: summary.kcal ?? 0,
      target: summary.kcalGoal ?? 0,
      unit: 'kcal',
    },
    water: {
      current: summary.waterLiters ?? 0,
      target: summary.waterLitersGoal ?? 0,
      unit: 'L',
    },
    other: {
      current: summary.checkedItems ?? 0,
      target: summary.totalItems ?? 0,
    },
    preparedDays: finalPreparedDays,
    targetDays: DAYS_GOAL,
  }
})

const displayedCategories = computed<Category[]>(() => {
  const categories: Category[] = []

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
        let dateInputForProcessing: string | number | undefined = fi.expirationDate
        if (typeof fi.expirationDate === 'number') {
          dateInputForProcessing = fi.expirationDate * 1000
        }

        const processedExpiryDate = dateInputForProcessing
          ? new Date(dateInputForProcessing).toISOString().split('T')[0]
          : null
        return {
          id: fi.id,
          name: fi.name,
          kcal: fi.kcal,
          expiryDate: processedExpiryDate,
          iconName: fi.icon,
          amount: fi.kcal ?? 0,
          unit: 'kcal',
        }
      }),
    })
  } else {
    categories.push({
      id: 'food',
      name: 'Mat',
      icon: Utensils,
      current: 0,
      target: 0,
      unit: 'kcal',
      items: [],
    })
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
    })
  } else {
    categories.push({
      id: 'water',
      name: 'Vann',
      icon: Droplet,
      current: 0,
      target: 0,
      unit: 'L',
      items: [],
    })
  }

  // Checklist Item Categories
  const checklistCategoryMapping: Record<string, { name: string; icon: FunctionalComponent }> = {
    health: { name: 'Helse & hygiene', icon: Ambulance },
    power: { name: 'Lys og strøm', icon: Zap },
    comm: { name: 'Kommunikasjon', icon: Phone },
    misc: { name: 'Diverse', icon: Package },
  }

  if (checklistItems.value) {
    // Group items by their category based on icon/type
    const categorizedItems: Record<string, InventoryItem[]> = {
      health: [],
      power: [],
      comm: [],
      misc: [],
    }

    checklistItems.value.forEach((ci: ChecklistItemResponse) => {
      const item: InventoryItem = {
        id: ci.id,
        name: ci.name,
        checked: ci.checked,
        iconName: ci.icon,
        amount: ci.checked ? 1 : 0,
        unit: 'stk',
      }

      // Determine category based on icon/type
      if (
        ci.icon?.toLowerCase().includes('health') ||
        ci.icon?.toLowerCase().includes('medical') ||
        ci.icon?.toLowerCase().includes('ambulance') ||
        ci.name.toLowerCase().includes('medisin') ||
        ci.name.toLowerCase().includes('førstehjelp') ||
        ci.name.toLowerCase().includes('hygiene')
      ) {
        categorizedItems.health.push(item)
      } else if (
        ci.icon?.toLowerCase().includes('power') ||
        ci.icon?.toLowerCase().includes('light') ||
        ci.icon?.toLowerCase().includes('battery') ||
        ci.name.toLowerCase().includes('strøm') ||
        ci.name.toLowerCase().includes('lys') ||
        ci.name.toLowerCase().includes('batteri')
      ) {
        categorizedItems.power.push(item)
      } else if (
        ci.icon?.toLowerCase().includes('phone') ||
        ci.icon?.toLowerCase().includes('radio') ||
        ci.icon?.toLowerCase().includes('communication') ||
        ci.name.toLowerCase().includes('telefon') ||
        ci.name.toLowerCase().includes('radio') ||
        ci.name.toLowerCase().includes('kommunikasjon')
      ) {
        categorizedItems.comm.push(item)
      } else {
        categorizedItems.misc.push(item)
      }
    })

    // Create categories with their respective items
    Object.entries(checklistCategoryMapping).forEach(([key, val]) => {
      const items = categorizedItems[key]
      if (items.length > 0) {
        categories.push({
          id: key,
          name: val.name,
          icon: val.icon,
          current: items.filter((item) => item.checked).length,
          target: items.length,
          unit: 'stk',
          items: items,
        })
      }
    })
  } else {
    // If no items, create empty categories
    Object.entries(checklistCategoryMapping).forEach(([key, val]) => {
      categories.push({
        id: key,
        name: val.name,
        icon: val.icon,
        current: 0,
        target: 0,
        unit: 'stk',
        items: [],
      })
    })
  }

  return categories
})

const membersAndGuests = computed(() => {
  if (!household.value) return []
  return [
    ...(household.value.members ?? []).map((m) => ({ type: 'member' as const, data: m })),
    ...(household.value.guests ?? []).map((g) => ({ type: 'guest' as const, data: g })),
  ]
})


function navigateToHousehold() {
  router.push('/husstand')
}

function openAddItemDialog(categoryId: string, categoryName: string): void {
  selectedCategory.value = { id: categoryId, name: categoryName }
  isAddItemDialogOpen.value = true
}

function getDialogComponent(
  categoryId: string,
):
  | typeof WaterItemDialog
  | typeof FoodItemDialog
  | typeof ChecklistItemDialog
  | typeof MiscItemDialog
  | null {
  switch (categoryId) {
    case 'water':
      return WaterItemDialog
    case 'food':
      return FoodItemDialog
    case 'health':
    case 'power':
    case 'comm':
    case 'misc':
      return ChecklistItemDialog
    default:
      if (displayedCategories.value.find((c) => c.id === categoryId && c.unit === 'stk')) {
        return ChecklistItemDialog
      }
      return null
  }
}

// Add mutation hooks
const createFoodItem = useCreateFoodItem({
  mutation: {
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: getGetAllFoodItemsQueryKey() })
      queryClient.invalidateQueries({ queryKey: getGetInventorySummaryQueryKey() })
    },
  },
})

const toggleChecklistItem = useToggleChecklistItem({
  mutation: {
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: getGetAllChecklistItemsQueryKey() })
      queryClient.invalidateQueries({ queryKey: getGetInventorySummaryQueryKey() })
    },
  },
})

const updateFoodItem = useUpdateFoodItem({
  mutation: {
    onSuccess: () => {

      queryClient.invalidateQueries({ queryKey: getGetAllFoodItemsQueryKey() })
      queryClient.invalidateQueries({ queryKey: getGetInventorySummaryQueryKey() })
    },
  },
})

const deleteFoodItemMutation = useDeleteFoodItem({
  mutation: {
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: getGetAllFoodItemsQueryKey() })
      queryClient.invalidateQueries({ queryKey: getGetInventorySummaryQueryKey() })
    },
  },
})

// Add mutation hook for setting water amount
const setWaterAmount = useSetWaterAmount({
  mutation: {
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: getGetInventorySummaryQueryKey() })
    },
  },
})

const originalCategoriesState = ref<string[]>([])
const expandedCategories = ref<string[]>([])

// Add a ref for editing water amount
const editingWaterAmount = ref<number | null>(null)
const waterToAdd = ref<number | null>(null) // New ref for water to add
const waterToSubtract = ref<number | null>(null) // New ref for water to subtract

// When expanding the water category, initialize the editingWaterAmount and add/subtract fields
watch(expandedCategories, (newVal) => {
  const waterCategory = displayedCategories.value.find((c) => c.id === 'water')
  if (newVal.includes('water') && waterCategory) {
    editingWaterAmount.value = waterCategory.current
    waterToAdd.value = null
    waterToSubtract.value = null
  }
})

// Handler for when the 'waterToAdd' input changes
function handleWaterAddChange() {
  const currentActual = displayedCategories.value.find((c) => c.id === 'water')?.current ?? 0
  if (waterToAdd.value !== null && waterToAdd.value >= 0) {
    editingWaterAmount.value = currentActual + waterToAdd.value
    waterToSubtract.value = null // Clear the other field
  } else if (waterToAdd.value === null && waterToSubtract.value === null) {
    editingWaterAmount.value = currentActual // Both empty, revert to current actual
  } else if (waterToAdd.value === null && waterToSubtract.value !== null) {
    editingWaterAmount.value = Math.max(0, currentActual - (waterToSubtract.value || 0))
  }
}

// Handler for when the 'waterToSubtract' input changes
function handleWaterSubtractChange() {
  const currentActual = displayedCategories.value.find((c) => c.id === 'water')?.current ?? 0
  if (waterToSubtract.value !== null && waterToSubtract.value >= 0) {
    editingWaterAmount.value = Math.max(0, currentActual - waterToSubtract.value)
    waterToAdd.value = null // Clear the other field
  } else if (waterToSubtract.value === null && waterToAdd.value === null) {
    editingWaterAmount.value = currentActual // Both empty, revert to current actual
  } else if (waterToSubtract.value === null && waterToAdd.value !== null) {
    editingWaterAmount.value = currentActual + (waterToAdd.value || 0)
  }
}

function saveEditWater() {
  if (editingWaterAmount.value !== null && editingWaterAmount.value >= 0) {
    setWaterAmount.mutate({ amount: editingWaterAmount.value })
    // Reset add/subtract fields after saving for a cleaner UI next time
    waterToAdd.value = null
    waterToSubtract.value = null
    // Collapse the water category accordion
    expandedCategories.value = expandedCategories.value.filter((id) => id !== 'water')
    // editingWaterAmount will be updated by the watch(expandedCategories) when data refetches
  }
}

const isAddItemDialogOpen = ref(false)
const selectedCategory = ref<{ id: string; name: string } | null>(null)
const isSearchActive = ref(false)
const isPreparednessInfoDialogOpen = ref(false)

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
        setTimeout(() => {
          itemElement.classList.remove('bg-blue-50')
        }, 1000)
      }, 1000)
    }
  }, 100)
}

function handleSearchChanged(isActive: boolean): void {
  isSearchActive.value = isActive
  if (isActive && !originalCategoriesState.value.length) {
    originalCategoriesState.value = [...expandedCategories.value]
  }
  if (!isActive) {
    expandedCategories.value = [...originalCategoriesState.value]
    originalCategoriesState.value = []
  }
}

const editingItemId = ref<string | null>(null)
const editingName = ref('')
const editingAmount = ref<number | null>(null)
const editingExpiryDate = ref<string | null>(null)

function startEdit(item: InventoryItem): void {
  if (editingItemId.value === item.id) return

  editingItemId.value = item.id
  editingName.value = item.name
  editingAmount.value = item.kcal ?? item.amount ?? 0
  editingExpiryDate.value = item.expiryDate ? item.expiryDate.split('T')[0] : null
}

function cancelEdit(): void {
  editingItemId.value = null
  editingName.value = ''
  editingAmount.value = null
  editingExpiryDate.value = null // Clear editing expiry date
}

async function promptDeleteFoodItem(item: InventoryItem): Promise<void> {
  if (window.confirm(`Er du sikker på at du vil slette matvaren "${item.name}"?`)) {
    try {
      await deleteFoodItemMutation.mutateAsync({ id: item.id })
    } catch (error) {
      console.error('Error deleting food item after confirmation:', error)
    }
  }
}

async function handleAddItem(newItem: InventoryItem): Promise<void> {
  try {
    if (!household.value?.id) return

    if (selectedCategory.value?.id === 'food') {
      const payload = {
        data: {
          name: newItem.name,
          kcal: newItem.kcal ?? 0,
          expirationDate: newItem.expiryDate
            ? new Date(newItem.expiryDate).toISOString()
            : undefined,
          icon: newItem.iconName ?? 'utensils',
        },
      }
      if (newItem.expiryDate) {
      }
      await createFoodItem.mutateAsync(payload)
    } else {
      await toggleChecklistItem.mutateAsync({
        id: newItem.id,
      })
    }

    isAddItemDialogOpen.value = false
    selectedCategory.value = null
  } catch (error) {
    console.error('Error adding item:', error)
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
          expirationDate: editingExpiryDate.value
            ? new Date(editingExpiryDate.value).toISOString()
            : undefined,
          icon: item.iconName ?? 'utensils',
        },
      })
    } else {
      // For checklist items
      await toggleChecklistItem.mutateAsync({
        id: item.id,
      })
    }
    cancelEdit()
  } catch (error) {
    console.error('Error updating item:', error)
  }
}

// Add focus trap functionality
function handleTabKey(event: KeyboardEvent, categoryId: string) {
  if (event.key === 'Tab') {
    const categoryElement = document.getElementById(`category-${categoryId}`)
    if (!categoryElement) return

    const focusableElements = categoryElement.querySelectorAll(
      'button, [href], input, select, textarea, [tabindex]:not([tabindex="-1"])'
    )
    const firstFocusableElement = focusableElements[0] as HTMLElement
    const lastFocusableElement = focusableElements[focusableElements.length - 1] as HTMLElement

    if (event.shiftKey) {
      if (document.activeElement === firstFocusableElement) {
        event.preventDefault()
        lastFocusableElement.focus()
      }
    } else {
      if (document.activeElement === lastFocusableElement) {
        event.preventDefault()
        firstFocusableElement.focus()
      }
    }
  }
}
</script>

<template>
  <div class="bg-gray-50 min-h-screen">
    <div class="max-w-8xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      <div class="bg-white rounded-lg shadow-sm border border-gray-200 p-6 mb-6">
        <div class="flex items-center text-sm text-gray-500 mb-3">
          <button class="hover:text-blue-600 flex items-center" @click="navigateToHousehold">
            <Home class="h-5 w-5 mr-2" />
            {{ household?.name }}
          </button>
          <span class="mx-2">/</span>
          <span class="text-gray-800">Beredskapslager</span>
        </div>
        <div class="flex items-center justify-between">
          <h1 class="text-2xl font-bold text-gray-900">Beredskapslager</h1>
        </div>
        <p class="text-base text-gray-600 mt-4">
          Her kan du se oversikt over beredskapslageret ditt, inkludert matvarer, utstyr og andre
          viktige ressurser.
        </p>
      </div>

      <!-- Main content area -->
      <div class="grid grid-cols-1 lg:grid-cols-12 gap-6">
        <!-- Left column - Overview -->
        <div class="lg:col-span-4 space-y-6">
          <div class="bg-white rounded-lg shadow-sm border border-gray-200 p-6">
            <div class="flex items-center justify-between mb-4">
              <h2 class="text-lg font-semibold text-gray-800">Oversikt</h2>
              <button
                class="text-blue-600 hover:text-blue-700 p-1.5 rounded-full hover:bg-blue-50 transition-colors -mr-1"
                title="Vis informasjon om beredskapsberegning"
                @click="isPreparednessInfoDialogOpen = true"
              >
                <Info class="h-5 w-5" />
              </button>
            </div>
            <HouseholdEmergencySupplies
              :household-id="householdId"
              :inventory="formattedInventory"
              :show-details-button="false"
              @open-info-dialog="isPreparednessInfoDialogOpen = true"
            />
          </div>

          <!-- New: Add a card showing household members -->
          <div class="bg-white rounded-lg shadow-sm border border-gray-200 p-6">
            <h2 class="text-lg font-semibold text-gray-800 mb-4">Husstandsmedlemmer</h2>
            <p class="text-base text-gray-600 mb-4">
              Alle disse personene har tilgang til beredskapslageret:
            </p>
            <ul class="space-y-4 max-h-40 overflow-y-auto pr-2">
              <li
                v-for="person in membersAndGuests"
                :key="
                  person.type === 'member'
                    ? (person.data as HouseholdMemberResponse).user?.id
                    : (person.data as GuestResponse).id
                "
                class="flex items-center p-4 bg-gray-50 rounded"
              >
                <div
                  :class="
                    person.type === 'member'
                      ? 'bg-blue-100 text-blue-700'
                      : 'bg-green-100 text-green-700'
                  "
                  class="h-12 w-12 rounded-full flex items-center justify-center mr-4"
                >
                  <span class="font-bold">
                    {{
                      person.type === 'member'
                        ? ((person.data as HouseholdMemberResponse).user?.firstName?.[0] ?? '?')
                        : ((person.data as GuestResponse).name?.[0]?.toUpperCase() ?? 'G')
                    }}
                  </span>
                </div>
                <span class="text-base font-medium">
                  {{
                    person.type === 'member'
                      ? (person.data as HouseholdMemberResponse).user?.firstName +
                        ' ' +
                        (person.data as HouseholdMemberResponse).user?.lastName
                      : (person.data as GuestResponse).name
                  }}
                </span>
                <span
                  v-if="
                    person.type === 'member' &&
                    (person.data as HouseholdMemberResponse).user?.id === authStore.currentUser?.id
                  "
                  class="ml-3 text-sm bg-blue-100 text-blue-800 px-2 py-1 rounded"
                  >Du</span
                >
                <span
                  v-else-if="person.type === 'guest'"
                  class="ml-3 text-sm bg-green-100 text-green-800 px-2 py-1 rounded"
                  >Gjest</span
                >
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
              <h2 class="text-lg font-semibold text-gray-800">Produkter</h2>
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
                      ? (expandedCategories = expandedCategories.filter((id) => id !== category.id))
                      : expandedCategories.push(category.id)
                  "
                  @keydown.enter="
                    expandedCategories.includes(category.id)
                      ? (expandedCategories = expandedCategories.filter((id) => id !== category.id))
                      : expandedCategories.push(category.id)
                  "
                  @keydown.space.prevent="
                    expandedCategories.includes(category.id)
                      ? (expandedCategories = expandedCategories.filter((id) => id !== category.id))
                      : expandedCategories.push(category.id)
                  "
                  role="button"
                  :aria-expanded="expandedCategories.includes(category.id)"
                  :aria-controls="`category-${category.id}`"
                  tabindex="0"
                >
                  <div class="flex items-center">
                    <div
                      class="flex-shrink-0 h-8 w-8 rounded-full flex items-center justify-center mr-3 bg-blue-100 text-blue-700"
                      aria-hidden="true"
                    >
                      <component :is="category.icon" class="h-5 w-5" />
                    </div>
                    <span class="text-base font-medium text-blue-800">{{ category.name }}</span>
                  </div>
                  <div class="flex items-center">
                    <!-- Progress indicator -->
                    <div class="w-20 bg-gray-200 rounded-full h-2.5 mr-4 overflow-hidden" role="progressbar" :aria-valuenow="category.current" :aria-valuemin="0" :aria-valuemax="category.target">
                      <div
                        :style="`width: ${Math.min(100, (category.current / category.target) * 100)}%`"
                        class="h-2.5 rounded-full bg-blue-500"
                      ></div>
                    </div>
                    <span class="mr-4 text-base font-medium">
                      {{ category.current }} / {{ category.target }} {{ category.unit }}
                    </span>
                    <ChevronDown
                      :class="{ 'transform rotate-180': expandedCategories.includes(category.id) }"
                      class="h-5 w-5 text-gray-500 transition-transform duration-200"
                      aria-hidden="true"
                    />
                  </div>
                </div>

                <!-- Category items -->
                <div
                  v-show="expandedCategories.includes(category.id)"
                  :id="`category-${category.id}`"
                  role="region"
                  :aria-label="`${category.name} items`"
                  @keydown="(e) => handleTabKey(e, category.id)"
                >
                  <div class="divide-y divide-gray-100">
                    <!-- For water: show inline input and save button -->
                    <template v-if="category.id === 'water'">
                      <div class="p-4 space-y-4">
                        <div class="grid sm:grid-cols-2 gap-4 items-end">
                          <div>
                            <label class="block mb-1 text-sm text-gray-700" for="waterAddInput"
                              >Legg til liter:</label
                            >
                            <input
                              id="waterAddInput"
                              v-model.number="waterToAdd"
                              class="border border-gray-300 rounded-md shadow-sm px-3 py-2 text-base focus:ring-1 focus:ring-blue-500 focus:border-blue-500 w-full"
                              min="0"
                              placeholder="0.0"
                              step="0.1"
                              type="number"
                              @input="handleWaterAddChange"
                              @keydown.enter.prevent="saveEditWater"
                            />
                          </div>
                          <div>
                            <label class="block mb-1 text-sm text-gray-700" for="waterSubtractInput"
                              >Trekk fra liter:</label
                            >
                            <input
                              id="waterSubtractInput"
                              v-model.number="waterToSubtract"
                              class="border border-gray-300 rounded-md shadow-sm px-3 py-2 text-base focus:ring-1 focus:ring-blue-500 focus:border-blue-500 w-full"
                              min="0"
                              placeholder="0.0"
                              step="0.1"
                              type="number"
                              @input="handleWaterSubtractChange"
                              @keydown.enter.prevent="saveEditWater"
                            />
                          </div>
                        </div>

                        <div class="flex justify-between items-center pt-3">
                          <div class="text-base text-gray-800">
                            Nytt totalt antall:
                            <span class="font-semibold text-blue-600"
                              >{{
                                editingWaterAmount === null ? category.current : editingWaterAmount
                              }}
                              L</span
                            >
                          </div>
                          <button
                            class="flex items-center bg-blue-600 text-white hover:bg-blue-700 px-3 py-1.5 rounded-md transition-colors text-base font-medium"
                            @click="saveEditWater"
                            @keydown.enter.prevent="saveEditWater"
                          >
                            <Save class="h-5 w-5 mr-2" />
                            Oppdater vannmengde
                          </button>
                        </div>
                      </div>
                    </template>
                    <!-- Individual items for other categories -->
                    <template v-else>
                      <div
                        v-for="item in category.items"
                        :id="`item-${item.id}`"
                        :key="item.id"
                        class="flex items-center justify-between py-3 px-4 hover:bg-gray-50 transition-colors duration-200 relative"
                        @click="
                          category.id === 'food' && editingItemId !== item.id
                            ? startEdit(item)
                            : null
                        "
                        @keydown.enter="
                          category.id === 'food' && editingItemId !== item.id
                            ? startEdit(item)
                            : null
                        "
                        @keydown.space.prevent="
                          category.id === 'food' && editingItemId !== item.id
                            ? startEdit(item)
                            : null
                        "
                        role="button"
                        tabindex="0"
                        :aria-label="`${item.name}, ${item.amount} ${item.unit}`"
                      >
                        <div class="flex items-center flex-grow">
                          <div class="flex-shrink-0 w-2 h-10 rounded mr-4 bg-blue-300" aria-hidden="true"></div>
                          <template v-if="category.id === 'food' && editingItemId === item.id">
                            <div class="flex flex-col sm:flex-row sm:items-center gap-2 flex-grow">
                              <input
                                v-model="editingName"
                                class="border border-gray-300 rounded-md shadow-sm px-2 py-1 text-sm focus:ring-1 focus:ring-blue-500 focus:border-blue-500 sm:w-32"
                                placeholder="Navn"
                                @keydown.enter.prevent="saveEdit(category, item)"
                                @click.stop="() => {}"
                              />
                              <input
                                v-model.number="editingAmount"
                                class="border border-gray-300 rounded-md shadow-sm px-2 py-1 text-sm focus:ring-1 focus:ring-blue-500 focus:border-blue-500 w-20 sm:w-16"
                                min="0.1"
                                placeholder="Kcal"
                                step="0.1"
                                type="number"
                                @keydown.enter.prevent="saveEdit(category, item)"
                                @click.stop="() => {}"
                              />
                              <input
                                v-model="editingExpiryDate"
                                class="border border-gray-300 rounded-md shadow-sm px-2 py-1 text-sm focus:ring-1 focus:ring-blue-500 focus:border-blue-500"
                                type="date"
                                @keydown.enter.prevent="saveEdit(category, item)"
                                @click.stop="() => {}"
                              />
                            </div>
                          </template>
                          <template v-else-if="category.id === 'food'">
                            <span class="text-base text-gray-800 cursor-pointer">
                              {{ item.name }}
                            </span>
                          </template>
                          <template v-else-if="category.id === 'water'">
                            <span class="text-base text-gray-800">{{ item.name }}</span>
                          </template>
                          <template v-else>
                            <!-- Checklist categories: show checkbox -->
                            <label
                              :for="`checkbox-${item.id}`"
                              class="flex items-center cursor-pointer flex-grow group transition-all duration-200"
                            >
                              <div class="relative flex items-center">
                                <input
                                  :id="`checkbox-${item.id}`"
                                  :checked="item.checked"
                                  class="h-5 w-5 text-blue-600 border-gray-300 rounded focus:ring-blue-500 mr-3 accent-blue-600"
                                  type="checkbox"
                                  @change="toggleChecklistItem.mutateAsync({ id: item.id })"
                                />
                              </div>
                              <span
                                class="text-base text-gray-800 group-hover:text-blue-700 transition-colors duration-200"
                              >
                                {{ item.name }}
                              </span>
                              <span
                                v-if="item.checked"
                                class="ml-2 text-xs font-medium px-2 py-0.5 rounded-full bg-green-100 text-green-800 transition-all duration-200"
                              >
                                Fullført
                              </span>
                            </label>
                          </template>
                        </div>
                        <div class="flex items-center space-x-2 sm:space-x-3">
                          <!-- AMOUNT DISPLAY (only when not editing food) -->
                          <div
                            v-if="category.id === 'food' && editingItemId !== item.id"
                            class="text-right min-w-[70px] sm:min-w-[90px]"
                          >
                            <span
                              class="font-medium px-2 py-1 rounded-full text-xs bg-blue-100 text-blue-800"
                              >{{ item.amount }} {{ item.unit }}</span
                            >
                          </div>

                          <!-- EDIT MODE SAVE/CANCEL BUTTONS -->
                          <template v-if="category.id === 'food' && editingItemId === item.id">
                            <div class="flex items-center space-x-2">
                              <button
                                class="flex items-center bg-green-600 text-white hover:bg-green-500 px-3 py-1.5 rounded-md transition-colors text-sm"
                                title="Lagre endringer"
                                @click.stop="saveEdit(category, item)"
                                @keydown.enter.prevent="saveEdit(category, item)"
                              >
                                <Save class="h-4 w-4 sm:mr-1" />
                                <span class="hidden sm:inline">Lagre</span>
                              </button>
                              <button
                                class="flex items-center bg-red-600 text-white hover:bg-red-500 px-3 py-1.5 rounded-md transition-colors text-sm"
                                title="Avbryt redigering"
                                @click.stop="cancelEdit"
                                @keydown.enter.prevent="cancelEdit"
                              >
                                <XCircle class="h-4 w-4 sm:mr-1" />
                                <span class="hidden sm:inline">Avbryt</span>
                              </button>
                            </div>
                          </template>

                          <!-- ACTION BUTTONS (Edit/Delete - only when not editing food) -->
                          <template v-if="category.id === 'food' && editingItemId !== item.id">
                            <div class="flex items-center space-x-1 sm:space-x-2">
                              <button
                                class="text-blue-600 hover:text-blue-800 p-1.5 rounded-md hover:bg-blue-50 transition-colors"
                                title="Rediger matvare"
                                @click.stop="startEdit(item)"
                                @keydown.enter.prevent="startEdit(item)"
                              >
                                <Edit class="h-5 w-5" />
                              </button>
                              <button
                                class="text-red-500 hover:text-red-700 p-1.5 rounded-md hover:bg-red-50 transition-colors"
                                title="Slett matvare"
                                @click.stop="promptDeleteFoodItem(item)"
                                @keydown.enter.prevent="promptDeleteFoodItem(item)"
                              >
                                <Trash class="h-5 w-5" />
                              </button>
                            </div>
                          </template>
                        </div>
                      </div>

                      <!-- Add item button: only for food -->
                      <div v-if="category.id === 'food'" class="p-2 bg-blue-50">
                        <button
                          class="py-2 px-3 rounded flex items-center text-base font-medium w-full justify-center bg-blue-600 hover:bg-blue-700 text-white"
                          @click="openAddItemDialog(category.id, category.name)"
                          @keydown.enter.prevent="openAddItemDialog(category.id, category.name)"
                        >
                          <Plus class="mr-2 h-4 w-4" />
                          Legg til vare
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
        :category-id="selectedCategory.id"
        :category-name="selectedCategory.name"
        :is-open="isAddItemDialogOpen"
        @close="isAddItemDialogOpen = false"
        @add-item="handleAddItem"
      />

      <!-- Preparedness Info Dialog -->
      <PreparednessInfoDialog
        :is-open="isPreparednessInfoDialogOpen"
        @close="isPreparednessInfoDialogOpen = false"
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
</style>
