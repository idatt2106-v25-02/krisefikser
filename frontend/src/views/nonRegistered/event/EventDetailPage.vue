<template>
  <div class="container mx-auto p-4">
    <div v-if="eventLoading">Laster hendelsesdetaljer...</div>
    <div v-if="eventError">Kunne ikke laste hendelse: {{ eventErrorMessage }}</div>

    <div v-if="event">
      <h1 class="text-3xl font-bold mb-2">{{ event.title }}</h1>
      <p class="text-gray-600 mb-4">Status: {{ mapEventStatus(event.status) }} | Start: {{ formatDate(event.startTime) }} | Slutt: {{ formatDate(event.endTime) }}</p>
      <p class="mb-6">{{ event.description }}</p>

      <hr class="my-6">

      <div v-if="event?.status === EventResponseStatus.FINISHED">
        <h2 class="text-2xl font-semibold mb-4">Refleksjoner</h2>
        <div v-if="reflectionsLoading">Laster refleksjoner...</div>
        <div v-if="reflectionsError">Kunne ikke laste refleksjoner: {{ reflectionsErrorMessage }}</div>

        <div v-if="reflections && reflections.length > 0" class="grid md:grid-cols-2 gap-6">
          <div
            v-for="reflection in reflections"
            :key="reflection.id"
            :id="`reflection-${reflection.id}`"
            class="bg-white rounded-lg shadow-md overflow-hidden flex flex-col relative border transition-all duration-300 min-h-[150px]"
          >
            <div class="absolute top-0 right-0 w-16 h-16 rounded-bl-full -mt-1 -mr-1 overflow-hidden z-0 bg-blue-50/70">
              <div class="absolute top-2.5 right-2.5">
                <BookText class="h-6 w-6 text-blue-500" />
              </div>
            </div>

            <div class="p-4 relative z-10 flex-grow flex flex-col">
              <h3 class="text-lg font-semibold mb-1">{{ reflection.title }}</h3>
              <p class="text-xs text-gray-500 mb-2">
                Forfatter: {{ reflection.authorName }} |
                Synlighet: {{ mapReflectionVisibility(reflection.visibility) }} |
                Dato: {{ formatDate(reflection.createdAt) }}
              </p>
              <div v-html="reflection.content" class="prose prose-sm max-w-none text-sm flex-grow mb-3 line-clamp-4"></div>
              <div class="mt-auto pt-2 border-t flex justify-end space-x-2">
                <Button v-if="canManageReflection(reflection)" size="sm" variant="outline" @click="openEditDialog(reflection)">Rediger</Button>
                <Button v-if="canManageReflection(reflection)" size="sm" variant="destructive" @click="confirmDeleteReflection(reflection.id!)">Slett</Button>
              </div>
            </div>
          </div>
        </div>
        <p v-else-if="!reflectionsLoading && !reflectionsError" class="text-gray-500">
          Ingen refleksjoner for denne hendelsen ennå.
        </p>

        <button
          v-if="authStore.isAuthenticated"
          @click="openNewReflectionDialog"
          class="mt-6 bg-blue-600 text-white px-4 py-2 rounded hover:bg-blue-700 flex items-center">
          <Plus class="h-4 w-4 mr-1" /> Legg til Refleksjon
        </button>
      </div>

    </div>

    <Dialog :open="isFormOpen" @update:open="cancelEditIfNotOpen">
      <DialogContent class="sm:max-w-[600px]">
        <DialogHeader>
          <DialogTitle>{{ formTitle }}</DialogTitle>
          <DialogDescription>{{ formDescription }}</DialogDescription>
        </DialogHeader>
        <ReflectionForm
          v-if="isFormOpen"
          :key="editingReflection ? editingReflection.id : 'new'"
          :event-id="eventId"
          :initial-data="editingReflection || undefined"
          @reflection-created="handleReflectionSubmitted"
          @reflection-updated="handleReflectionSubmitted"
          @cancel="cancelEdit"
          class="pt-4"
        />
      </DialogContent>
    </Dialog>

  </div>
</template>

