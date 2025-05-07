<template>
  <div class="container mx-auto p-4">
    <div class="flex justify-between items-center mb-6">
      <h1 class="text-3xl font-bold">Offentlige Refleksjoner</h1>
      <Button variant="outline" @click="goBackToKriser">
        <ArrowLeft class="h-4 w-4 mr-2" />
        Tilbake til Kriser
      </Button>
    </div>

    <div v-if="isLoading">Laster refleksjoner...</div>
    <div v-if="error">
      <p class="text-red-500">Kunne ikke laste refleksjoner: {{ errorMessage }}</p>
    </div>

    <div v-if="publicReflections && publicReflections.length > 0" class="grid md:grid-cols-2 lg:grid-cols-3 gap-6">
      <div
        v-for="reflection in publicReflections"
        :key="reflection.id"
        class="bg-white rounded-lg shadow-md overflow-hidden flex flex-col relative border transition-all duration-300 hover:shadow-lg min-h-[200px]"
      >
        <div class="absolute top-0 right-0 w-16 h-16 rounded-bl-full -mt-1 -mr-1 overflow-hidden z-0 bg-indigo-50/70">
          <div class="absolute top-2.5 right-2.5">
            <Globe class="h-6 w-6 text-indigo-600" /> <!-- Globe icon for public -->
          </div>
        </div>

        <div class="p-4 relative z-10 flex-grow flex flex-col">
          <h3 class="text-lg font-semibold mb-1">{{ reflection.title }}</h3>
          <p class="text-xs text-gray-500 mb-2">
            Forfatter: {{ reflection.authorName }} |
            Synlighet: {{ mapReflectionVisibility(reflection.visibility) }} |
            Oppdatert: {{ formatDate(reflection.updatedAt) }}
          </p>

          <div v-if="reflection.eventId && eventTitles[reflection.eventId]" class="mb-2 text-sm text-gray-700">
            Knyttet til hendelse:
            <router-link :to="`/kriser/${reflection.eventId}`" class="text-blue-600 hover:underline">
              {{ eventTitles[reflection.eventId] }}
            </router-link>
          </div>
          <div v-else-if="reflection.eventId" class="mb-2 text-sm text-gray-500 italic">
            Laster hendelsesnavn...
          </div>

          <p v-if="reflection.content" class="text-sm text-gray-600 flex-grow mb-3 line-clamp-3" v-html="stripHtml(reflection.content)"></p>

          <div class="mt-auto pt-3 border-t flex justify-end space-x-2">
            <Button size="sm" variant="outline" @click="viewReflection(reflection.id!)">Vis</Button>
            <Button v-if="canManageReflection(reflection)" size="sm" variant="outline" @click="editReflection(reflection.id!)">Rediger</Button>
            <Button v-if="canManageReflection(reflection)" size="sm" variant="destructive" @click="confirmDeleteReflection(reflection.id!)">Slett</Button>
          </div>
        </div>
      </div>
    </div>
    <div v-else-if="!isLoading && !error" class="text-center py-10">
      <BookOpen class="mx-auto h-12 w-12 text-gray-400" />
      <h3 class="mt-2 text-sm font-medium text-gray-900">Ingen offentlige refleksjoner</h3>
      <p class="mt-1 text-sm text-gray-500">Det er ingen offentlige refleksjoner tilgjengelig for øyeblikket.</p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue';
import { useRouter } from 'vue-router';
import { useQueryClient } from '@tanstack/vue-query';
import { useAuthStore } from '@/stores/auth/useAuthStore.ts';
import {
  useGetPublicReflections, // Using the hook for public reflections
  useDeleteReflection,
  getGetPublicReflectionsQueryKey // Query key for public reflections
} from '@/api/generated/reflection/reflection.ts';
import { useGetEventById } from '@/api/generated/event/event.ts';
import type { ReflectionResponse } from '@/api/generated/model';
import { ReflectionResponseVisibility } from '@/api/generated/model';
import { Button } from '@/components/ui/button';
import { ArrowLeft, BookOpen, Globe } from 'lucide-vue-next'; // Added Globe icon

const router = useRouter();
const queryClient = useQueryClient();
const authStore = useAuthStore(); // To check for admin/author for edit/delete

// Fetch public reflections
const { data: publicReflections, isLoading, error } = useGetPublicReflections<ReflectionResponse[]>({
  query: {
    // Public reflections don't strictly need auth, but keeping enabled pattern for consistency
    // Or set to true if the endpoint is always available
    enabled: true,
    refetchOnWindowFocus: true,
  }
});

const deleteReflectionMutation = useDeleteReflection({
  mutation: {
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: getGetPublicReflectionsQueryKey() });
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
  if (!authStore.currentUser) return false; // If user not logged in, cannot manage
  return authStore.isAdmin || authStore.currentUser.id === reflection.authorId;
};

const viewReflection = (id: string) => {
  router.push(`/refleksjon/${id}`);
};

const editReflection = (id: string) => {
  // Navigation to edit should still work, ReflectionDetailView will handle auth for form
  router.push(`/refleksjon/${id}?action=edit`);
};

const confirmDeleteReflection = async (id: string) => {
  if (!authStore.isAuthenticated) {
    alert("Du må være logget inn for å slette refleksjoner.");
    return;
  }
  if (window.confirm('Er du sikker på at du vil slette denne refleksjonen? Handlingen kan ikke angres.')) {
    try {
      await deleteReflectionMutation.mutateAsync({ id });
    } catch (err) {
      // Error handling is in mutation's onError
    }
  }
};

const goBackToKriser = () => {
  router.push('/kriser');
};

// Fetch event titles for associated events
const eventTitles = ref<Record<string, string>>({});
const eventIdsToFetch = computed(() => {
  const ids = new Set<number>();
  (publicReflections.value || []).forEach((reflection: ReflectionResponse) => {
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

// No onMounted auth check needed as this page is public
// onMounted(() => {
//   if (!authStore.isAuthenticated) {
//     router.push('/logg-inn');
//   }
// });

</script>

<style scoped>
.line-clamp-3 {
  overflow: hidden;
  display: -webkit-box;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 3;
}
</style>
