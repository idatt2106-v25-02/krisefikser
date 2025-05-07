<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue';
import { useRouter } from 'vue-router';
import { useQueryClient } from '@tanstack/vue-query';
import { useAuthStore } from '@/stores/auth/useAuthStore';
import {
  useGetCurrentUserReflections,
  useDeleteReflection,
  getGetCurrentUserReflectionsQueryKey,
} from '@/api/generated/reflection/reflection';
import { useGetEventById, useGetAllEvents } from '@/api/generated/event/event';
import type { ReflectionResponse, EventResponse } from '@/api/generated/model';
import { ReflectionResponseVisibility, EventResponseStatus } from '@/api/generated/model';
import { Button } from '@/components/ui/button';
import { ArrowLeft, BookOpen, Eye, Edit2, Trash2, PlusCircle, Search, X } from 'lucide-vue-next';
import ReflectionForm from '@/components/reflections/ReflectionForm.vue';
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
  DialogFooter,
  DialogClose
} from '@/components/ui/dialog';
import { Input } from '@/components/ui/input';

const router = useRouter();
const queryClient = useQueryClient();
const authStore = useAuthStore();

// Fetch current user's reflections
const {
  data: reflections,
  isLoading: isLoadingReflections,
  error: reflectionsError,
  refetch: refetchMyReflections
} = useGetCurrentUserReflections<ReflectionResponse[]>({
  query: {
    enabled: authStore.isAuthenticated,
    refetchOnWindowFocus: true,
    staleTime: 30000,
  }
});

const deleteReflectionMutation = useDeleteReflection({
  mutation: {
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: getGetCurrentUserReflectionsQueryKey() });
    },
    onError: (err: Error | unknown) => {
      console.error("Feil ved sletting av refleksjon:", err);
      alert("Kunne ikke slette refleksjon: " + (err instanceof Error ? err.message : 'Ukjent feil'));
    }
  }
});

