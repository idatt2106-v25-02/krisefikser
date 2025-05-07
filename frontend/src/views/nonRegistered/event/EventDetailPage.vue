<script lang="ts">
import { defineComponent, ref, computed, watch, nextTick } from 'vue';
import { useRoute } from 'vue-router';
import { useQueryClient } from '@tanstack/vue-query';
import { useAuthStore } from '@/stores/auth/useAuthStore.ts';
import { useGetEventById } from '@/api/generated/event/event.ts'; // Adjust path as needed
import { useGetReflectionsByEventId, useDeleteReflection, getGetReflectionsByEventIdQueryKey } from '@/api/generated/reflection/reflection.ts'; // Adjust path as needed
import type { ReflectionResponse } from '@/api/generated/model'; // Adjust path as needed
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

<template>
  <div class="container mx-auto px-4 py-8 max-w-6xl">
    <!-- Background decoration - similar wave pattern for consistency -->
    <div class="absolute top-0 left-0 w-full h-64 bg-blue-50 -z-10 overflow-hidden">
      <div class="absolute bottom-0 left-0 right-0">
        <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 1440 320" class="w-full h-auto">
          <path fill="#ffffff" fill-opacity="1" d="M0,128L48,144C96,160,192,192,288,186.7C384,181,480,139,576,138.7C672,139,768,181,864,170.7C960,160,1056,96,1152,85.3C1248,75,1344,117,1392,138.7L1440,160L1440,320L1392,320C1344,320,1248,320,1152,320C1056,320,960,320,864,320C768,320,672,320,576,320C480,320,384,320,288,320C192,320,96,320,48,320L0,320Z"></path>
        </svg>
      </div>
    </div>

    <!-- Loading state -->
    <div v-if="eventLoading" class="bg-white rounded-lg shadow-lg p-8 flex flex-col items-center justify-center py-16 mt-6">
      <div class="inline-block animate-spin rounded-full h-12 w-12 border-4 border-b-blue-500 border-blue-100 mb-4"></div>
      <p class="text-gray-600 font-medium">Laster hendelsesdetaljer...</p>
    </div>

    <!-- Error state -->
    <div v-if="eventError" class="bg-white rounded-lg shadow-lg p-8 border-l-4 border-red-500 mt-6">
      <div class="flex items-start">
        <div class="flex-shrink-0 mt-0.5">
          <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6 text-red-500" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
          </svg>
        </div>
        <div class="ml-3">
          <h3 class="text-lg font-medium text-red-800">Kunne ikke laste hendelse</h3>
          <p class="mt-2 text-red-700">{{ eventErrorMessage }}</p>
        </div>
      </div>
    </div>

    <div v-if="event" class="mt-6">
      <!-- Event header card with improved styling -->
      <div class="bg-white rounded-lg shadow-lg overflow-hidden mb-8">
        <!-- Status banner based on event status - UPDATED COLORS to match KriserPage -->
        <div
          class="w-full h-2"
          :class="{
            'bg-blue-500': event.status === EventResponseStatus.UPCOMING,
            'bg-red-500': event.status === EventResponseStatus.ONGOING,
            'bg-gray-300': event.status === EventResponseStatus.FINISHED
          }"
        ></div>

        <div class="p-6">
          <h1 class="text-3xl font-bold mb-4 text-gray-800">{{ event.title }}</h1>

          <!-- Metadata badges - UPDATED COLORS to match KriserPage -->
          <div class="flex flex-wrap gap-2 mb-6">
            <span
              class="inline-flex items-center px-3 py-1 rounded-full text-sm font-medium"
              :class="{
                'bg-blue-100 text-blue-800': event.status === EventResponseStatus.UPCOMING,
                'bg-red-100 text-red-800': event.status === EventResponseStatus.ONGOING,
                'bg-gray-100 text-gray-800': event.status === EventResponseStatus.FINISHED
              }"
            >
              <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4 mr-1.5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 10V3L4 14h7v7l9-11h-7z" />
              </svg>
              {{ mapEventStatus(event.status) }}
            </span>

            <span class="inline-flex items-center px-3 py-1 rounded-full bg-gray-100 text-gray-800 text-sm font-medium">
              <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4 mr-1.5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
              </svg>
              {{ event.status === EventResponseStatus.FINISHED ? 'Avsluttet: ' :
              event.status === EventResponseStatus.ONGOING ? 'Startet: ' : 'Starter: ' }}
              {{ formatDate(event.status === EventResponseStatus.FINISHED ? event.endTime : event.startTime) }}
            </span>
          </div>

          <!-- Description with proper formatting -->
          <div class="prose prose-blue max-w-none text-gray-700">
            <p class="text-base leading-relaxed">{{ event.description }}</p>
          </div>
        </div>
      </div>

      </div>
  </div>
</template>



