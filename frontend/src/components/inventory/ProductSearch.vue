<script setup lang="ts">
import { ref, computed, watch } from 'vue';
import { Search, Filter, Clock } from 'lucide-vue-next';
import { Input } from '@/components/ui/input';

const props = defineProps<{
  categories: any[]
}>();

const emit = defineEmits(['jumpToItem', 'searchChanged']);

// Search functionality
const searchQuery = ref('');
const searchResults = ref<Array<{
  categoryId: string
  categoryName: string
  item: any
}>>([]);
const isSearching = computed(() => searchQuery.value.trim().length > 0);

// Filter options
const showFilters = ref(false);
const filterExpiringItems = ref(false); // Filter for items expiring soon
const filterLowStock = ref(false); // Filter for items with low stock
const daysThreshold = ref(90); // Default: 90 days for expiring soon

// Apply all filters and search
function applyFiltersAndSearch(query: string) {
  if (query.trim().length === 0 && !filterExpiringItems.value && !filterLowStock.value) {
    searchResults.value = [];
    emit('searchChanged', false, []);
    return;
  }

  const results: Array<{
    categoryId: string
    categoryName: string
    item: any
  }> = [];

  // Current date and expiration threshold
  const today = new Date();
  const expirationThreshold = new Date();
  expirationThreshold.setDate(today.getDate() + daysThreshold.value);

  // Search through all categories and their items
  props.categories.forEach((category) => {
    category.items.forEach((item) => {
      let matches = true;

      // Apply text search if query exists
      if (query.trim().length > 0) {
        matches = item.name.toLowerCase().includes(query.toLowerCase()) ||
          (item.type && item.type.toLowerCase().includes(query.toLowerCase()));
      }

      // Apply expiration filter if enabled
      if (matches && filterExpiringItems.value && item.expiryDate) {
        const expiryDate = new Date(item.expiryDate);
        matches = expiryDate <= expirationThreshold;
      }

      // Apply low stock filter if enabled
      if (matches && filterLowStock.value) {
        // Determine what "low stock" means for this item
        // This is a simplified approach - you might want more sophisticated logic
        const isLowStock = item.amount <= 2; // Consider 2 or fewer as low stock
        matches = isLowStock;
      }

      if (matches) {
        results.push({
          categoryId: category.id,
          categoryName: category.name,
          item,
        });
      }
    });
  });

  searchResults.value = results;
  emit('searchChanged', results.length > 0, results);
}

// Watch for changes to search query or filters
watch([searchQuery, filterExpiringItems, filterLowStock, daysThreshold], () => {
  applyFiltersAndSearch(searchQuery.value);
});

function clearSearch() {
  searchQuery.value = '';
  filterExpiringItems.value = false;
  filterLowStock.value = false;
  searchResults.value = [];
  emit('searchChanged', false, []);
}

function handleJumpToItem(categoryId: string, itemId: string) {
  emit('jumpToItem', categoryId, itemId);
}

// Get category icon for search results
function getCategoryIcon(categoryId: string) {
  const category = props.categories.find(cat => cat.id === categoryId);
  return category ? category.icon : null;
}

// Format date in Norwegian format
function formatDate(dateString: string) {
  if (!dateString) return '';
  const date = new Date(dateString);
  return date.toLocaleDateString('nb-NO', { day: '2-digit', month: '2-digit', year: 'numeric' });
}

// Check if an item is expiring soon
function isExpiringSoon(dateString: string) {
  if (!dateString) return false;
  const expiryDate = new Date(dateString);
  const today = new Date();
  const expirationThreshold = new Date();
  expirationThreshold.setDate(today.getDate() + daysThreshold.value);
  return expiryDate <= expirationThreshold && expiryDate >= today;
}

// Check if an item is expired
function isExpired(dateString: string) {
  if (!dateString) return false;
  const expiryDate = new Date(dateString);
  const today = new Date();
  return expiryDate < today;
}
</script>