const reflectionsErrorMessage = computed(() => {
  const e = reflectionsError.value as Error | null;
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

const viewReflection = (reflection: ReflectionResponse) => {
  router.push(`/refleksjon/${reflection.id}`);
};

const editReflection = (reflection: ReflectionResponse) => {
  router.push(`/refleksjon/${reflection.id}?action=edit`);
};

const confirmDeleteReflection = async (id: string) => {
  if (window.confirm('Er du sikker på at du vil slette denne refleksjonen? Handlingen kan ikke angres.')) {
    try {
      await deleteReflectionMutation.mutateAsync({ id });
    } catch (err) {/* Error handling in mutation onError */}
  }
};

const goBack = () => {
  router.back();
};

// --- New Reflection Flow ---
const isEventSelectDialogOpen = ref(false);
const isReflectionFormDialogOpen = ref(false);
const selectedEventIdForNewReflection = ref<number | null>(null);
const eventSearchTerm = ref('');

const {
  data: allEvents,
  isLoading: isLoadingEvents,
  error: eventsError
} = useGetAllEvents<EventResponse[]> ({
  query: {
    enabled: computed(() => isEventSelectDialogOpen.value),
    refetchOnWindowFocus: false,
  }
});

const finishedEventsForSelection = computed(() => {
  if (!allEvents.value) return [];
  return allEvents.value
    .filter(event => event.status === EventResponseStatus.FINISHED)
    .sort((a, b) => new Date(b.endTime || 0).getTime() - new Date(a.endTime || 0).getTime());
});

const filteredFinishedEventsForSelection = computed(() => {
  if (!finishedEventsForSelection.value) return [];
  if (!eventSearchTerm.value) return finishedEventsForSelection.value;
  return finishedEventsForSelection.value.filter(event =>
    event.title?.toLowerCase().includes(eventSearchTerm.value.toLowerCase())
  );
});

const eventsErrorMessage = computed(() => {
  const e = eventsError.value as Error | null;
  if (e instanceof Error) return e.message;
  return e ? 'Kunne ikke laste hendelser.' : '';
});

const openEventSelectDialog = () => {
  eventSearchTerm.value = '';
  selectedEventIdForNewReflection.value = null; // Clear previous selection
  isEventSelectDialogOpen.value = true;
};

const handleEventSelected = (event: EventResponse) => {
  if (event.id === undefined) {
    console.error("Selected event has no ID");
    return;
  }
  selectedEventIdForNewReflection.value = event.id;
  isEventSelectDialogOpen.value = false;
  isReflectionFormDialogOpen.value = true;
};

const handleNewReflectionCreated = () => {
  isReflectionFormDialogOpen.value = false;
  selectedEventIdForNewReflection.value = null;
  refetchMyReflections();
};

const cancelNewReflectionForm = () => {
  isReflectionFormDialogOpen.value = false;
  selectedEventIdForNewReflection.value = null;
};

const closeEventSelectDialog = () => {
  isEventSelectDialogOpen.value = false;
}
// --- End New Reflection Flow ---

// Fetch event titles for existing reflections
const eventTitles = ref<Record<string, string>>({});
const reflectionEventIdsToFetch = computed(() => {
  const ids = new Set<number>();
  if (reflections.value) {
    reflections.value.forEach((reflection: ReflectionResponse) => {
      if (reflection.eventId) {
        ids.add(reflection.eventId);
      }
    });
  }
  return Array.from(ids);
});

watch(reflectionEventIdsToFetch, (newIds) => {
  newIds.forEach(id => {
    if (!eventTitles.value[id]) {
      const eventIdRef = ref(id);
      const { data: eventData, error: fetchError } = useGetEventById(eventIdRef, {
        query: { enabled: computed(() => !!eventIdRef.value) }
      });
      watch(eventData, (newEventData) => {
        if (newEventData?.title) {
          eventTitles.value = { ...eventTitles.value, [id]: newEventData.title };
        }
      }, { immediate: true });
      watch(fetchError, (err) => {
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

<template>
  <div class="bg-gray-50 min-h-screen">
    <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      <!-- Page Header with background -->
      <div class="bg-white rounded-lg shadow-sm border border-gray-200 p-6 mb-6">
        <div class="flex items-center justify-between mb-4">
          <router-link
            to="/kriser"
            class="inline-flex items-center text-blue-600 hover:text-blue-800"
          >
            ← Tilbake til kriser
          </router-link>
          <Button variant="outline" class="flex items-center" @click="openEventSelectDialog">
            <PlusCircle class="h-4 w-4 mr-2" />
            Ny refleksjon
          </Button>
        </div>
        <div>
          <h1 class="text-3xl font-bold text-gray-900 mb-1">Mine Refleksjoner</h1>
          <p class="text-gray-600 max-w-3xl">
            Her finner du alle refleksjonene du har skrevet knyttet til ulike hendelser.
          </p>
        </div>
      </div>

      <!-- Loading state with improved animation -->
      <div v-if="isLoadingReflections" class="grid md:grid-cols-2 lg:grid-cols-3 gap-6">
        <div v-for="n in 3" :key="n" class="bg-white rounded-lg shadow-md overflow-hidden flex flex-col relative border transition-all duration-300 min-h-[220px] animate-pulse">
          <div class="h-1 w-full bg-gray-200"></div>
          <div class="p-4 flex-1 flex flex-col">
            <div class="h-5 bg-gray-200 rounded w-2/3 mb-2"></div>
            <div class="h-4 bg-gray-100 rounded w-1/3 mb-4"></div>
            <div class="h-3 bg-gray-100 rounded w-full mb-2"></div>
            <div class="h-3 bg-gray-100 rounded w-5/6 mb-2"></div>
            <div class="h-3 bg-gray-100 rounded w-4/6 mb-2"></div>
            <div class="mt-auto pt-3 border-t flex justify-end space-x-2">
              <div class="h-8 w-20 bg-gray-200 rounded"></div>
            </div>
          </div>
        </div>
      </div>

      <!-- Error state with better visual cues -->
      <div v-else-if="reflectionsError" class="bg-white rounded-lg shadow-lg p-6">
        <div class="bg-red-50 border-l-4 border-red-500 p-4 rounded">
          <div class="flex">
            <div class="flex-shrink-0">
              <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 text-red-500" viewBox="0 0 20 20" fill="currentColor">
                <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.707 7.293a1 1 0 00-1.414 1.414L8.586 10l-1.293 1.293a1 1 0 101.414 1.414L10 11.414l1.293 1.293a1 1 0 001.414-1.414L11.414 10l1.293-1.293a1 1 0 00-1.414-1.414L10 8.586 8.707 7.293z" clip-rule="evenodd" />
              </svg>
            </div>
            <div class="ml-3">
              <p class="text-sm text-red-700">
                Kunne ikke laste refleksjoner: {{ reflectionsErrorMessage }}
              </p>
            </div>
          </div>
        </div>
      </div>

      <!-- Reflection cards -->
      <div v-else-if="reflections && reflections.length > 0" class="grid md:grid-cols-2 lg:grid-cols-3 gap-6">
        <div
          v-for="reflection in reflections"
          :key="reflection.id"
          class="bg-white rounded-lg shadow-md overflow-hidden flex flex-col relative border transition-all duration-300 hover:shadow-lg min-h-[220px]"
        >
          <!-- Colored top bar based on visibility -->
          <div
            class="h-1 w-full"
            :class="{
              'bg-blue-500': reflection.visibility === ReflectionResponseVisibility.PRIVATE,
              'bg-green-500': reflection.visibility === ReflectionResponseVisibility.HOUSEHOLD,
              'bg-indigo-500': reflection.visibility === ReflectionResponseVisibility.PUBLIC
            }"
          ></div>

          <!-- Decorative corner -->
          <div class="absolute top-0 right-0 w-16 h-16 rounded-bl-full -mt-1 -mr-1 overflow-hidden z-0"
               :class="{
                 'bg-blue-50/70': reflection.visibility === ReflectionResponseVisibility.PRIVATE,
                 'bg-green-50/70': reflection.visibility === ReflectionResponseVisibility.HOUSEHOLD,
                 'bg-indigo-50/70': reflection.visibility === ReflectionResponseVisibility.PUBLIC
               }">
            <div class="absolute top-2.5 right-2.5">
              <BookOpen
                class="h-6 w-6"
                :class="{
                  'text-blue-600': reflection.visibility === ReflectionResponseVisibility.PRIVATE,
                  'text-green-600': reflection.visibility === ReflectionResponseVisibility.HOUSEHOLD,
                  'text-indigo-600': reflection.visibility === ReflectionResponseVisibility.PUBLIC
                }"/>
            </div>
          </div>

          <div class="p-4 relative z-10 flex-grow flex flex-col">
            <h3 class="text-lg font-semibold mb-1 pr-10">{{ reflection.title }}</h3>
            <p class="text-xs text-gray-500 mb-2">
              <span class="inline-flex items-center px-2 py-0.5 rounded-full bg-gray-100 text-gray-800 text-xs mr-1">
                {{ mapReflectionVisibility(reflection.visibility) }}
              </span>
              <span class="inline-flex items-center px-2 py-0.5 rounded-full bg-gray-100 text-gray-800 text-xs">
                {{ formatDate(reflection.updatedAt) }}
              </span>
            </p>

            <div v-if="reflection.eventId && eventTitles[reflection.eventId]" class="mb-2 text-sm text-gray-700">
              <span class="inline-flex items-center px-2 py-0.5 rounded-full bg-purple-100 text-purple-800 text-xs">
                <router-link :to="`/kriser/${reflection.eventId}`" class="hover:underline">
                  {{ eventTitles[reflection.eventId] }}
                </router-link>
              </span>
            </div>
            <div v-else-if="reflection.eventId" class="mb-2 text-sm text-gray-500 italic">
              Laster hendelsesnavn...
            </div>

            <p v-if="reflection.content" class="text-sm text-gray-600 flex-grow mb-3 line-clamp-3" v-html="stripHtml(reflection.content)"></p>

            <div class="mt-auto pt-3 border-t flex justify-end space-x-2">
              <Button size="sm" variant="outline" @click="viewReflection(reflection)" class="flex items-center">
                <Eye class="h-4 w-4 mr-1"/>Se detaljer
              </Button>
              <Button
                v-if="canManageReflection(reflection)"
                size="sm"
                variant="destructive"
                @click="confirmDeleteReflection(reflection.id!)"
                class="flex items-center"
              >
                <Trash2 class="h-4 w-4 mr-1"/>Slett
              </Button>
            </div>
          </div>
        </div>
      </div>

      <!-- Empty state -->
      <div v-else-if="!isLoadingReflections && !reflectionsError" class="bg-white rounded-lg shadow-md p-8 text-center">
        <BookOpen class="mx-auto h-12 w-12 text-gray-400" />
        <h3 class="mt-2 text-xl font-medium text-gray-900">Ingen refleksjoner</h3>
        <p class="mt-1 text-base text-gray-500">Du har ikke skrevet noen refleksjoner ennå, eller ingen ble funnet.</p>
        <Button class="mt-6" @click="openEventSelectDialog">
          <PlusCircle class="h-4 w-4 mr-2" />
          Skriv din første refleksjon
        </Button>
      </div>

      <!-- Event Selection Dialog -->
      <Dialog :open="isEventSelectDialogOpen" @update:open="isEventSelectDialogOpen = $event">
        <DialogContent class="sm:max-w-[500px] p-0 overflow-hidden">
          <DialogHeader class="p-6 pb-2">
            <DialogTitle class="text-xl">Velg en avsluttet hendelse</DialogTitle>
            <DialogDescription>
              Refleksjoner må knyttes til en tidligere hendelse. Velg en hendelse fra listen under.
            </DialogDescription>
          </DialogHeader>
          <div class="p-6 pt-4">
            <div class="relative mb-4">
              <Input
                type="text"
                placeholder="Søk etter hendelse..."
                v-model="eventSearchTerm"
                class="pl-10"
              />
              <Search class="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-gray-400"/>
              <Button v-if="eventSearchTerm" variant="ghost" size="icon" class="absolute right-2 top-1/2 -translate-y-1/2 h-7 w-7" @click="eventSearchTerm = ''">
                <X class="h-4 w-4" />
              </Button>
            </div>
            <div v-if="isLoadingEvents" class="text-center py-8">
              <div class="inline-block animate-spin rounded-full h-8 w-8 border-t-2 border-b-2 border-blue-500 mb-4"></div>
              <p class="text-gray-600">Laster hendelser...</p>
            </div>
            <div v-else-if="eventsError" class="bg-red-50 border-l-4 border-red-500 p-4 rounded my-4">
              <p class="text-sm text-red-700">{{ eventsErrorMessage }}</p>
            </div>
            <div v-else-if="filteredFinishedEventsForSelection.length === 0" class="text-center py-8 text-gray-500">
              <svg xmlns="http://www.w3.org/2000/svg" class="h-12 w-12 text-gray-400 mx-auto mb-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
              </svg>
              <p>Ingen avsluttede hendelser funnet{{ eventSearchTerm ? ' for ditt søk' : '' }}.</p>
            </div>
            <div v-else class="max-h-[300px] overflow-y-auto space-y-2 pr-2">
              <div
                v-for="event in filteredFinishedEventsForSelection"
                :key="event.id"
                @click="handleEventSelected(event)"
                class="p-3 rounded-md hover:bg-gray-100 cursor-pointer border border-gray-200 transition-colors"
              >
                <h4 class="font-medium">{{ event.title }}</h4>
                <p class="text-xs text-gray-500">Avsluttet: {{ formatDate(event.endTime) }}</p>
              </div>
            </div>
          </div>
          <div class="p-4 bg-gray-50 border-t flex justify-end">
            <Button variant="outline" @click="closeEventSelectDialog">Avbryt</Button>
          </div>
        </DialogContent>
      </Dialog>

      <!-- Reflection Form Dialog -->
      <Dialog :open="isReflectionFormDialogOpen" @update:open="isReflectionFormDialogOpen = $event">
        <DialogContent class="sm:max-w-[600px] p-0 overflow-hidden">
          <DialogHeader class="p-6 pb-2">
            <DialogTitle class="text-xl">Skriv en ny refleksjon</DialogTitle>
            <DialogDescription>
              Del dine tanker og erfaringer knyttet til den valgte hendelsen.
            </DialogDescription>
          </DialogHeader>
          <div class="p-6 pt-2">
            <ReflectionForm
              v-if="selectedEventIdForNewReflection !== null"
              :event-id="selectedEventIdForNewReflection"
              @reflection-created="handleNewReflectionCreated"
              @cancel="cancelNewReflectionForm"
              class="pt-4"
            />
          </div>
        </DialogContent>
      </Dialog>
    </div>
  </div>
</template>



<style scoped>
.line-clamp-3 {
  overflow: hidden;
  display: -webkit-box;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 3;
}
</style>
