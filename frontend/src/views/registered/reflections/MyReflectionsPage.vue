<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useQueryClient, useQueries } from '@tanstack/vue-query'
import { useAuthStore } from '@/stores/auth/useAuthStore'
import {
  useGetCurrentUserReflections,
  useDeleteReflection,
  getGetCurrentUserReflectionsQueryKey,
} from '@/api/generated/reflection/reflection'
import {
  useGetAllEvents,
  getEventById
} from '@/api/generated/event/event'
import type { ReflectionResponse, EventResponse } from '@/api/generated/model'
import { ReflectionResponseVisibility, EventResponseStatus } from '@/api/generated/model'
import { Button } from '@/components/ui/button'
import { BookOpen, Eye, Trash2, PlusCircle, Search } from 'lucide-vue-next'
import ReflectionForm from '@/components/reflections/ReflectionForm.vue'
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
} from '@/components/ui/dialog'
import { Input } from '@/components/ui/input'

const router = useRouter()
const queryClient = useQueryClient()
const authStore = useAuthStore()

// current user reflections
const {
  data: reflections,
  isLoading: isLoadingReflections,
  error: reflectionsError,
  refetch: refetchMyReflections,
} = useGetCurrentUserReflections<ReflectionResponse[]>({
  query: {
    enabled: authStore.isAuthenticated,
    refetchOnWindowFocus: true,
    staleTime: 30000,
  },
})

const deleteReflectionMutation = useDeleteReflection({
  mutation: {
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: getGetCurrentUserReflectionsQueryKey() })
    },
    onError: (err: Error | unknown) => {
      console.error('Feil ved sletting av refleksjon:', err)
      alert('Kunne ikke slette refleksjon: ' + (err instanceof Error ? err.message : 'Ukjent feil'))
    },
  },
})

const reflectionsErrorMessage = computed(() => {
  const e = reflectionsError.value as Error | null
  if (e instanceof Error) return e.message
  return e ? 'En ukjent feil oppstod.' : ''
})

const formatDate = (dateInput?: string | number[] | null) => {
  if (!dateInput) return 'Ukjent dato'
  if (Array.isArray(dateInput)) {
    const [year, month, day, hour, minute, second, nanosecond] = dateInput
    const ms = Math.floor((nanosecond || 0) / 1e6)
    const date = new Date(year, month - 1, day, hour, minute, second, ms)
    if (isNaN(date.getTime())) return 'Ukjent dato'
    return date.toLocaleDateString('nb-NO', {
      year: 'numeric',
      month: 'long',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    })
  }
  const date = new Date(dateInput)
  if (isNaN(date.getTime())) return 'Ukjent dato'
  return date.toLocaleDateString('nb-NO', {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  })
}

const mapReflectionVisibility = (v?: ReflectionResponseVisibility) => {
  if (!v) return 'Ukjent'
  switch (v) {
    case ReflectionResponseVisibility.PUBLIC:
      return 'Offentlig'
    case ReflectionResponseVisibility.HOUSEHOLD:
      return 'Husstand'
    case ReflectionResponseVisibility.PRIVATE:
      return 'Privat'
  }
  return 'Ukjent'
}

const stripHtml = (html?: string) => {
  if (!html) return ''
  const doc = new DOMParser().parseFromString(html, 'text/html')
  return doc.body.textContent || ''
}

const canManageReflection = (r: ReflectionResponse) => {
  if (!authStore.currentUser) return false
  return authStore.isAdmin || authStore.currentUser.id === r.authorId
}

const viewReflection = (r: ReflectionResponse) => {
  router.push(`/refleksjon/${r.id}`)
}

const confirmDeleteReflection = async (id: string) => {
  if (
    window.confirm(
      'Er du sikker på at du vil slette denne refleksjonen? Handlingen kan ikke angres.'
    )
  ) {
    await deleteReflectionMutation.mutateAsync({ id })
  }
}

// new reflection flow
const isEventSelectDialogOpen = ref(false)
const isReflectionFormDialogOpen = ref(false)
const selectedEventIdForNewReflection = ref<number | null>(null)
const eventSearchTerm = ref('')

const {
  data: allEvents,
  isLoading: isLoadingEvents,
  error: eventsError,
} = useGetAllEvents<EventResponse[]>({
  query: {
    enabled: computed(() => isEventSelectDialogOpen.value),
    refetchOnWindowFocus: false,
  },
})

const finishedEventsForSelection = computed(() => {
  if (!allEvents.value) return []
  return allEvents.value
    .filter((e) => e.status === EventResponseStatus.FINISHED)
    .sort((a, b) => new Date(b.endTime || 0).getTime() - new Date(a.endTime || 0).getTime())
})

