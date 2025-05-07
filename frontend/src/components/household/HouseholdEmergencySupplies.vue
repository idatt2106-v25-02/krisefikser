<script setup lang="ts">
import { defineProps, defineEmits } from 'vue'
import { useRouter } from 'vue-router'

interface InventoryItem {
  name: string
  expiryDate: string
  amount: number
  productType: {
    name: string
    unit: string
  }
}

interface Inventory {
  food: { current: number; target: number; unit: string }
  water: { current: number; target: number; unit: string }
  other: { current: number; target: number }
  preparedDays: number
  targetDays: number
}

interface Props {
  inventory: Inventory
  inventoryItems?: InventoryItem[]
  householdId?: string
  showDetailsButton?: boolean
}

defineProps<Props>()
const router = useRouter()

const emit = defineEmits<{
  (e: 'open-info-dialog'): void
}>()

function navigateToInventory() {
  router.push('/husstand/beredskapslager')
}
</script>

<template>
  <div class="mb-12">
    <!-- Summary boxes -->
    <div class="bg-white border border-gray-200 rounded-lg p-6 mb-6">
      <div class="grid grid-cols-1 sm:grid-cols-3 gap-4 mb-8 text-center sm:text-left">
        <div>
          <div class="text-sm text-gray-500 mb-1">Mat</div>
          <div class="text-lg text-blue-600 font-semibold break-all">
            {{ inventory.food.current }}/{{ inventory.food.target }} {{ inventory.food.unit }}
          </div>
        </div>
        <div>
          <div class="text-sm text-gray-500 mb-1">Vann</div>
          <div class="text-lg text-blue-600 font-semibold break-all">
            {{ inventory.water.current }}/{{ inventory.water.target }} {{ inventory.water.unit }}
          </div>
        </div>
        <div>
          <div class="text-sm text-gray-500 mb-1">Annet</div>
          <div class="text-lg text-blue-600 font-semibold">
            {{ inventory.other.current }}/{{ inventory.other.target }}
          </div>
        </div>
      </div>

      <!-- Days prepared -->
      <div
        class="mb-4 bg-blue-50 p-4 rounded-lg border border-blue-100 cursor-pointer hover:bg-blue-100 transition-colors duration-150"
        @click="emit('open-info-dialog')"
        role="button"
        tabindex="0"
        aria-label="Vis informasjon om beredskapsberegning"
      >
        <div class="flex justify-between items-center mb-3">
          <span class="text-base font-medium text-blue-800">Dager forberedt</span>
          <div class="flex items-center">
            <span
              :class="[
                'text-3xl font-bold',
                inventory.preparedDays <= 3 ? 'text-red-600' : 'text-blue-700',
              ]"
              >{{ inventory.preparedDays }}</span
            >
            <span class="text-lg text-blue-600 ml-1">/{{ inventory.targetDays }}</span>
          </div>
        </div>
        <div class="h-4 bg-blue-200 rounded-full overflow-hidden mb-2">
          <div
            class="h-full bg-blue-600 rounded-full"
            :style="`width: ${(inventory.preparedDays / inventory.targetDays) * 100}%`"
          ></div>
        </div>
        <div class="text-sm text-blue-700">
          Norske myndigheter anbefaler at du har nok forsyninger tilregnet
          {{ inventory.targetDays }} dager.
        </div>
      </div>

      <button
        v-if="showDetailsButton"
        @click="navigateToInventory"
        class="w-full mt-4 bg-blue-600 hover:bg-blue-700 text-white py-2 px-4 rounded-md transition-colors duration-200 font-medium flex items-center justify-center"
      >
        Vis detaljer
      </button>
    </div>
  </div>
</template>
