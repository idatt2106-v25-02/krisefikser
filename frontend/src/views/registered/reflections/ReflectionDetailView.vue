<script lang="ts">
import { computed, defineComponent, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useQueryClient } from '@tanstack/vue-query'
import {
  getGetCurrentUserReflectionsQueryKey,
  getGetReflectionsByEventIdQueryKey,
  useDeleteReflection,
  useGetReflectionById,
} from '@/api/generated/reflection/reflection.ts'
import { useGetEventById } from '@/api/generated/event/event.ts'
import type { ReflectionResponse } from '@/api/generated/model'
import { ReflectionResponseVisibility } from '@/api/generated/model'
import { useAuthStore } from '@/stores/auth/useAuthStore.ts'
import { ArrowLeft, BookText } from 'lucide-vue-next'
import { Button as BaseButton } from '@/components/ui/button'
import { ConfirmationDialog } from '@/components/ui/confirmation-dialog'
import ReflectionForm from '@/components/reflections/ReflectionForm.vue'
import {
  Dialog as BaseDialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
} from '@/components/ui/dialog'

export default defineComponent({
  name: 'ReflectionDetailView',
  components: {
    ArrowLeft,
    BookText,
    BaseButton,
    ReflectionForm,
    BaseDialog,
    DialogContent,
    DialogDescription,
    DialogHeader,
    DialogTitle,
    ConfirmationDialog,
  },
  setup() {
    const route = useRoute()
    const router = useRouter()
    const queryClient = useQueryClient()
    const authStore = useAuthStore()
    const reflectionId = computed(() => route.params.id as string)

    const isEditing = ref(false)
    const reflectionToEdit = ref<ReflectionResponse | null>(null)
    const showDeleteDialog = ref(false)

    const { data: reflection, isLoading, error, refetch } = useGetReflectionById(reflectionId)

    // Create a separate ref for the associated Event ID
    const associatedEventId = ref<number | undefined>(undefined)

    // Watch the reflection data to update the associatedEventId ref
    watch(
      reflection,
      (newReflection) => {
        if (newReflection?.eventId !== undefined) {
          associatedEventId.value = newReflection.eventId
        } else {
          associatedEventId.value = undefined
        }
      },
      { immediate: true },
    )

    const {
      data: associatedEvent,
      isLoading: isEventLoading,
      error: eventError,
    } = useGetEventById(
      computed(() => associatedEventId.value as number),
      {
        query: {
          // Enable only when associatedEventId has a number value
          enabled: computed(() => typeof associatedEventId.value === 'number'),
        },
      },
    )

    const deleteReflectionMutation = useDeleteReflection()

    const errorMessage = computed(() => {
      if (error.value instanceof Error) return error.value.message
      if (eventError.value instanceof Error) return eventError.value.message
      return error.value ? 'En ukjent feil oppstod.' : ''
    })

    const goBack = () => {
      router.push({ name: 'my-reflections' })
    }

    const canManageReflection = (reflectionData: ReflectionResponse | null | undefined) => {
      if (!reflectionData || !authStore.currentUser) return false
      return authStore.isAdmin || authStore.currentUser.id === reflectionData.authorId
    }

    const mapReflectionVisibility = (visibility?: ReflectionResponseVisibility): string => {
      if (!visibility) return 'Ukjent'
      switch (visibility) {
        case ReflectionResponseVisibility.PUBLIC:
          return 'Offentlig'
        case ReflectionResponseVisibility.HOUSEHOLD:
          return 'Husstand'
        case ReflectionResponseVisibility.PRIVATE:
          return 'Privat'
        default:
          return 'Ukjent synlighet'
      }
    }

    const formatDate = (dateTimeString?: string | null): string => {
      if (!dateTimeString) return 'Ukjent dato'
      try {
        const date = new Date(dateTimeString)
        // Check if the date object is valid
        if (isNaN(date.getTime())) {
          console.warn('Could not parse date string:', dateTimeString)
          return 'Ugyldig dato' // Return specific error message
        }
        return date.toLocaleDateString('nb-NO', {
          year: 'numeric',
          month: 'long',
          day: 'numeric',
          hour: '2-digit',
          minute: '2-digit',
        })
      } catch (e) {
        console.error('Error formatting date:', dateTimeString, e)
        // Fallback for other unexpected errors during formatting
        return dateTimeString
      }
    }

    const openEditForm = () => {
      if (reflection.value && canManageReflection(reflection.value)) {
        reflectionToEdit.value = { ...reflection.value } // Clone for editing
        isEditing.value = true
      }
    }

    const cancelEdit = () => {
      isEditing.value = false
      reflectionToEdit.value = null
    }

    const handleReflectionUpdated = () => {
      cancelEdit()
      refetch()
      if (reflection.value?.eventId) {
        queryClient.invalidateQueries({
          queryKey: getGetReflectionsByEventIdQueryKey(reflection.value.eventId),
        })
      }
      queryClient.invalidateQueries({ queryKey: getGetCurrentUserReflectionsQueryKey() })
    }

    const confirmDelete = () => {
      if (!reflection.value || !reflection.value.id || !canManageReflection(reflection.value))
        return
      showDeleteDialog.value = true
    }

    const handleDeleteReflection = async () => {
      if (!reflection.value || !reflection.value.id || !canManageReflection(reflection.value))
        return
      try {
        await deleteReflectionMutation.mutateAsync({ id: reflection.value.id })
        if (reflection.value?.eventId) {
          queryClient.invalidateQueries({
            queryKey: getGetReflectionsByEventIdQueryKey(reflection.value.eventId),
          })
        }
        queryClient.invalidateQueries({ queryKey: getGetCurrentUserReflectionsQueryKey() })
        showDeleteDialog.value = false
        goBack()
      } catch (err) {
        console.error('Feil ved sletting:', err)
        alert('Kunne ikke slette: ' + (err instanceof Error ? err.message : 'Ukjent feil'))
      }
    }

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
      showDeleteDialog,
      confirmDelete,
      handleDeleteReflection,
      associatedEvent,
      isEventLoading,
    }
  },
})
</script>

