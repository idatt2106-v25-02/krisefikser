<script setup lang="ts">
import { useRouter } from 'vue-router';
import { Home, MapPin, Users, ShoppingBag, CheckCircle } from 'lucide-vue-next';

const router = useRouter();

// Mock API response with active household indicator
const apiResponse = {
  activeHouseholdId: '1', // This indicates which household is active for the current user
  households: [
    {
      id: '1',
      name: 'Familien Hansen',
      address: 'Kongens gate 1, 0153 Oslo',
      members: [
        { id: '1', name: 'Erik Hansen' },
        { id: '2', name: 'Maria Hansen' },
        { id: '3', name: 'Lars Hansen' },
        { id: '4', name: 'Mona Hansen' }
      ],
      inventory: [
        {
          name: 'Melk',
          expiryDate: '2024-04-10',
          amount: 2,
          productType: {
            name: 'Meieriprodukter',
            unit: 'liter'
          }
        },
        {
          name: 'Brød',
          expiryDate: '2024-04-05',
          amount: 1,
          productType: {
            name: 'Bakevarer',
            unit: 'stk'
          }
        }
      ]
    },
    {
      id: '2',
      name: 'Kollektiv Grünerløkka',
      address: 'Thorvald Meyers gate 15, 0555 Oslo',
      members: [
        { id: '4', name: 'Sofia Berg' },
        { id: '5', name: 'Anders Nilsen' },
        { id: '6', name: 'Emma Larsen' }
      ],
      inventory: [
        {
          name: 'Pasta',
          expiryDate: '2025-01-15',
          amount: 3,
          productType: {
            name: 'Tørrvarer',
            unit: 'pakke'
          }
        },
        {
          name: 'Tomatsaus',
          expiryDate: '2024-08-20',
          amount: 4,
          productType: {
            name: 'Hermetikk',
            unit: 'boks'
          }
        }
      ]
    }
  ]
};

const navigateToHousehold = (id: string) => {
  if (id === apiResponse.activeHouseholdId) {
    router.push(`/husstand/${id}`);
  } else {
    // Option: Show a modal or alert explaining they can't access non-active households
    // For now, we'll just log a message
    console.log('Du kan kun se detaljer for din aktive husstand.');
  }
};

// Function to switch active household
const switchActiveHousehold = (id: string) => {
  // In a real app, this would make an API call to update the user's active household
  console.log(`Byttet aktiv husstand til ${id}`);
  // For demo purposes, we could add a simulated API call here
  // apiResponse.activeHouseholdId = id;
};

// Get the earliest expiring product for each household
const getEarliestExpiringProduct = (inventory: any[]) => {
  if (!inventory.length) return null;

  return inventory.reduce((earliest, current) => {
    if (!earliest) return current;
    return new Date(current.expiryDate) < new Date(earliest.expiryDate) ? current : earliest;
  }, null);
};

// Check if a household is the active one
const isActiveHousehold = (id: string) => {
  return id === apiResponse.activeHouseholdId;
};
</script>

<template>
  <div class="bg-gray-50 min-h-screen">
    <div class="container mx-auto px-4 py-10">
      <div class="flex items-center mb-8">
        <Home class="h-8 w-8 text-blue-600 mr-3" />
        <h1 class="text-3xl font-bold text-gray-800">Mine husstander</h1>
      </div>

      <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
        <div
          v-for="household in apiResponse.households"
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
                {{ household.members.length }} medlemmer
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
                    v-for="(member, index) in household.members.slice(0, 2)"
                    :key="member.id"
                    class="text-gray-700"
                  >
                    {{ member.name }}{{ index < Math.min(household.members.length, 2) - 1 ? ',' : '' }}
                  </span>
                  <span v-if="household.members.length > 2" class="text-gray-500">
                    og {{ household.members.length - 2 }} flere
                  </span>
                </div>
              </div>

              <!-- Inventory summary -->
              <div class="flex items-start">
                <ShoppingBag class="h-5 w-5 text-gray-400 mr-2 flex-shrink-0 mt-0.5" />
                <div>
                  <span class="text-gray-700">{{ household.inventory.length }} varer i beholdning</span>
                  <div v-if="getEarliestExpiringProduct(household.inventory)" class="mt-1 text-sm">
                    <span
                      :class="[
                        'font-medium',
                        isActiveHousehold(household.id) ? 'text-amber-600' : 'text-amber-500'
                      ]"
                    >
                      {{ getEarliestExpiringProduct(household.inventory)?.name }} utløper
                      {{ new Date(getEarliestExpiringProduct(household.inventory)?.expiryDate).toLocaleDateString('no-NO') }}
                    </span>
                  </div>
                </div>
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

              <!-- Inactive household: Switch to button -->
              <button
                v-else
                @click="switchActiveHousehold(household.id)"
                class="flex-1 bg-gray-200 hover:bg-gray-300 text-gray-700 py-2.5 px-4 rounded-lg transition-colors duration-200 font-medium flex items-center justify-center"
              >
                Bytt husstand på profilsiden
              </button>
            </div>
          </div>
        </div>
      </div>

      <!-- Empty state -->
      <div v-if="apiResponse.households.length === 0" class="text-center py-16 bg-white rounded-xl shadow mt-6">
        <Home class="h-12 w-12 text-gray-300 mx-auto mb-4" />
        <h3 class="text-xl font-medium text-gray-800 mb-2">Ingen husstander funnet</h3>
        <p class="text-gray-600 mb-6">Du har ikke opprettet eller blitt lagt til i noen husstander ennå.</p>
        <button class="bg-blue-600 hover:bg-blue-700 text-white py-2 px-6 rounded-lg transition-colors duration-200 font-medium">
          Opprett husstand
        </button>
      </div>
    </div>
  </div>
</template>
