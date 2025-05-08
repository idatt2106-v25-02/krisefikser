<script setup lang="ts">
import { computed } from 'vue'
import { Button } from '@/components/ui/button'
import { Info } from 'lucide-vue-next'
import HouseholdEmergencySupplies from '@/components/household/HouseholdEmergencySupplies.vue'
import { useGetInventorySummary } from '@/api/generated/item/item'
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

const props = defineProps<{
  householdId: string
  inventoryItems: InventoryItem[]
}>()

const emit = defineEmits<{
  (e: 'showInfo'): void
}>()

const router = useRouter()

// Fetch Inventory Summary
const { data: inventorySummary } = useGetInventorySummary({
  query: {
    enabled: computed(() => !!props.householdId),
  },
})

// Computed property to format inventory data
const inventoryPreviewData = computed(() => {
  const DAYS_GOAL = 7

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

  const dailyKcalNeeded = summary.kcalGoal > 0 ? summary.kcalGoal / DAYS_GOAL : 0
  const effectiveFoodDays =
    summary.kcalGoal === 0 && summary.kcal > 0
      ? DAYS_GOAL
      : dailyKcalNeeded > 0
        ? summary.kcal / dailyKcalNeeded
        : 0

  const dailyWaterNeeded = summary.waterLitersGoal > 0 ? summary.waterLitersGoal / DAYS_GOAL : 0
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
    derivedPreparedDays = summary.kcal > 0 ? Math.floor(effectiveWaterDays) : 0
  } else if (summary.waterLitersGoal === 0) {
    derivedPreparedDays = summary.waterLiters > 0 ? Math.floor(effectiveFoodDays) : 0
  } else {
    derivedPreparedDays = Math.floor(Math.min(effectiveFoodDays, effectiveWaterDays))
  }

  let finalPreparedDays = Math.min(derivedPreparedDays, DAYS_GOAL)
  if (finalPreparedDays < 0) finalPreparedDays = 0

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

function navigateToInventory() {
  router.push('/husstand/beredskapslager')
}
</script>

<template>
  <div class="bg-white rounded-lg shadow-sm border border-gray-200 p-6">
    <div class="flex justify-between items-center mb-5">
      <h2 class="text-xl font-semibold text-gray-800">Beredskapslager</h2>
      <div class="flex items-center space-x-2">
        <button
          @click="$emit('showInfo')"
          class="text-blue-600 hover:text-blue-700 p-1.5 rounded-full hover:bg-blue-50 transition-colors"
          title="Vis informasjon om beredskapsberegning"
        >
          <Info class="h-5 w-5" />
        </button>
        <Button variant="outline" size="sm" @click="navigateToInventory" class="flex items-center gap-1">
          <span>Se detaljer</span>
        </Button>
      </div>
    </div>

    <HouseholdEmergencySupplies
      :inventory="inventoryPreviewData"
      :inventory-items="inventoryItems"
      :show-details-button="false"
      @open-info-dialog="$emit('showInfo')"
    />
  </div>
</template>
