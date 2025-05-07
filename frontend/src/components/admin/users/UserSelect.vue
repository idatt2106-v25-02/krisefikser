<!-- UserSelect.vue -->
<script setup lang="ts">
import { ref, computed } from 'vue';
import { Search } from 'lucide-vue-next';
import { useGetAllUsers } from '@/api/generated/user/user.ts';
import type { UserResponse } from '@/api/generated/model';

// Import shadcn components
import {
  Command,
  CommandEmpty,
  CommandGroup,
  CommandInput,
  CommandItem,
  CommandList,
} from '@/components/ui/command';
import {
  Popover,
  PopoverContent,
  PopoverTrigger,
} from '@/components/ui/popover';
import { Button } from '@/components/ui/button';

const props = defineProps<{
  modelValue: string;
}>();

const emit = defineEmits<{
  (e: 'update:modelValue', value: string): void;
}>();

const { data: usersData } = useGetAllUsers<UserResponse[]>();
const searchQuery = ref('');
const open = ref(false);

const filteredUsers = computed(() => {
  if (!usersData.value) return [];
  let result = [...usersData.value];

  if (searchQuery.value) {
    const query = searchQuery.value.toLowerCase();
    result = result.filter(user =>
      (user.firstName?.toLowerCase().includes(query) || false) ||
      (user.lastName?.toLowerCase().includes(query) || false) ||
      (user.email?.toLowerCase().includes(query) || false)
    );
  }

  return result;
});

const selectedUser = computed(() => {
  if (!props.modelValue || !usersData.value) return null;
  return usersData.value.find(user => user.id === props.modelValue) || null;
});

const handleSelect = (userId: string) => {
  emit('update:modelValue', userId);
  open.value = false;
};
</script>

<template>
  <Popover v-model:open="open">
    <PopoverTrigger as-child>
      <Button
        variant="outline"
        role="combobox"
        :aria-expanded="open"
        class="w-full justify-between"
      >
        <span v-if="selectedUser">
          {{ selectedUser.firstName }} {{ selectedUser.lastName }}
          <span class="text-gray-500 ml-2">({{ selectedUser.email }})</span>
        </span>
        <span v-else class="text-gray-500">Velg en bruker...</span>
      </Button>
    </PopoverTrigger>
    <PopoverContent class="w-full p-0">
      <Command>
        <div class="flex items-center border-b px-3">
          <Search class="mr-2 h-4 w-4 shrink-0 opacity-50" />
          <CommandInput
            v-model="searchQuery"
            placeholder="Søk etter bruker..."
            class="flex h-11 w-full rounded-md bg-transparent py-3 text-sm outline-none placeholder:text-muted-foreground disabled:cursor-not-allowed disabled:opacity-50"
          />
        </div>
        <CommandList>
          <CommandEmpty>Ingen brukere funnet.</CommandEmpty>
          <CommandGroup>
            <CommandItem
              v-for="user in filteredUsers"
              :key="user.id"
              :value="user.id || ''"
              @select="handleSelect(user.id || '')"
            >
              <div class="flex flex-col">
                <span>{{ user.firstName }} {{ user.lastName }}</span>
                <span class="text-sm text-gray-500">{{ user.email }}</span>
              </div>
            </CommandItem>
          </CommandGroup>
        </CommandList>
      </Command>
    </PopoverContent>
  </Popover>
</template>