const filteredFinishedEventsForSelection = computed(() => {
  if (!finishedEventsForSelection.value) return []
  if (!eventSearchTerm.value) return finishedEventsForSelection.value
  return finishedEventsForSelection.value.filter((e) =>
    e.title?.toLowerCase().includes(eventSearchTerm.value.toLowerCase())
  )
})

const eventsErrorMessage = computed(() => {
  const e = eventsError.value as Error | null
  if (e instanceof Error) return e.message
  return e ? 'Kunne ikke laste hendelser.' : ''
})

const openEventSelectDialog = () => {
  eventSearchTerm.value = ''
  selectedEventIdForNewReflection.value = null
  isEventSelectDialogOpen.value = true
}
const handleEventSelected = (event: EventResponse) => {
  if (event.id === undefined) return
  selectedEventIdForNewReflection.value = event.id
  isEventSelectDialogOpen.value = false
  isReflectionFormDialogOpen.value = true
}
const handleNewReflectionCreated = () => {
  isReflectionFormDialogOpen.value = false
  selectedEventIdForNewReflection.value = null
  refetchMyReflections()
}
const cancelNewReflectionForm = () => {
  isReflectionFormDialogOpen.value = false
  selectedEventIdForNewReflection.value = null
}
const closeEventSelectDialog = () => {
  isEventSelectDialogOpen.value = false
}

// fetch titles via useQueries in setup
const reflectionEventIdsToFetch = computed(() => {
  const ids = new Set<number>()
  reflections.value?.forEach((r) => {
    if (r.eventId) ids.add(r.eventId)
  })
  return Array.from(ids)
})

const eventQueries = useQueries({
  queries: computed(() =>
    reflectionEventIdsToFetch.value.map((id) => ({
      queryKey: ['getEventById', id],
      queryFn: () => getEventById(id),
      enabled: true,
    }))
  )
})

const eventTitles = computed(() => {
  const map: Record<number, string> = {}
  const results = eventQueries.value
  results?.forEach((q, index) => {
    const id = reflectionEventIdsToFetch.value[index]
    if (q.data?.title) {
      map[id] = q.data.title
    } else {
      map[id] = 'Hendelse utilgjengelig'
    }
  })
  return map
})

onMounted(() => {
  if (!authStore.isAuthenticated) router.push('/logg-inn')
})
</script>