<script lang="ts">
import { defineComponent, ref, computed, onMounted, watch, nextTick } from 'vue';
import { useRoute } from 'vue-router';
import { useQueryClient } from '@tanstack/vue-query';
import { useAuthStore } from '@/stores/auth/useAuthStore.ts';
import { useGetEventById } from '@/api/generated/event/event.ts'; // Adjust path as needed
import { useGetReflectionsByEventId, useDeleteReflection, getGetReflectionsByEventIdQueryKey } from '@/api/generated/reflection/reflection.ts'; // Adjust path as needed
import type { EventResponse, ReflectionResponse } from '@/api/generated/model'; // Adjust path as needed
import { EventResponseStatus, ReflectionResponseVisibility } from '@/api/generated/model'; // Import enums
import ReflectionForm from '@/components/reflections/ReflectionForm.vue'; // <-- Import ReflectionForm
import { Button } from '@/components/ui/button'; // Import Button
import { ArrowLeft, BookText, Plus } from 'lucide-vue-next'; // Import icons
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
} from '@/components/ui/dialog'; // Import Dialog components

export default defineComponent({
  name: 'EventDetailPage',
  components: { ReflectionForm, Button, ArrowLeft, BookText, Plus, Dialog, DialogContent, DialogDescription, DialogHeader, DialogTitle },
  setup() {
    const route = useRoute();
    const queryClient = useQueryClient();
    const authStore = useAuthStore();
    const eventId = computed(() => Number(route.params.id));

    const isEditing = ref(false); // To control the dialog visibility
    const editingReflection = ref<ReflectionResponse | null>(null); // Holds data for editing

    // Compute form title and description based on mode
    const formTitle = computed(() => editingReflection.value ? 'Rediger Refleksjon' : 'Skriv en ny refleksjon');
    const formDescription = computed(() => editingReflection.value ? 'Gjør endringer i refleksjonen din her.' : 'Del dine tanker og erfaringer knyttet til denne hendelsen.');
    const isFormOpen = computed(() => isEditing.value || showCreateReflectionForm.value); // Combined flag for dialog
    const showCreateReflectionForm = ref(false); // Specific flag for creating new

    // Fetch Event Details
    const {
      data: event,
      isLoading: eventLoading,
      error: eventError,
    } = useGetEventById(eventId);

    const mapEventStatus = (status?: EventResponseStatus): string => {
      if (!status) return 'Ukjent';
      switch (status) {
        case EventResponseStatus.UPCOMING: return 'Kommende';
        case EventResponseStatus.ONGOING: return 'Pågående';
        case EventResponseStatus.FINISHED: return 'Avsluttet';
        default:
          const exhaustiveCheck: never = status;
          console.warn(`Ukjent hendelsesstatus: ${exhaustiveCheck}`);
          return 'Ukjent status';
      }
    };

    const mapReflectionVisibility = (visibility?: ReflectionResponseVisibility): string => {
      if (!visibility) return 'Ukjent';
      switch (visibility) {
        case ReflectionResponseVisibility.PUBLIC: return 'Offentlig';
        case ReflectionResponseVisibility.HOUSEHOLD: return 'Husstand';
        case ReflectionResponseVisibility.PRIVATE: return 'Privat';
        default:
          const exhaustiveCheck: never = visibility;
          console.warn(`Ukjent refleksjonsynlighet: ${exhaustiveCheck}`);
          return 'Ukjent synlighet';
      }
    };

    const eventErrorMessage = computed(() => {
      if (eventError.value instanceof Error) return eventError.value.message;
      return eventError.value ? 'En ukjent feil oppstod ved lasting av hendelse.' : '';
    });

    // Fetch Reflections for the Event
    const {
      data: reflections,
      isLoading: reflectionsLoading,
      error: reflectionsError,
      refetch: refetchReflections
    } = useGetReflectionsByEventId(eventId, { query: { enabled: computed(() => !!eventId.value && eventId.value > 0) }});

    const deleteReflectionMutation = useDeleteReflection();

    const reflectionsErrorMessage = computed(() => {
      if (reflectionsError.value instanceof Error) return reflectionsError.value.message;
      return reflectionsError.value ? 'En ukjent feil oppstod ved lasting av refleksjoner.' : '';
    });

    const formatDate = (dateTimeString?: string) => {
      if (!dateTimeString) return 'Ukjent dato';
      try {
        return new Date(dateTimeString).toLocaleDateString('nb-NO', {
          year: 'numeric',
          month: 'long',
          day: 'numeric',
          hour: '2-digit',
          minute: '2-digit',
        });
      } catch (e) {
        return dateTimeString;
      }
    };

    const canManageReflection = (reflection: ReflectionResponse) => {
      if (!authStore.currentUser) return false;
      return authStore.isAdmin || authStore.currentUser.id === reflection.authorId;
    };

    const scrollToReflection = (reflectionId: string) => {
      // Ensure DOM is updated before trying to scroll
      nextTick(() => {
        const element = document.getElementById(`reflection-${reflectionId}`);
        if (element) {
          element.scrollIntoView({ behavior: 'smooth', block: 'center' });
          // Optional: Add a temporary highlight effect
          element.classList.add('ring-2', 'ring-blue-500', 'transition-all', 'duration-1000');
          setTimeout(() => {
            element.classList.remove('ring-2', 'ring-blue-500', 'transition-all', 'duration-1000');
          }, 2000);
        }
      });
    };

    // Handles opening and closing the dialog
    const openNewReflectionDialog = () => {
      editingReflection.value = null; // Ensure not in edit mode
      showCreateReflectionForm.value = true;
      isEditing.value = false; // Also ensure this is false
    };

    const openEditDialog = (reflection: ReflectionResponse) => {
      if (canManageReflection(reflection)) {
        editingReflection.value = { ...reflection };
        showCreateReflectionForm.value = false;
        isEditing.value = true; // Open dialog in edit mode
      }
    };

    const cancelEdit = () => {
      isEditing.value = false;
      showCreateReflectionForm.value = false;
      editingReflection.value = null;
    };

    // Handles closing via dialog overlay click or escape key
    const cancelEditIfNotOpen = (openState: boolean) => {
      if (!openState) {
        cancelEdit();
      }
    };

    const handleReflectionSubmitted = () => {
      cancelEdit();
      // Query invalidation is handled by ReflectionForm,
      // but we could add extra checks or notifications here if needed.
    };

    const confirmDeleteReflection = async (reflectionId: string) => {
      if (!reflectionId) return;
      const reflectionToDelete = reflections.value?.find(r => r.id === reflectionId);
      if (!reflectionToDelete || !canManageReflection(reflectionToDelete)) return;

      if (window.confirm('Er du sikker på at du vil slette denne refleksjonen?')) {
        try {
          await deleteReflectionMutation.mutateAsync({ id: reflectionId });
          queryClient.invalidateQueries({ queryKey: getGetReflectionsByEventIdQueryKey(eventId.value) });
        } catch (err) {
           console.error("Feil ved sletting:", err);
           alert("Kunne ikke slette: " + (err instanceof Error ? err.message : 'Ukjent feil'));
        }
      }
    };

    // Watch for route query changes to handle navigation focus/edit
    watch(() => route.query, (newQuery) => {
      const targetReflectionId = newQuery.reflectionId as string;
      const targetAction = newQuery.action as string;

      if (targetReflectionId && reflections.value) {
        const reflectionToFocus = reflections.value.find(r => r.id === targetReflectionId);
        if (reflectionToFocus) {
          scrollToReflection(targetReflectionId);
          if (targetAction === 'edit' && canManageReflection(reflectionToFocus)) {
            openEditDialog(reflectionToFocus);
          }
        }
      }
    }, { immediate: true, deep: true }); // Use deep if query object structure matters

    return {
      event,
      eventLoading,
      eventError,
      eventErrorMessage,
      reflections,
      reflectionsLoading,
      reflectionsError,
      reflectionsErrorMessage,
      formatDate,
      showCreateReflectionForm,
      editingReflection,
      canManageReflection,
      confirmDeleteReflection,
      openNewReflectionDialog,
      openEditDialog,
      cancelEdit,
      cancelEditIfNotOpen,
      handleReflectionSubmitted,
      eventId,
      mapEventStatus,
      mapReflectionVisibility,
      isFormOpen,
      formTitle,
      formDescription,
      authStore,
      EventResponseStatus,
    };
  },
});
</script>

<style scoped>
/* Add any page-specific styles here */
.line-clamp-4 {
  overflow: hidden;
  display: -webkit-box;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 4; /* Adjust line count as needed */
}
</style>
