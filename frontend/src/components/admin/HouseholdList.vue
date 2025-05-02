<!-- HouseholdList.vue -->
<script setup lang="ts">
import { computed } from 'vue';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { User, UserCog, UserPlus, Trash2 } from 'lucide-vue-next';
import type { HouseholdResponse, UserResponse } from '@/api/generated/model';

const props = defineProps<{
  households: HouseholdResponse[];
}>();

const emit = defineEmits<{
  (e: 'edit', household: HouseholdResponse): void;
  (e: 'addMember', household: HouseholdResponse): void;
  (e: 'delete', id: string): void;
  (e: 'removeMember', data: { householdId: string; userId: string }): void;
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
      <CardHeader class="flex flex-row items-center justify-between">
        <div>
          <CardTitle class="text-lg">{{ household.name }}</CardTitle>
          <p class="text-muted-foreground text-sm">{{ household.address }}</p>
        </div>
        <div class="flex gap-2">
          <Button variant="outline" size="sm" @click="emit('edit', household)">
            <UserCog class="h-4 w-4 mr-2" />
            Rediger
          </Button>
          <Button variant="outline" size="sm" @click="emit('addMember', household)">
            <UserPlus class="h-4 w-4 mr-2" />
            Legg til medlem
          </Button>
          <Button variant="destructive" size="sm" @click="emit('delete', household.id)">
            <Trash2 class="h-4 w-4 mr-2" />
            Slett
          </Button>
        </div>
      </CardHeader>
      <CardContent>
        <div class="space-y-3">
          <h4 class="font-medium text-sm">Medlemmer</h4>
          <div v-for="member in household.members" :key="member.user?.id" class="flex items-center justify-between p-2 bg-muted/50 rounded-md">
            <div class="flex items-center gap-2">
              <User class="h-4 w-4 text-muted-foreground" />
              <span>{{ member.user?.firstName }} {{ member.user?.lastName }}</span>
              <span class="text-muted-foreground text-sm">({{ member.user?.email }})</span>
            </div>
            <Button
              v-if="member.user?.id !== household.owner?.id"
              variant="ghost"
              size="sm"
              @click="emit('removeMember', { householdId: household.id, userId: member.user?.id || '' })"
            >
              <Trash2 class="h-4 w-4 text-destructive" />
            </Button>
          </div>
        </div>
      </CardContent>
    </Card>
  </div>
</template>
