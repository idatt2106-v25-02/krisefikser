<template>
  <div class="bg-gray-50 min-h-screen">
    <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      <!-- Page Header with background -->
      <div class="bg-white rounded-lg shadow-sm border border-gray-200 p-6 mb-6">
        <router-link
          to="/husstand"
          class="inline-flex items-center text-blue-600 hover:text-blue-800 mb-4"
        >
          ← Tilbake til husstand
        </router-link>
        <div>
          <h1 class="text-3xl font-bold text-gray-900 mb-1">Husstandens Refleksjoner</h1>
          <p class="text-gray-600 max-w-3xl">
            Her finner du alle refleksjonene som er delt med din husstand.
          </p>
        </div>
      </div>

      <!-- Loading state with improved animation -->
      <div v-if="isLoading" class="bg-white rounded-lg shadow-sm border border-gray-200 p-8 flex justify-center">
        <div class="flex flex-col items-center">
          <div class="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-500 mb-4"></div>
          <p class="text-gray-600">Laster refleksjoner...</p>
        </div>
      </div>

      <!-- Error state with better visual cues -->
      <div v-else-if="error" class="bg-white rounded-lg shadow-lg p-6">
        <div class="bg-red-50 border-l-4 border-red-500 p-4 rounded">
          <div class="flex">
            <div class="flex-shrink-0">
              <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 text-red-500" viewBox="0 0 20 20" fill="currentColor">
                <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.707 7.293a1 1 0 00-1.414 1.414L8.586 10l-1.293 1.293a1 1 0 101.414 1.414L10 11.414l1.293 1.293a1 1 0 001.414-1.414L11.414 10l1.293-1.293a1 1 0 00-1.414-1.414L10 8.586 8.707 7.293z" clip-rule="evenodd" />
              </svg>
            </div>
            <div class="ml-3">
              <p class="text-sm text-red-700">
                Kunne ikke laste refleksjoner: {{ errorMessage }}
              </p>
            </div>
          </div>
        </div>
      </div>

      <!-- Reflection cards -->
      <div v-else-if="householdReflections && householdReflections.length > 0" class="grid md:grid-cols-2 lg:grid-cols-3 gap-6">
        <div
          v-for="reflection in householdReflections"
          :key="reflection.id"
          class="bg-white rounded-lg shadow-md overflow-hidden flex flex-col relative border border-gray-200 transition-all duration-300 hover:shadow-lg min-h-[220px]"
        >
          <!-- Colored top bar based on visibility -->
          <div
            class="h-1 w-full bg-green-500"
          ></div>

          <!-- Decorative corner -->
          <div class="absolute top-0 right-0 w-16 h-16 rounded-bl-full -mt-1 -mr-1 overflow-hidden z-0 bg-green-50/70">
            <div class="absolute top-2.5 right-2.5">
              <Users class="h-6 w-6 text-green-600" />
            </div>
          </div>

          <div class="p-4 relative z-10 flex-grow flex flex-col">
            <h3 class="text-lg font-semibold mb-1 pr-10">{{ reflection.title }}</h3>
            <div class="flex flex-wrap gap-1.5 mb-3 text-xs">
              <span class="px-2 py-0.5 rounded-full bg-blue-100 text-blue-800 font-medium">
                {{ reflection.authorName }}
              </span>
              <span class="px-2 py-0.5 rounded-full bg-green-100 text-green-800 font-medium">
                {{ mapReflectionVisibility(reflection.visibility) }}
              </span>
              <span class="px-2 py-0.5 rounded-full bg-gray-100 text-gray-800 font-medium">
                {{ formatDate(reflection.updatedAt) }}
              </span>
            </div>

            <div v-if="reflection.eventId && eventTitles[reflection.eventId]" class="mb-2">
              <span class="inline-flex items-center px-2 py-0.5 rounded-full bg-purple-100 text-purple-800 text-xs">
                <router-link :to="`/kriser/${reflection.eventId}`" class="hover:underline">
                  {{ eventTitles[reflection.eventId] }}
                </router-link>
              </span>
            </div>
            <div v-else-if="reflection.eventId" class="mb-2 text-sm text-gray-500 italic">
              <span class="inline-flex items-center px-2 py-0.5 rounded-full bg-gray-100 text-gray-600 text-xs">
                Laster hendelsesnavn...
              </span>
            </div>

            <p v-if="reflection.content" class="text-sm text-gray-600 flex-grow mb-3 line-clamp-3" v-html="stripHtml(reflection.content)"></p>

            <div class="mt-auto pt-3 border-t flex justify-end space-x-2">
              <Button size="sm" variant="outline" @click="viewReflection(reflection.id!)" class="flex items-center">
                <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4 mr-1" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z" />
                </svg>
                Se detaljer
              </Button>
              <Button
                v-if="canManageReflection(reflection)"
                size="sm"
                variant="destructive"
                @click="confirmDeleteReflection(reflection.id!)"
                class="flex items-center"
              >
                <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4 mr-1" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
                </svg>
                Slett
              </Button>
            </div>
          </div>
        </div>
      </div>

      <!-- Empty state -->
      <div v-else-if="!isLoading && !error" class="bg-white rounded-lg shadow-md p-8 text-center">
        <Users class="mx-auto h-12 w-12 text-gray-400" />
        <h3 class="mt-2 text-xl font-medium text-gray-900">Ingen husstandsrefleksjoner</h3>
        <p class="mt-1 text-base text-gray-500">Det ser ikke ut til å være noen refleksjoner delt med denne husstanden ennå.</p>
        <Button class="mt-6" @click="navigateToMyReflections">
          <BookOpen class="h-4 w-4 mr-2" />
          Se mine refleksjoner
        </Button>
      </div>

      <!-- Delete Reflection Confirmation -->
      <ConfirmationDialog
        :is-open="showDeleteDialog"
        title="Slett refleksjon"
        description="Er du sikker på at du vil slette denne refleksjonen? Handlingen kan ikke angres."
        confirm-text="Slett"
        variant="destructive"
        @confirm="handleDeleteReflection"
        @cancel="showDeleteDialog = false"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue';
