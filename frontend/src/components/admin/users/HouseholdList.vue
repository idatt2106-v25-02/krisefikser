<!-- HouseholdList.vue -->
<script setup lang="ts">
import { ref, computed } from 'vue';
import { ChevronDown, ChevronRight, User } from 'lucide-vue-next';
import type { HouseholdResponse, UserResponse } from '@/api/generated/model';

const props = defineProps<{
  households: HouseholdResponse[];
}>();

const expanded = ref<Record<string, boolean>>({});

const groupedHouseholds = computed(() => {
  return props.households.map(household => ({
    ...household,
    members: household.members || [],
    users: household.members?.map(member => member.user).filter((user): user is UserResponse => user !== undefined) || []
  }));
});

const toggleExpand = (id: string) => {
  expanded.value[id] = !expanded.value[id];
};
</script>

<template>
  <div class="overflow-x-auto">
    <table class="min-w-full divide-y divide-gray-200 bg-white rounded-lg shadow">
      <thead class="bg-gray-50">
        <tr>
          <th class="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider w-10"></th>
          <th class="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Husstand</th>
          <th class="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Adresse</th>
        </tr>
      </thead>
      <tbody class="bg-white divide-y divide-gray-200">
        <template v-for="household in groupedHouseholds" :key="household.id">
          <tr
            class="hover:bg-blue-50 transition-colors cursor-pointer"
            @click="toggleExpand(household.id)"
            :aria-expanded="expanded[household.id] ? 'true' : 'false'"
            tabindex="0"
            @keydown.enter.space="toggleExpand(household.id)"
          >
            <td class="px-4 py-2 align-top">
              <component :is="expanded[household.id] ? ChevronDown : ChevronRight" class="h-4 w-4 text-blue-500" />
            </td>
            <td class="px-4 py-2 font-semibold text-gray-800 align-top">{{ household.name }}</td>
            <td class="px-4 py-2 text-gray-600 align-top">{{ household.address }}</td>
          </tr>
          <tr v-if="expanded[household.id]">
            <td></td>
            <td colspan="2" class="px-3 pb-2 pt-0">
              <div class="bg-blue-50 rounded-md p-2">
                <div class="font-bold text-sm text-blue-800 mb-1">Medlemmer</div>
                <div v-if="household.members.length === 0" class="text-xs text-gray-400 italic">Ingen medlemmer</div>
                <div v-for="member in household.members" :key="member.user?.id" class="flex items-center gap-2 py-1 px-2 mb-1 bg-white border-l-4 border-blue-300 shadow-sm rounded-md hover:bg-blue-100 transition-colors">
                  <div class="bg-blue-100 p-1 rounded-full">
                    <User class="h-3 w-3 text-blue-600" />
                  </div>
                  <div class="flex flex-col">
                    <span class="text-sm font-semibold text-gray-800">{{ member.user?.firstName }} {{ member.user?.lastName }}</span>
                    <span class="text-xs text-blue-700">{{ member.user?.email }}</span>
                  </div>
                </div>
              </div>
            </td>
          </tr>
        </template>
      </tbody>
    </table>
  </div>
</template>
