<script lang="ts" setup>
interface Column {
  key: string
  label: string
  align?: 'left' | 'center' | 'right'
}

interface Props {
  columns: Column[]
  isLoading?: boolean
  emptyMessage?: string
}

defineProps<Props>()
</script>

<template>
  <div class="relative">
    <!-- Loading state -->
    <div v-if="isLoading" class="p-8 text-center">
      <div class="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-500 mx-auto"></div>
      <p class="mt-2 text-gray-500">Laster data...</p>
    </div>

    <!-- Data table -->
    <div v-else class="relative">
      <div class="overflow-x-auto max-h-[calc(100vh-300px)] sm:max-h-[calc(100vh-250px)] overflow-y-auto scrollbar-thin scrollbar-track-gray-200 scrollbar-thumb-gray-400 hover:scrollbar-thumb-gray-500 scrollbar-track-rounded-md scrollbar-thumb-rounded-md">
        <div class="min-w-full inline-block align-middle">
          <div class="overflow-hidden">
            <table class="min-w-full divide-y divide-gray-200">
              <thead class="bg-gray-50 sticky top-0 z-10">
                <tr>
                  <th
                    v-for="column in columns"
                    :key="column.key"
                    class="px-4 py-3 text-xs font-medium text-gray-500 uppercase tracking-wider whitespace-nowrap"
                    :class="{
                      'text-left': column.align === 'left' || !column.align,
                      'text-center': column.align === 'center',
                      'text-right': column.align === 'right'
                    }"
                    scope="col"
                  >
                    {{ column.label }}
                  </th>
                </tr>
              </thead>
              <tbody class="bg-white divide-y divide-gray-200">
                <slot />
                <!-- Empty state -->
                <tr v-if="!$slots.default">
                  <td
                    :colspan="columns.length"
                    class="px-4 py-8 text-center text-gray-500 whitespace-nowrap"
                  >
                    {{ emptyMessage || 'Ingen data funnet' }}
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
