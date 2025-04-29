<script setup lang="ts">
import { useRouter } from 'vue-router';
import { Home, MapPin, Users, ShoppingBag, CheckCircle } from 'lucide-vue-next';
import { useGetAllHouseholds, useGetActiveHousehold } from '@/api/generated/household/household';
import { useAuthStore } from '@/stores/useAuthStore';

const router = useRouter();
const authStore = useAuthStore();

// Get household data from API
const { data: households, isLoading: isLoadingHouseholds } = useGetAllHouseholds({
  query: {
    enabled: authStore.isAuthenticated,
    refetchOnMount: true,
    refetchOnWindowFocus: true
  }
});

const { data: activeHousehold, isLoading: isLoadingActiveHousehold } = useGetActiveHousehold({
  query: {
    enabled: authStore.isAuthenticated,
    refetchOnMount: true,
    refetchOnWindowFocus: true
  }
});

const navigateToHousehold = (id: string | undefined) => {

  if (id === activeHousehold.value?.id) {
    router.push(`/husstand/${id}`);
  } else {
    // Option: Show a modal or alert explaining they can't access non-active households
    console.log('Du kan kun se detaljer for din aktive husstand.');
  }
};

// Check if a household is the active one
const isActiveHousehold = (id: string | undefined) => {
  return id === activeHousehold.value?.id;
};
</script>

<template>
  <div class="bg-gray-50 min-h-screen">
    <div class="container mx-auto px-4 py-10">
      <div class="flex items-center mb-8">
        <Home class="h-8 w-8 text-blue-600 mr-3" />
        <h1 class="text-3xl font-bold text-gray-800">Mine husstander</h1>
      </div>

      <div v-if="isLoadingHouseholds || isLoadingActiveHousehold" class="flex justify-center items-center h-64">
        <div class="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-500"></div>
      </div>

      <div v-else-if="households && households.length > 0" class="grid grid-cols-1 md:grid-cols-2 gap-6">
        <div
          v-for="household in households"
          :key="household.id"
          :class="[
            'bg-white rounded-xl overflow-hidden shadow-md transition-all duration-300',
            isActiveHousehold(household.id) ? 'ring-2 ring-blue-500' : 'hover:shadow-lg opacity-80'
          ]"
        >
          <!-- Colored top banner -->
          <div
            :class="[
              'h-3',
              isActiveHousehold(household.id) ? 'bg-gradient-to-r from-blue-500 to-blue-600' : 'bg-gradient-to-r from-gray-300 to-gray-400'
            ]"
          ></div>

          <!-- Card content -->
          <div class="p-6">
            <div class="flex justify-between items-start mb-4">
              <div class="flex items-center">
                <h2
                  :class="[
                    'text-xl font-bold',
                    isActiveHousehold(household.id) ? 'text-gray-800' : 'text-gray-600'
                  ]"
                >
                  {{ household.name }}
                </h2>
                <CheckCircle
                  v-if="isActiveHousehold(household.id)"
                  class="h-5 w-5 text-blue-600 ml-2"
                  data-tooltip="Aktiv husstand"
                />
              </div>
              <div
                :class="[
                  'text-xs font-medium px-2.5 py-1 rounded',
                  isActiveHousehold(household.id) ? 'bg-blue-100 text-blue-800' : 'bg-gray-100 text-gray-700'
                ]"
              >
                {{ household.members?.length || 0 }} medlemmer
              </div>
            </div>

            <div class="space-y-4">
              <!-- Address -->
              <div class="flex items-start text-gray-600">
                <MapPin class="h-5 w-5 text-gray-400 mr-2 flex-shrink-0 mt-0.5" />
                <span>{{ household.address }}</span>
              </div>

              <!-- Members -->
              <div class="flex items-start">
                <Users class="h-5 w-5 text-gray-400 mr-2 flex-shrink-0 mt-0.5" />
                <div class="flex flex-wrap gap-1">
                  <span
                    v-for="(member, index) in (household.members || []).slice(0, 2)"
                    :key="member.user?.id"
                    class="text-gray-700"
                  >
                    {{ member.user?.firstName }}{{ index < Math.min((household.members || []).length, 2) - 1 ? ',' : '' }}
                  </span>
                  <span v-if="(household.members || []).length > 2" class="text-gray-500">
                    og {{ (household.members || []).length - 2 }} flere
                  </span>
                </div>
              </div>

              <!-- Action buttons -->
              <div class="mt-6 flex gap-3">
                <!-- Active household: See details button -->
                <button
                  v-if="isActiveHousehold(household.id)"
                  @click="navigateToHousehold(household.id)"
                  class="flex-1 bg-blue-600 hover:bg-blue-700 text-white py-2.5 px-4 rounded-lg transition-colors duration-200 font-medium flex items-center justify-center"
                >
                  Se detaljer
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Empty state -->
      <div v-else class="text-center py-16 bg-white rounded-xl shadow mt-6">
        <Home class="h-12 w-12 text-gray-300 mx-auto mb-4" />
        <h3 class="text-xl font-medium text-gray-800 mb-2">Ingen husstander funnet</h3>
        <p class="text-gray-600 mb-6">Du har ikke opprettet eller blitt lagt til i noen husstander ennå.</p>
        <router-link
          to="/husstand/opprett"
          class="bg-blue-600 hover:bg-blue-700 text-white py-2 px-6 rounded-lg transition-colors duration-200 font-medium"
        >
          Opprett husstand
        </router-link>
      </div>
    </div>
  </div>
</template>
