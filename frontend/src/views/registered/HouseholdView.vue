<script setup lang="ts">
import { useRouter } from 'vue-router';

const router = useRouter();

// Mock API response
const apiResponse = {
  households: [
    {
      id: '1',
      name: 'Familien Hansen',
      address: 'Kongens gate 1, 0153 Oslo',
      members: [
        { id: '1', name: 'Erik Hansen' },
        { id: '2', name: 'Maria Hansen' },
        { id: '3', name: 'Lars Hansen' }
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
  router.push(`/husstand/${id}`);
};
</script>

<template>
  <div class="container mx-auto px-4 py-8">
    <h1 class="text-3xl font-bold text-gray-900 mb-8">Mine husstander</h1>
    <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
      <div
        v-for="household in apiResponse.households"
        :key="household.id"
        @click="navigateToHousehold(household.id)"
        class="bg-white rounded-lg shadow-sm border border-gray-200 p-6 hover:shadow-md transition-shadow duration-200 cursor-pointer hover:-translate-y-1 transform transition-transform"
      >
        <h2 class="text-xl font-semibold text-gray-800 mb-2">{{ household.name }}</h2>
        <p class="text-gray-600 mb-2">{{ household.address }}</p>
        <p class="text-gray-500">{{ household.members.length }} medlemmer</p>
      </div>
    </div>
  </div>
</template>