<template>
  <div class="bg-white rounded-lg shadow-sm border border-gray-200 px-4 py-3">
    <!-- Search bar with filters toggle -->
    <div class="flex items-center gap-2">
      <div class="relative flex-1">
        <div class="absolute inset-y-0 left-0 flex items-center pl-3 pointer-events-none">
          <Search class="h-4 w-4 text-gray-400" />
        </div>
        <Input
          v-model="searchQuery"
          type="text"
          placeholder="Søk etter produkter..."
          class="pl-10 w-full text-base"
        />
        <button
          v-if="searchQuery || filterExpiringItems || filterLowStock"
          @click="clearSearch"
          class="absolute inset-y-0 right-0 flex items-center pr-3 text-gray-400 hover:text-gray-600"
        >
          <span class="text-xl">&times;</span>
        </button>
      </div>

      <button
        @click="showFilters = !showFilters"
        class="p-2 rounded-md bg-gray-100 hover:bg-gray-200 flex items-center text-gray-700"
        :class="{ 'bg-blue-100 text-blue-700': filterExpiringItems || filterLowStock }"
      >
        <Filter class="h-5 w-5" />
        <span class="ml-1 text-base hidden sm:inline">Filter</span>
      </button>
    </div>

    <!-- Filter options -->
    <div v-if="showFilters" class="mt-3 p-3 bg-gray-50 rounded-md border border-gray-200">
      <div class="text-base font-medium text-gray-700 mb-2">Filtrer etter:</div>
      <div class="space-y-3">
        <label class="flex items-center cursor-pointer">
          <input
            type="checkbox"
            v-model="filterExpiringItems"
            class="rounded border-gray-300 text-blue-600 focus:ring-blue-500 h-4 w-4"
          />
          <span class="ml-2 text-base text-gray-600 flex items-center">
            Utløper snart
          </span>
        </label>

        <div v-if="filterExpiringItems" class="ml-6 mt-1">
          <div class="text-sm text-gray-500 mb-1">Vis produkter som utløper innen:</div>
          <div class="flex items-center gap-2">
            <input
              type="range"
              v-model="daysThreshold"
              min="2"
              max="60"
              step="1"
              class="w-40 h-2 bg-gray-200 rounded-lg appearance-none cursor-pointer"
            />
            <span class="text-sm text-gray-600">{{ daysThreshold }} dager</span>
          </div>
        </div>

        <label class="flex items-center cursor-pointer">
          <input
            type="checkbox"
            v-model="filterLowStock"
            class="rounded border-gray-300 text-blue-600 focus:ring-blue-500 h-4 w-4"
          />
          <span class="ml-2 text-base text-gray-600">Lav beholdning</span>
        </label>
      </div>
    </div>

    <!-- Search results -->
    <div v-if="searchResults.length > 0" class="mt-3">
      <div class="flex items-center justify-between mb-2 text-gray-700">
        <h3 class="text-base font-medium">{{ searchResults.length }} resultat{{ searchResults.length !== 1 ? 'er' : '' }}</h3>
        <button
          @click="clearSearch"
          class="text-sm text-blue-600 hover:text-blue-800 hover:underline"
        >
          Fjern filter
        </button>
      </div>
      <ul class="divide-y divide-gray-100">
        <li
          v-for="result in searchResults"
          :key="`${result.categoryId}-${result.item.id}`"
          class="py-3 cursor-pointer hover:bg-gray-50 flex justify-between items-center"
          @click="handleJumpToItem(result.categoryId, result.item.id)"
        >
          <div class="flex items-center">
            <div v-if="getCategoryIcon(result.categoryId)" class="flex-shrink-0 h-7 w-7 rounded-full flex items-center justify-center mr-2 bg-blue-100 text-blue-700">
              <component :is="getCategoryIcon(result.categoryId)" class="h-4 w-4" />
            </div>
            <div>
              <span class="font-medium text-base">{{ result.item.name }}</span>
              <div v-if="result.item.expiryDate" class="text-sm">
                <span
                  :class="{
                    'text-red-600': isExpired(result.item.expiryDate),
                    'text-amber-600': !isExpired(result.item.expiryDate) && isExpiringSoon(result.item.expiryDate),
                    'text-gray-500': !isExpired(result.item.expiryDate) && !isExpiringSoon(result.item.expiryDate)
                  }"
                >
                  Utløper: {{ formatDate(result.item.expiryDate) }}
                </span>
              </div>
            </div>
          </div>
          <div class="flex items-center space-x-2 text-gray-500">
            <span class="px-3 py-1 bg-gray-100 rounded-full text-sm">
              {{ result.item.amount }} {{ result.item.unit }}
            </span>
            <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="text-gray-400">
              <path d="m9 18 6-6-6-6"/>
            </svg>
          </div>
        </li>
      </ul>
    </div>

    <!-- No results message -->
    <div v-else-if="isSearching || filterExpiringItems || filterLowStock" class="text-center py-4 text-gray-500 mt-2">
      <p class="text-base">Ingen produkter funnet</p>
      <p v-if="searchQuery" class="text-sm text-gray-400">Søkeord: "{{ searchQuery }}"</p>
    </div>
  </div>
</template>
