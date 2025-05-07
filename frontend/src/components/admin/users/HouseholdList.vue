<!-- HouseholdList.vue -->
<script setup lang="ts">
import { computed } from 'vue';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { User } from 'lucide-vue-next';
import type { HouseholdResponse, UserResponse } from '@/api/generated/model';

const props = defineProps<{
  households: HouseholdResponse[];
}>();

const groupedHouseholds = computed(() => {
  return props.households.map(household => ({
    ...household,
    members: household.members || [],
    users: household.members?.map(member => member.user).filter((user): user is UserResponse => user !== undefined) || []
  }));
});
</script>

<template>
  <div class="space-y-4 py-4 bg-transparent">
    <Card v-for="household in groupedHouseholds" :key="household.id">
      <CardHeader>
        <div>
          <CardTitle class="text-lg">{{ household.name }}</CardTitle>
          <p class="text-muted-foreground text-sm">{{ household.address }}</p>
        </div>
      </CardHeader>
      <CardContent>
        <div class="space-y-3">
          <h4 class="font-medium text-sm">Medlemmer</h4>
          <div v-for="member in household.members" :key="member.user?.id" class="flex items-center p-2 bg-muted/50 rounded-md">
            <div class="flex items-center gap-2">
              <User class="h-4 w-4 text-muted-foreground" />
              <span>{{ member.user?.firstName }} {{ member.user?.lastName }}</span>
              <span class="text-muted-foreground text-sm">({{ member.user?.email }})</span>
            </div>
          </div>
        </div>
      </CardContent>
    </Card>
  </div>
</template>
