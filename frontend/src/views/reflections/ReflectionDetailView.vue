<template>
  <div class="container mx-auto px-4 py-12 max-w-4xl">
    <!-- Background decoration - wave pattern -->
    <div class="absolute top-0 left-0 w-full h-64 bg-blue-50 -z-10 overflow-hidden">
      <div class="absolute bottom-0 left-0 right-0">
        <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 1440 320" class="w-full h-auto">
          <path fill="#ffffff" fill-opacity="1" d="M0,128L48,144C96,160,192,192,288,186.7C384,181,480,139,576,138.7C672,139,768,181,864,170.7C960,160,1056,96,1152,85.3C1248,75,1344,117,1392,138.7L1440,160L1440,320L1392,320C1344,320,1248,320,1152,320C1056,320,960,320,864,320C768,320,672,320,576,320C480,320,384,320,288,320C192,320,96,320,48,320L0,320Z"></path>
        </svg>
      </div>
    </div>

    <button class="mb-6 text-blue-600 hover:underline font-medium flex items-center" @click="goBack">
      <ArrowLeft class="h-5 w-5 mr-1" /> Tilbake til Mine Refleksjoner
    </button>

    <div v-if="isLoading" class="text-center py-8">
      <div class="inline-block animate-spin rounded-full h-8 w-8 border-t-2 border-b-2 border-blue-500 mb-4"></div>
      <p class="text-gray-600">Laster refleksjon...</p>
    </div>

    <div v-else-if="error" class="text-center py-8">
       <p class="text-red-500">Kunne ikke laste refleksjon: {{ errorMessage }}</p>
    </div>

    <div v-else-if="reflection" class="bg-white rounded-lg shadow-lg p-8 relative">
      <!-- Decorative element -->
      <div class="absolute top-0 right-0 w-32 h-32 bg-blue-50/70 rounded-bl-full -mt-2 -mr-2 overflow-hidden z-0 icon-corner">
        <div class="absolute top-5 right-5 icon-wrapper">
          <BookText class="h-10 w-10 text-blue-500 icon" /> <!-- Icon for reflection -->
        </div>
      </div>

      <!-- Reflection content -->
      <div class="relative z-10">
        <div class="flex justify-between items-start mb-4">
          <h1 class="text-2xl sm:text-3xl font-bold text-gray-800 border-b pb-4 flex-grow mr-4">{{ reflection.title }}</h1>
           <!-- Edit/Delete Buttons -->
           <div v-if="canManageReflection(reflection)" class="flex space-x-3 flex-shrink-0 mt-1">
              <Button size="sm" variant="outline" @click="openEditForm">Rediger</Button>
              <Button size="sm" variant="destructive" @click="confirmDelete">Slett</Button>
            </div>
        </div>
        <p class="text-sm text-gray-500 mb-6">
            Forfatter: {{ reflection.authorName }} | 
            Synlighet: {{ mapReflectionVisibility(reflection.visibility) }} | 
            Sist endret: {{ formatDate(reflection.updatedAt) }}
            <!-- Link to Event - Now displays title -->
            <span v-if="reflection.eventId"> | 
              <router-link 
                :to="{ name: 'event-detail', params: { id: reflection.eventId } }" 
                class="text-blue-600 hover:underline"
                :title="`Gå til hendelse #${reflection.eventId}`"
              >
                <span v-if="isEventLoading">Laster hendelse...</span>
                <span v-else-if="associatedEvent?.title">{{ associatedEvent.title }}</span>
                <span v-else>Hendelse #{{ reflection.eventId }}</span> <!-- Fallback -->
              </router-link>
            </span>
        </p>

        <div class="text-gray-700 leading-relaxed reflection-content prose max-w-none" v-html="reflection.content"></div>
      </div>
    </div>

    <div v-else class="text-center py-8">
      <p class="text-gray-600">Refleksjon ikke funnet.</p>
    </div>

    <!-- Edit Modal/Form -->
    <Dialog :open="isEditing" @update:open="cancelEditIfNotOpen">
      <DialogContent class="sm:max-w-[600px]">
        <DialogHeader>
          <DialogTitle>Rediger Refleksjon</DialogTitle>
          <DialogDescription>
            Gjør endringer i refleksjonen din her.
          </DialogDescription>
        </DialogHeader>
        <ReflectionForm 
          v-if="isEditing && reflectionToEdit" 
          :key="reflectionToEdit.id" 
          :event-id="reflectionToEdit.eventId!" 
          :initial-data="reflectionToEdit"
          @reflection-updated="handleReflectionUpdated"
          @cancel="cancelEdit"
          class="pt-4"
        />
      </DialogContent>
    </Dialog>

  </div>