<template>
  <div class="bg-gray-50 min-h-screen">
    <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      <div class="bg-white rounded-lg shadow-sm border border-gray-200 p-6 mb-6">
        <div class="flex items-center justify-between mb-4">
          <router-link to="/kriser" class="inline-flex items-center text-blue-600 hover:text-blue-800">
            ← Tilbake til kriser
          </router-link>
          <Button variant="outline" class="flex items-center" @click="openEventSelectDialog">
            <PlusCircle class="h-4 w-4 mr-2" />Ny refleksjon
          </Button>
        </div>
        <h1 class="text-3xl font-bold text-gray-900 mb-1">Mine Refleksjoner</h1>
        <p class="text-gray-600 max-w-3xl">
          Her finner du alle refleksjonene du har skrevet knyttet til ulike hendelser.
        </p>
      </div>

      <div v-if="isLoadingReflections" class="grid md:grid-cols-2 lg:grid-cols-3 gap-6">
        <div
          v-for="n in 3"
          :key="n"
          class="bg-white rounded-lg shadow-md overflow-hidden flex flex-col relative border transition-all duration-300 min-h-[220px] animate-pulse"
        >
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

      <div v-else-if="reflectionsError" class="bg-white rounded-lg shadow-lg p-6">
        <div class="bg-red-50 border-l-4 border-red-500 p-4 rounded">
          <p class="text-sm text-red-700">Kunne ikke laste refleksjoner: {{ reflectionsErrorMessage }}</p>
        </div>
      </div>

      <div v-else-if="reflections && reflections.length > 0" class="grid md:grid-cols-2 lg:grid-cols-3 gap-6">
        <div
          v-for="reflection in reflections"
          :key="reflection.id"
          class="bg-white rounded-lg shadow-md overflow-hidden flex flex-col relative border transition-all duration-300 hover:shadow-lg min-h-[220px]"
        >
          <div
            class="h-1 w-full"
            :class="{
              'bg-blue-500': reflection.visibility === ReflectionResponseVisibility.PRIVATE,
              'bg-green-500': reflection.visibility === ReflectionResponseVisibility.HOUSEHOLD,
              'bg-indigo-500': reflection.visibility === ReflectionResponseVisibility.PUBLIC
            }"
          ></div>
          <div class="absolute top-0 right-0 w-16 h-16 rounded-bl-full -mt-1 -mr-1 overflow-hidden z-0"
               :class="{
                 'bg-blue-50/70': reflection.visibility === ReflectionResponseVisibility.PRIVATE,
                 'bg-green-50/70': reflection.visibility === ReflectionResponseVisibility.HOUSEHOLD,
                 'bg-indigo-50/70': reflection.visibility === ReflectionResponseVisibility.PUBLIC
               }">
            <BookOpen
              class="h-6 w-6"
              :class="{
                'text-blue-600': reflection.visibility === ReflectionResponseVisibility.PRIVATE,
                'text-green-600': reflection.visibility === ReflectionResponseVisibility.HOUSEHOLD,
                'text-indigo-600': reflection.visibility === ReflectionResponseVisibility.PUBLIC
              }"/>
          </div>
          <div class="p-4 relative z-10 flex-grow flex flex-col">
            <h3 class="text-lg font-semibold mb-1 pr-10">{{ reflection.title }}</h3>
            <p class="text-xs text-gray-500 mb-2">
              <span class="px-2 py-0.5 rounded-full bg-gray-100">{{ mapReflectionVisibility(reflection.visibility) }}</span>
              <span class="px-2 py-0.5 rounded-full bg-gray-100">{{ formatDate(reflection.updatedAt) }}</span>
            </p>
            <div v-if="reflection.eventId" class="mb-2 text-sm text-gray-700">
              <span class="px-2 py-0.5 rounded-full bg-purple-100 text-purple-800 text-xs">
                <router-link :to="`/kriser/${reflection.eventId}`">{{ eventTitles[reflection.eventId] }}</router-link>
              </span>
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

      <div v-else class="bg-white rounded-lg shadow-md p-8 text-center">
        <BookOpen class="mx-auto h-12 w-12 text-gray-400" />
        <h3 class="mt-2 text-xl font-medium text-gray-900">Ingen refleksjoner</h3>
        <p class="mt-1 text-base text-gray-500">Du har ikke skrevet noen refleksjoner ennå.</p>
        <Button class="mt-6" @click="openEventSelectDialog">Skriv din første refleksjon</Button>
      </div>

      <!-- Event Selection Dialog -->
      <Dialog v-model:open="isEventSelectDialogOpen">
        <DialogContent class="sm:max-w-[600px]">
          <DialogHeader>
            <DialogTitle>Velg hendelse for ny refleksjon</DialogTitle>
            <DialogDescription>
              Velg en hendelse du ønsker å skrive en refleksjon om.
            </DialogDescription>
          </DialogHeader>
          <div class="relative mb-4">
            <Input
              type="text"
              placeholder="Søk etter hendelse..."
              v-model="eventSearchTerm"
              class="pl-10"
            />
            <Search class="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-gray-400"/>
            <Button v-if="eventSearchTerm" variant="ghost" size="icon" class="absolute right-2 top-1/2 -translate-y-1/2 h-7 w-7" @click="eventSearchTerm = ''">
              <Search class="h-4 w-4 rotate-45" />
            </Button>
          </div>
          <div v-if="isLoadingEvents" class="py-4 text-center text-gray-500">
            Laster hendelser...
          </div>
          <div v-else-if="eventsError" class="py-4 text-center text-red-500">
            {{ eventsErrorMessage }}
          </div>
          <div v-else-if="filteredFinishedEventsForSelection.length === 0" class="py-4 text-center text-gray-500">
            Ingen ferdige hendelser funnet.
          </div>
          <div v-else class="max-h-[400px] overflow-y-auto space-y-2">
            <button
              v-for="event in filteredFinishedEventsForSelection"
              :key="event.id"
              class="w-full text-left p-3 rounded-lg hover:bg-gray-100 transition-colors"
              @click="handleEventSelected(event)"
            >
              <h4 class="font-medium">{{ event.title }}</h4>
              <p class="text-sm text-gray-500">
                {{ formatDate(event.endTime) }}
              </p>
            </button>
          </div>
          <div class="flex justify-end mt-4">
            <Button variant="outline" @click="closeEventSelectDialog">Avbryt</Button>
          </div>
        </DialogContent>
      </Dialog>

      <!-- Reflection Form Dialog -->
      <Dialog v-model:open="isReflectionFormDialogOpen">
        <DialogContent class="sm:max-w-[800px]">
          <DialogHeader>
            <DialogTitle>Ny refleksjon</DialogTitle>
            <DialogDescription>
              Skriv en refleksjon om hendelsen du valgte.
            </DialogDescription>
          </DialogHeader>
          <ReflectionForm
            v-if="selectedEventIdForNewReflection !== null"
            :event-id="selectedEventIdForNewReflection"
            @created="handleNewReflectionCreated"
            @cancel="cancelNewReflectionForm"
          />
        </DialogContent>
      </Dialog>
    </div>
  </div>
</template>