<template>
  <div class="container mx-auto px-4 py-12 max-w-4xl">
    <!-- Background decoration - wave pattern -->
    <div class="absolute top-0 left-0 w-full h-64 bg-blue-50 -z-10 overflow-hidden">
      <div class="absolute bottom-0 left-0 right-0">
        <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 1440 320" class="w-full h-auto">
          <path
            fill="#ffffff"
            fill-opacity="1"
            d="M0,128L48,144C96,160,192,192,288,186.7C384,181,480,139,576,138.7C672,139,768,181,864,170.7C960,160,1056,96,1152,85.3C1248,75,1344,117,1392,138.7L1440,160L1440,320L1392,320C1344,320,1248,320,1152,320C1056,320,960,320,864,320C768,320,672,320,576,320C480,320,384,320,288,320C192,320,96,320,48,320L0,320Z"
          ></path>
        </svg>
      </div>
    </div>

    <div class="flex items-center mb-6">
      <button
        class="text-blue-600 hover:text-blue-800 transition-colors font-medium flex items-center group"
        @click="goBack"
      >
        <ArrowLeft class="h-5 w-5 mr-2 group-hover:translate-x-[-3px] transition-transform" />
        <span>Tilbake til Mine Refleksjoner</span>
      </button>
    </div>

    <!-- Loading state with improved animation -->
    <div
      v-if="isLoading"
      class="bg-white rounded-lg shadow-lg p-8 flex flex-col items-center justify-center py-16"
    >
      <div
        class="inline-block animate-spin rounded-full h-12 w-12 border-4 border-b-blue-500 border-blue-100 mb-4"
      ></div>
      <p class="text-gray-600 font-medium">Laster refleksjon...</p>
    </div>

    <!-- Error state with better visual cues -->
    <div v-else-if="error" class="bg-white rounded-lg shadow-lg p-8 border-l-4 border-red-500">
      <div class="flex items-start">
        <div class="flex-shrink-0 mt-0.5">
          <svg
            xmlns="http://www.w3.org/2000/svg"
            class="h-6 w-6 text-red-500"
            fill="none"
            viewBox="0 0 24 24"
            stroke="currentColor"
          >
            <path
              stroke-linecap="round"
              stroke-linejoin="round"
              stroke-width="2"
              d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z"
            />
          </svg>
        </div>
        <div class="ml-3">
          <h3 class="text-lg font-medium text-red-800">Kunne ikke laste refleksjon</h3>
          <p class="mt-2 text-red-700">{{ errorMessage }}</p>
        </div>
      </div>
    </div>

    <!-- Content with enhanced styling -->
    <div v-else-if="reflection" class="bg-white rounded-lg shadow-lg p-8 relative">
      <!-- Decorative element -->
      <div
        class="absolute top-0 right-0 w-32 h-32 bg-blue-50/70 rounded-bl-full -mt-2 -mr-2 overflow-hidden z-0 icon-corner"
      >
        <div class="absolute top-5 right-5 icon-wrapper">
          <BookText class="h-10 w-10 text-blue-500 icon" />
        </div>
      </div>

      <!-- Reflection content with improved layout -->
      <div class="relative z-10">
        <h1
          class="text-2xl sm:text-3xl font-bold text-gray-800 pb-4 border-b border-gray-200 w-full mb-4"
        >
          {{ reflection.title }}
        </h1>

        <!-- Metadata with badges for better visual hierarchy -->
        <div class="flex flex-wrap gap-2 mb-6 text-sm text-gray-500">
          <span
            class="inline-flex items-center px-2.5 py-0.5 rounded-full bg-blue-100 text-blue-800"
          >
            <svg
              xmlns="http://www.w3.org/2000/svg"
              class="h-3.5 w-3.5 mr-1"
              fill="none"
              viewBox="0 0 24 24"
              stroke="currentColor"
            >
              <path
                stroke-linecap="round"
                stroke-linejoin="round"
                stroke-width="2"
                d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z"
              />
            </svg>
            {{ reflection.authorName }}
          </span>
          <span
            class="inline-flex items-center px-2.5 py-0.5 rounded-full bg-green-100 text-green-800"
          >
            <svg
              xmlns="http://www.w3.org/2000/svg"
              class="h-3.5 w-3.5 mr-1"
              fill="none"
              viewBox="0 0 24 24"
              stroke="currentColor"
            >
              <path
                stroke-linecap="round"
                stroke-linejoin="round"
                stroke-width="2"
                d="M15 12a3 3 0 11-6 0 3 3 0 016 0z"
              />
              <path
                stroke-linecap="round"
                stroke-linejoin="round"
                stroke-width="2"
                d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z"
              />
            </svg>
            {{ mapReflectionVisibility(reflection.visibility) }}
          </span>
          <span
            class="inline-flex items-center px-2.5 py-0.5 rounded-full bg-gray-100 text-gray-800"
          >
            <svg
              xmlns="http://www.w3.org/2000/svg"
              class="h-3.5 w-3.5 mr-1"
              fill="none"
              viewBox="0 0 24 24"
              stroke="currentColor"
            >
              <path
                stroke-linecap="round"
                stroke-linejoin="round"
                stroke-width="2"
                d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z"
              />
            </svg>
            {{ formatDate(reflection.updatedAt) }}
          </span>

          <!-- Link to Event with badge styling -->
          <router-link
            v-if="reflection.eventId"
            :to="{ name: 'event-detail', params: { id: reflection.eventId } }"
            class="inline-flex items-center px-2.5 py-0.5 rounded-full bg-purple-100 text-purple-800 hover:bg-purple-200 transition-colors"
          >
            <svg
              xmlns="http://www.w3.org/2000/svg"
              class="h-3.5 w-3.5 mr-1"
              fill="none"
              viewBox="0 0 24 24"
              stroke="currentColor"
            >
              <path
                stroke-linecap="round"
                stroke-linejoin="round"
                stroke-width="2"
                d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"
              />
            </svg>
            <span v-if="isEventLoading">Laster hendelse...</span>
            <span v-else-if="associatedEvent?.title">{{ associatedEvent.title }}</span>
            <span v-else>Hendelse #{{ reflection.eventId }}</span>
          </router-link>
        </div>

        <!-- Main content with better text styling -->
        <div
          class="text-gray-700 leading-relaxed reflection-content prose prose-blue max-w-none mt-8"
          v-html="reflection.content"
        ></div>

        <!-- Edit/Delete Buttons moved to the bottom -->
        <div
          v-if="canManageReflection(reflection)"
          class="flex justify-end space-x-3 mt-8 pt-4 border-t"
        >
          <BaseButton
            size="sm"
            variant="outline"
            class="flex items-center gap-1.5"
            @click="openEditForm"
          >
            <svg
              xmlns="http://www.w3.org/2000/svg"
              class="h-4 w-4"
              fill="none"
              viewBox="0 0 24 24"
              stroke="currentColor"
            >
              <path
                stroke-linecap="round"
                stroke-linejoin="round"
                stroke-width="2"
                d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z"
              />
            </svg>
            <span>Rediger</span>
          </BaseButton>
          <BaseButton
            size="sm"
            variant="destructive"
            class="flex items-center gap-1.5"
            @click="confirmDelete"
          >
            <svg
              xmlns="http://www.w3.org/2000/svg"
              class="h-4 w-4"
              fill="none"
              viewBox="0 0 24 24"
              stroke="currentColor"
            >
              <path
                stroke-linecap="round"
                stroke-linejoin="round"
                stroke-width="2"
                d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16"
              />
            </svg>
            <span>Slett</span>
          </BaseButton>
        </div>
      </div>
    </div>

    <!-- Empty state -->
    <div
      v-else
      class="bg-white rounded-lg shadow-lg p-8 flex flex-col items-center justify-center py-16"
    >
      <svg
        xmlns="http://www.w3.org/2000/svg"
        class="h-16 w-16 text-gray-400 mb-4"
        fill="none"
        viewBox="0 0 24 24"
        stroke="currentColor"
      >
        <path
          stroke-linecap="round"
          stroke-linejoin="round"
          stroke-width="2"
          d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z"
        />
      </svg>
      <p class="text-gray-600 text-center text-lg">Refleksjon ikke funnet.</p>
      <p class="text-gray-500 text-center mt-2">Den kan ha blitt slettet eller flyttet.</p>
      <BaseButton class="mt-4" @click="goBack">Tilbake til oversikten</BaseButton>
    </div>

    <!-- Edit Modal/Form with improved styling -->
    <BaseDialog :open="isEditing">
      <DialogContent class="sm:max-w-[600px] p-0 overflow-hidden">
        <DialogHeader class="p-6 pb-2">
          <DialogTitle class="text-xl">Rediger Refleksjon</DialogTitle>
          <DialogDescription> Gjør endringer i refleksjonen din her. </DialogDescription>
        </DialogHeader>
        <div class="p-6 pt-0">
          <ReflectionForm
            v-if="isEditing && reflectionToEdit"
            :key="reflectionToEdit.id"
            :event-id="reflectionToEdit.eventId!"
            :initial-data="reflectionToEdit"
            @updated="handleReflectionUpdated"
            @cancel="cancelEdit"
            class="pt-4"
          />
        </div>
      </DialogContent>
    </BaseDialog>

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
</template>

<style scoped>
.reflection-content {
  line-height: 1.6;
}

.reflection-content ol,
.reflection-content ul {
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
  0% {
    transform: scale(1);
  }
  50% {
    transform: scale(1.05);
  }
  100% {
    transform: scale(1);
  }
}

.icon-wrapper {
  animation: pulse-gentle 3s infinite ease-in-out;
}
</style>