</template>

<script lang="ts">
import { defineComponent, computed, ref, watch, nextTick } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useQueryClient } from '@tanstack/vue-query';
import {
  useGetReflectionById,
  useDeleteReflection,
  getGetReflectionsByEventIdQueryKey,
  getGetCurrentUserReflectionsQueryKey
} from '@/api/generated/reflection/reflection';
import { useGetEventById } from '@/api/generated/event/event';
import type { ReflectionResponse, EventResponse } from '@/api/generated/model';
import { ReflectionResponseVisibility } from '@/api/generated/model';
import { useAuthStore } from '@/stores/auth/useAuthStore';
import { ArrowLeft, BookText } from 'lucide-vue-next';
import { Button } from '@/components/ui/button';
import ReflectionForm from '@/components/reflections/ReflectionForm.vue';
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
} from '@/components/ui/dialog';

export default defineComponent({
  name: 'ReflectionDetailView',
  components: { 
    ArrowLeft, 
    BookText, 
    Button, 
    ReflectionForm, 
    Dialog, 
    DialogContent, 
    DialogDescription, 
    DialogHeader, 
    DialogTitle 
  },
  setup() {
    const route = useRoute();
    const router = useRouter();
    const queryClient = useQueryClient();
    const authStore = useAuthStore();
    const reflectionId = computed(() => route.params.id as string);

    const isEditing = ref(false);
    const reflectionToEdit = ref<ReflectionResponse | null>(null);

    const { 
      data: reflection, 
      isLoading, 
      error, 
      refetch 
    } = useGetReflectionById(reflectionId);

    // Create a separate ref for the associated Event ID
    const associatedEventId = ref<number | undefined>(undefined);

    // Watch the reflection data to update the associatedEventId ref
    watch(reflection, (newReflection) => {
      if (newReflection?.eventId !== undefined) {
        associatedEventId.value = newReflection.eventId;
      } else {
        associatedEventId.value = undefined;
      }
    }, { immediate: true });

    // Fetch associated event details using the associatedEventId ref
    const { 
      data: associatedEvent,
      isLoading: isEventLoading,
      error: eventError 
    } = useGetEventById(
      associatedEventId, // Pass the ref<number | undefined>
      {
        query: {
          // Enable only when associatedEventId has a number value
          enabled: computed(() => typeof associatedEventId.value === 'number')
        }
      }
    );

    const deleteReflectionMutation = useDeleteReflection();

    const errorMessage = computed(() => {
      if (error.value instanceof Error) return error.value.message;
      if (eventError.value instanceof Error) return eventError.value.message;
      return error.value ? 'En ukjent feil oppstod.' : '';
    });

    const goBack = () => {
      router.push({ name: 'my-reflections' });
    };

    const canManageReflection = (reflectionData: ReflectionResponse | null | undefined) => {
      if (!reflectionData || !authStore.currentUser) return false;
      return authStore.isAdmin || authStore.currentUser.id === reflectionData.authorId;
    };

    const mapReflectionVisibility = (visibility?: ReflectionResponseVisibility): string => {
      if (!visibility) return 'Ukjent';
      switch (visibility) {
        case ReflectionResponseVisibility.PUBLIC: return 'Offentlig';
        case ReflectionResponseVisibility.HOUSEHOLD: return 'Husstand';
        case ReflectionResponseVisibility.PRIVATE: return 'Privat';
        default: return 'Ukjent synlighet';
      }
    };

    const formatDate = (dateTimeString?: string | null): string => {
       if (!dateTimeString) return 'Ukjent dato';
       try {
         const date = new Date(dateTimeString);
         // Check if the date object is valid
         if (isNaN(date.getTime())) {
           console.warn("Could not parse date string:", dateTimeString);
           return 'Ugyldig dato'; // Return specific error message
         }
         return date.toLocaleDateString('nb-NO', { 
            year: 'numeric', month: 'long', day: 'numeric',
            hour: '2-digit', minute: '2-digit'
         });
       } catch (e) { 
         console.error("Error formatting date:", dateTimeString, e);
         // Fallback for other unexpected errors during formatting
         return dateTimeString; 
       }
    };

    const openEditForm = () => {
      if (reflection.value && canManageReflection(reflection.value)) {
        reflectionToEdit.value = { ...reflection.value }; // Clone for editing
        isEditing.value = true;
      }
    };

    const cancelEdit = () => {
      isEditing.value = false;
      reflectionToEdit.value = null;
    };

    // Handle external closing of the dialog (e.g., clicking outside)
    const cancelEditIfNotOpen = (openState: boolean) => {
      if (!openState) {
        cancelEdit();
      }
    };

    const handleReflectionUpdated = () => {
      cancelEdit();
      refetch(); 
      if (reflection.value?.eventId) {
        queryClient.invalidateQueries({ queryKey: getGetReflectionsByEventIdQueryKey(reflection.value.eventId) });
      }
      queryClient.invalidateQueries({ queryKey: getGetCurrentUserReflectionsQueryKey() });
    };

    const confirmDelete = async () => {
      if (!reflection.value || !reflection.value.id || !canManageReflection(reflection.value)) return;
      if (window.confirm('Er du sikker på at du vil slette denne refleksjonen?')) {
        try {
          await deleteReflectionMutation.mutateAsync({ id: reflection.value.id });
           if (reflection.value?.eventId) {
             queryClient.invalidateQueries({ queryKey: getGetReflectionsByEventIdQueryKey(reflection.value.eventId) });
           }
           queryClient.invalidateQueries({ queryKey: getGetCurrentUserReflectionsQueryKey() });
          goBack();
        } catch (err) {
           console.error("Feil ved sletting:", err);
           alert("Kunne ikke slette: " + (err instanceof Error ? err.message : 'Ukjent feil'));
        }
      }
    };

    return {
      reflection,
      isLoading,
      error,
      errorMessage,
      goBack,
      canManageReflection,
      mapReflectionVisibility,
      formatDate,
      isEditing,
      reflectionToEdit,
      openEditForm,
      cancelEdit,
      handleReflectionUpdated,
      confirmDelete,
      cancelEditIfNotOpen,
      associatedEvent,
      isEventLoading,
    };
  },
});
</script>

<style scoped>
/* Scoped styles similar to ScenarioDetailView */
.reflection-content {
  line-height: 1.6;
}

.reflection-content ol, .reflection-content ul {
  list-style-position: inside;
  padding-left: 1em;
  margin: 1em 0;
}

.reflection-content li {
  margin: 0.5em 0;
}

/* Ensure prose styles work well within scoped styles if needed */
:deep(.prose) {
  /* Add overrides if necessary */
}

/* Add gentle animation to the icon */
@keyframes pulse-gentle {
  0% { transform: scale(1); }
  50% { transform: scale(1.05); }
  100% { transform: scale(1); }
}

.icon-wrapper {
  animation: pulse-gentle 3s infinite ease-in-out;
}
</style> 