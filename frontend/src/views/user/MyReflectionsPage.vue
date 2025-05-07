<template>
  <div class="container mx-auto p-4">
    <div class="flex justify-between items-center mb-6">
      <h1 class="text-3xl font-bold">Mine Refleksjoner</h1>
      <div class="flex space-x-2">
        <Button variant="outline" @click="openEventSelectDialog">
          <PlusCircle class="h-4 w-4 mr-2" />
          Legg til ny refleksjon
        </Button>
        <Button variant="outline" @click="goBack">
          <ArrowLeft class="h-4 w-4 mr-2" />
          Tilbake
        </Button>
      </div>
    </div>

    <div v-if="isLoadingReflections">Laster refleksjoner...</div>
    <div v-if="reflectionsError">
      <p class="text-red-500">Kunne ikke laste refleksjoner: {{ reflectionsErrorMessage }}</p>
    </div>

    <div v-if="reflections && reflections.length > 0" class="grid md:grid-cols-2 lg:grid-cols-3 gap-6">
      <div 
        v-for="reflection in reflections" 
        :key="reflection.id"
        class="bg-white rounded-lg shadow-md overflow-hidden flex flex-col relative border transition-all duration-300 hover:shadow-lg min-h-[220px]"
      >
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
          <h3 class="text-lg font-semibold mb-1">{{ reflection.title }}</h3>
          <p class="text-xs text-gray-500 mb-2">
            Synlighet: {{ mapReflectionVisibility(reflection.visibility) }} | 
            Oppdatert: {{ formatDate(reflection.updatedAt) }}
          </p>
          
          <div v-if="reflection.eventId && eventTitles[reflection.eventId]" class="mb-2 text-sm text-gray-700">
            Knyttet til: 
            <router-link :to="`/kriser/${reflection.eventId}`" class="text-blue-600 hover:underline">
              {{ eventTitles[reflection.eventId] }}
            </router-link>
          </div>
          <div v-else-if="reflection.eventId" class="mb-2 text-sm text-gray-500 italic">
            Laster hendelsesnavn...
          </div>

          <p v-if="reflection.content" class="text-sm text-gray-600 flex-grow mb-3 line-clamp-3" v-html="stripHtml(reflection.content)"></p>
          
          <div class="mt-auto pt-3 border-t flex justify-end space-x-2">
            <Button size="sm" variant="outline" @click="viewReflection(reflection)"><Eye class="h-4 w-4 mr-1"/>Vis</Button>
            <Button v-if="canManageReflection(reflection)" size="sm" variant="outline" @click="editReflection(reflection)"><Edit2 class="h-4 w-4 mr-1"/>Rediger</Button>
            <Button v-if="canManageReflection(reflection)" size="sm" variant="destructive" @click="confirmDeleteReflection(reflection.id!)"><Trash2 class="h-4 w-4 mr-1"/>Slett</Button>
          </div>
        </div>
      </div>
    </div>
    <div v-else-if="!isLoadingReflections && !reflectionsError" class="text-center py-10">
      <BookOpen class="mx-auto h-12 w-12 text-gray-400" />
      <h3 class="mt-2 text-sm font-medium text-gray-900">Ingen refleksjoner</h3>
      <p class="mt-1 text-sm text-gray-500">Du har ikke skrevet noen refleksjoner ennå, eller ingen ble funnet.</p>
      <Button class="mt-4" @click="openEventSelectDialog">
        <PlusCircle class="h-4 w-4 mr-2" />
        Skriv din første refleksjon
      </Button>
    </div>

    <!-- Event Selection Dialog -->
    <Dialog :open="isEventSelectDialogOpen" @update:open="isEventSelectDialogOpen = $event">
      <DialogContent class="sm:max-w-[500px]">
        <DialogHeader>
          <DialogTitle>Velg en avsluttet hendelse</DialogTitle>
          <DialogDescription>
            Refleksjoner må knyttes til en tidligere hendelse. Velg en hendelse fra listen under.
          </DialogDescription>
        </DialogHeader>
        <div class="py-4">
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
          <div v-if="isLoadingEvents" class="text-center text-gray-500">Laster hendelser...</div>
          <div v-else-if="eventsError" class="text-center text-red-500">{{ eventsErrorMessage }}</div>
          <div v-else-if="filteredFinishedEventsForSelection.length === 0" class="text-center text-gray-500">
            Ingen avsluttede hendelser funnet{{ eventSearchTerm ? ' for ditt søk' : '' }}.
          </div>
          <div v-else class="max-h-[300px] overflow-y-auto space-y-2 pr-2">
            <div 
              v-for="event in filteredFinishedEventsForSelection" 
              :key="event.id"
              @click="handleEventSelected(event)"
              class="p-3 rounded-md hover:bg-gray-100 cursor-pointer border"
            >
              <h4 class="font-medium">{{ event.title }}</h4>
              <p class="text-xs text-gray-500">Avsluttet: {{ formatDate(event.endTime) }}</p>
            </div>
          </div>
        </div>
        <DialogFooter>
            <Button variant="outline" @click="closeEventSelectDialog">Avbryt</Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>

    <!-- Reflection Form Dialog -->
    <Dialog :open="isReflectionFormDialogOpen" @update:open="isReflectionFormDialogOpen = $event">
      <DialogContent class="sm:max-w-[600px]">
        <DialogHeader>
          <DialogTitle>Skriv en ny refleksjon</DialogTitle>
          <DialogDescription>
            Del dine tanker og erfaringer knyttet til den valgte hendelsen.
          </DialogDescription>
        </DialogHeader>
        <ReflectionForm 
          v-if="selectedEventIdForNewReflection !== null" 
          :event-id="selectedEventIdForNewReflection" 
          @reflection-created="handleNewReflectionCreated" 
          @cancel="cancelNewReflectionForm"
          class="pt-4"
        />
      </DialogContent>
    </Dialog>

  </div>
</template>

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

const formatDate = (dateTimeString?: string) => {
  if (!dateTimeString) return 'Ukjent dato';
  try {
    return new Date(dateTimeString).toLocaleDateString('nb-NO', {
      year: 'numeric', month: 'long', day: 'numeric',
    });
  } catch (e) {
    console.warn("Invalid date format:", dateTimeString);
    return dateTimeString; 
  }
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

<style scoped>
.line-clamp-3 {
  overflow: hidden;
  display: -webkit-box;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 3;
}
</style> 