import { useRouter } from 'vue-router';
import { useQueryClient } from '@tanstack/vue-query';
import { useAuthStore } from '@/stores/auth/useAuthStore';
import {
  useGetHouseholdReflections,
  useDeleteReflection,
  getGetHouseholdReflectionsQueryKey
} from '@/api/generated/reflection/reflection';
import { useGetEventById } from '@/api/generated/event/event';
import type { ReflectionResponse } from '@/api/generated/model';
import { ReflectionResponseVisibility } from '@/api/generated/model';
import { Button } from '@/components/ui/button';
import { BookOpen, Users } from 'lucide-vue-next';
import { ConfirmationDialog } from '@/components/ui/confirmation-dialog';

const router = useRouter();
const queryClient = useQueryClient();
const authStore = useAuthStore();

const { data: householdReflections, isLoading, error } = useGetHouseholdReflections<ReflectionResponse[]>({
  query: {
    enabled: authStore.isAuthenticated,
    refetchOnWindowFocus: true,
  }
});

const deleteReflectionMutation = useDeleteReflection({
  mutation: {
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: getGetHouseholdReflectionsQueryKey() });
    },
    onError: (err: Error | unknown) => {
      console.error("Feil ved sletting av refleksjon:", err);
      alert("Kunne ikke slette refleksjon: " + (err instanceof Error ? err.message : 'Ukjent feil'));
    }
  }
});

const errorMessage = computed(() => {
  const e = error.value as Error | null;
  if (e instanceof Error) return e.message;
  return e ? 'En ukjent feil oppstod.' : '';
});

const formatDate = (dateInput?: string | number[] | null) => {
  if (!dateInput) return 'Ukjent dato';

  // If it's an array, convert to Date
  if (Array.isArray(dateInput)) {
    // Java months are 1-based, JS months are 0-based
    const [year, month, day, hour, minute, second, nanosecond] = dateInput;
    const ms = Math.floor((nanosecond || 0) / 1e6); // Convert nanoseconds to milliseconds
    const date = new Date(year, month - 1, day, hour, minute, second, ms);
    if (isNaN(date.getTime())) return 'Ukjent dato';
    return date.toLocaleDateString('nb-NO', {
      year: 'numeric',
      month: 'long',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    });
  }

  // If it's a string, try to parse as date
  const date = new Date(dateInput);
  if (isNaN(date.getTime())) return 'Ukjent dato';
  return date.toLocaleDateString('nb-NO', {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  });
};

const mapReflectionVisibility = (visibility?: ReflectionResponseVisibility): string => {
  if (!visibility) return 'Ukjent';
  switch (visibility) {
    case ReflectionResponseVisibility.PUBLIC: return 'Offentlig';
    case ReflectionResponseVisibility.HOUSEHOLD: return 'Husstand';
    case ReflectionResponseVisibility.PRIVATE: return 'Privat';
    default: return 'Ukjent';
  }
};

const stripHtml = (html?: string) => {
  if (!html) return '';
  const doc = new DOMParser().parseFromString(html, 'text/html');
  return doc.body.textContent || "";
};

const canManageReflection = (reflection: ReflectionResponse) => {
  if (!authStore.currentUser) return false;
  return authStore.isAdmin || authStore.currentUser.id === reflection.authorId;
};

const viewReflection = (id: string) => {
  router.push(`/refleksjon/${id}`);
};

const showDeleteDialog = ref(false)
const reflectionToDelete = ref<string | null>(null)

const confirmDeleteReflection = (id: string) => {
  reflectionToDelete.value = id
  showDeleteDialog.value = true
}

const handleDeleteReflection = async () => {
  if (!reflectionToDelete.value) return
  try {
    await deleteReflectionMutation.mutateAsync({ id: reflectionToDelete.value })
    showDeleteDialog.value = false
    reflectionToDelete.value = null
  } catch (error) {
    console.error('Error deleting reflection:', error)
  }
}

const navigateToMyReflections = () => {
  router.push('/mine-refleksjoner');
};

const eventTitles = ref<Record<string, string>>({});
const eventIdsToFetch = computed(() => {
  const ids = new Set<number>();
  (householdReflections.value || []).forEach((reflection: ReflectionResponse) => {
    if (reflection.eventId) {
      ids.add(reflection.eventId);
    }
  });
  return Array.from(ids);
});

watch(eventIdsToFetch, (newIds) => {
  newIds.forEach(id => {
    if (!eventTitles.value[id]) {
      const eventIdRef = ref(id);
      const { data: eventData, error: eventError } = useGetEventById(eventIdRef, {
        query: {
          enabled: computed(() => !!eventIdRef.value),
        }
      });

      watch(eventData, (newEventData) => {
        if (newEventData?.title) {
          eventTitles.value = {
            ...eventTitles.value,
            [id]: newEventData.title,
          };
        }
      }, { immediate: true });

      watch(eventError, (err) => {
        if (err) {
          console.error(`Kunne ikke hente hendelsesnavn for ID ${id}:`, err);
          eventTitles.value = { ...eventTitles.value, [id]: 'Hendelse utilgjengelig' };
        }
      });
    }
  });
}, { deep: true, immediate: true });

onMounted(() => {
  if (!authStore.isAuthenticated) {
    router.push('/logg-inn');
  }
});

</script>

<style scoped>
.line-clamp-3 {
  overflow: hidden;
  display: -webkit-box;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 3;
}
</style>
