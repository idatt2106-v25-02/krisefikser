<script lang="ts">
import { defineComponent, computed, ref } from 'vue';
import { useGetAllEvents } from '@/api/generated/event/event.ts';
import { useGetAllScenarios } from '@/api/generated/scenario/scenario.ts';
import type { EventResponse, ScenarioResponse } from '@/api/generated/model';
import { EventResponseStatus } from '@/api/generated/model';
import { Button as BaseButton} from '@/components/ui/button';
import { BookText } from 'lucide-vue-next';
import { useRouter } from 'vue-router';

export default defineComponent({
  name: 'KriserPage',
  components: {BaseButton, BookText },
  setup() {
    const { data: events, isLoading: isLoadingEvents, error: eventsError } = useGetAllEvents<EventResponse[]>();
    const { data: scenarios, isLoading: isLoadingScenarios, error: scenariosError } = useGetAllScenarios<ScenarioResponse[]>(); // Fetch scenarios
    const activeTab = ref('all');
    const router = useRouter();

    const navigateToPublicReflections = () => {
      router.push('/refleksjoner/offentlige');
    };

    const mapEventStatus = (status?: EventResponseStatus): string => {
      if (!status) return 'Ukjent';
      switch (status) {
        case EventResponseStatus.UPCOMING:
          return 'Kommende';
        case EventResponseStatus.ONGOING:
          return 'Pågående';
        case EventResponseStatus.FINISHED:
          return 'Avsluttet';
        default:
          const exhaustiveCheck: never = status;
          console.warn(`Ukjent hendelsesstatus mottatt: ${exhaustiveCheck}`);
          return 'Ukjent status';
      }
    };

    const eventsErrorMessage = computed(() => {
      if (eventsError.value instanceof Error) {
        return eventsError.value.message;
      }
      return 'En ukjent feil oppstod ved lasting av hendelser.';
    });

    const scenariosErrorMessage = computed(() => {
      if (scenariosError.value instanceof Error) {
        return scenariosError.value.message;
      }
      return 'En ukjent feil oppstod ved lasting av scenarioer.';
    });

    const allEvents = computed(() => {
      return events.value || [];
    });

    const upcomingEvents = computed(() => {
      return events.value?.filter(event => event.status === EventResponseStatus.UPCOMING) || [];
    });

    const ongoingEvents = computed(() => {
      return events.value?.filter(event => event.status === EventResponseStatus.ONGOING) || [];
    });

    const finishedEvents = computed(() => {
      return events.value?.filter(event => event.status === EventResponseStatus.FINISHED) || [];
    });

    const filteredEvents = computed(() => {
      switch (activeTab.value) {
        case 'all':
          return allEvents.value;
        case 'ongoing':
          return ongoingEvents.value;
        case 'upcoming':
          return upcomingEvents.value;
        case 'finished':
          return finishedEvents.value;
        default:
          return allEvents.value;
      }
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
        console.error("Error formatting date:", dateTimeString, e);
        return dateTimeString; // fallback
      }
    };

    // Utility to strip HTML for preview
    const stripHtml = (html?: string) => {
      if (!html) return '';
      const doc = new DOMParser().parseFromString(html, 'text/html');
      return doc.body.textContent || "";
    };

    return {
      isLoadingEvents,
      eventsError,
      eventsErrorMessage,
      scenarios,
      isLoadingScenarios,
      scenariosError,
      scenariosErrorMessage,
      activeTab,
      allEvents,
      upcomingEvents,
      ongoingEvents,
      finishedEvents,
      filteredEvents,
      formatDate,
      mapEventStatus,
      navigateToPublicReflections, // Expose to template
      stripHtml,
    };
  },
});
</script>

<template>
  <div class="bg-gray-50 min-h-screen">
    <div class="max-w-9xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      <!-- Page Header -->
      <div class="bg-white rounded-lg shadow-sm border border-gray-200 p-6 mb-6">
        <div class="flex flex-col md:flex-row md:items-start md:justify-between">
          <div>
            <h1 class="text-3xl font-bold text-gray-900 mb-1">Kriser og hendelser</h1>
            <p class="text-gray-600 max-w-3xl">
              Oversikt over kommende, pågående og tidligere kriser. Hold deg oppdatert på situasjoner som kan påvirke ditt område.
              Legg inn dine refleksjoner på avsluttede hendelser og se hva andre har skrevet.
            </p>
          </div>
          <div class="mt-4 md:mt-0">
            <BaseButton variant="outline" @click="navigateToPublicReflections">
              <BookText class="h-4 w-4 mr-2" />
              Offentlige Refleksjoner
            </BaseButton>
          </div>
        </div>
      </div>

      <!-- Loading state -->
      <div v-if="isLoadingEvents" class="bg-white rounded-lg shadow-sm border border-gray-200 p-8 flex justify-center">
        <div class="flex flex-col items-center">
          <div class="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-500 mb-4"></div>
          <p class="text-gray-600">Laster inn kriser...</p>
        </div>
      </div>

      <!-- Error state -->
      <div v-else-if="eventsError" class="bg-white rounded-lg shadow-sm border border-gray-200 p-6">
        <div class="bg-red-50 border-l-4 border-red-500 p-4 rounded">
          <div class="flex">
            <div class="flex-shrink-0">
              <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 text-red-500" viewBox="0 0 20 20" fill="currentColor">
                <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.707 7.293a1 1 0 00-1.414 1.414L8.586 10l-1.293 1.293a1 1 0 101.414 1.414L10 11.414l1.293 1.293a1 1 0 001.414-1.414L11.414 10l1.293-1.293a1 1 0 00-1.414-1.414L10 8.586 8.707 7.293z" clip-rule="evenodd" />
              </svg>
            </div>
            <div class="ml-3">
              <p class="text-sm text-red-700">
                Kunne ikke laste kriser: {{ eventsErrorMessage }}
              </p>
            </div>
          </div>
        </div>
      </div>

      <!-- Main content area -->
      <div v-else class="grid grid-cols-1 lg:grid-cols-12 gap-6">
        <!-- Main content -->
        <div class="lg:col-span-8">
          <!-- Crisis Events Section -->
          <div class="bg-white rounded-lg shadow-sm border border-gray-200 mb-6">
            <!-- Tab navigation -->
            <div class="border-b">
              <div class="flex overflow-x-auto whitespace-nowrap scrollbar-hide">
                <button
                  @click="activeTab = 'all'"
                  :class="[
                    'py-4 px-6 font-medium transition-colors border-b-2 -mb-px flex-shrink-0',
                    activeTab === 'all'
                      ? 'border-blue-500 text-blue-700'
                      : 'border-transparent text-gray-500 hover:text-gray-700'
                  ]"
                >
                  Alle hendelser
                </button>
                <button
                  @click="activeTab = 'ongoing'"
                  :class="[
                    'py-4 px-6 font-medium transition-colors border-b-2 -mb-px flex-shrink-0',
                    activeTab === 'ongoing'
                      ? 'border-red-500 text-red-700'
                      : 'border-transparent text-gray-500 hover:text-gray-700'
                  ]"
                >
                  <div class="flex items-center">
                    <span class="relative flex h-2 w-2 mr-2" v-if="ongoingEvents.length">
                      <span class="animate-ping absolute inline-flex h-full w-full rounded-full bg-red-400 opacity-75"></span>
                      <span class="relative inline-flex rounded-full h-2 w-2 bg-red-500"></span>
                    </span>
                    Pågående
                  </div>
                </button>
                <button
                  @click="activeTab = 'upcoming'"
                  :class="[
                    'py-4 px-6 font-medium transition-colors border-b-2 -mb-px flex-shrink-0',
                    activeTab === 'upcoming'
                      ? 'border-blue-500 text-blue-700'
                      : 'border-transparent text-gray-500 hover:text-gray-700'
                  ]"
                >
                  Kommende
                </button>
                <button
                  @click="activeTab = 'finished'"
                  :class="[
                    'py-4 px-6 font-medium transition-colors border-b-2 -mb-px flex-shrink-0',
                    activeTab === 'finished'
                      ? 'border-gray-500 text-gray-700'
                      : 'border-transparent text-gray-500 hover:text-gray-700'
                  ]"
                >
                  Avsluttede
                </button>
              </div>
            </div>

            <!-- Event grid with scrolling -->
            <div class="p-6">
              <!-- Empty state -->
              <div v-if="filteredEvents.length === 0" class="text-center py-12">
                <svg xmlns="http://www.w3.org/2000/svg" class="h-12 w-12 mx-auto text-gray-400 mb-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
                </svg>
                <p class="text-gray-500">Ingen hendelser funnet i denne kategorien.</p>
              </div>

              <!-- Grid of events -->
              <div v-else class="space-y-4 max-h-[400px] overflow-y-auto pr-2">
                <router-link
                  v-for="event in filteredEvents"
                  :key="event.id"
                  :to="{ name: 'event-detail', params: { id: event.id } }"
                  class="block bg-white rounded-lg border hover:shadow-md transition overflow-hidden cursor-pointer"
                  :class="{
                    'border-l-4 border-red-500': event.status === 'ONGOING',
                    'border-l-4 border-blue-500': event.status === 'UPCOMING',
                    'border-l-4 border-gray-300': event.status === 'FINISHED'
                  }"
                >
                  <div class="p-4">
                    <div class="flex justify-between items-start">
                      <h3 class="text-lg font-semibold text-gray-800 mb-2">{{ event.title }}</h3>
                      <span
                        class="px-2.5 py-1 rounded-full text-xs font-medium"
                        :class="{
                          'bg-red-100 text-red-800': event.status === 'ONGOING',
                          'bg-blue-100 text-blue-800': event.status === 'UPCOMING',
                          'bg-gray-100 text-gray-800': event.status === 'FINISHED'
                        }"
                      >
                        {{ mapEventStatus(event.status) }}
                      </span>
                    </div>
                    <p v-if="event.description" class="text-gray-600 mb-3">{{ stripHtml(event.description) }}</p>
                    <div class="flex justify-between items-center mt-4 text-sm text-gray-500">
                      <div>
                        <p v-if="event.status === 'UPCOMING'">Starter: {{ formatDate(event.startTime) }}</p>
                        <p v-else-if="event.status === 'ONGOING'">Startet: {{ formatDate(event.startTime) }}</p>
                        <p v-else-if="event.status === 'FINISHED'">Avsluttet: {{ formatDate(event.endTime) }}</p>
                      </div>
                      <div class="flex items-center gap-2">
                        <router-link
                          v-if="event.status === 'FINISHED'"
                          :to="{ name: 'my-reflections' }"
                          class="inline-flex items-center px-2.5 py-1.5 text-xs font-medium text-blue-600 bg-blue-50 rounded-md hover:bg-blue-100 transition-colors"
                        >
                          <BookText class="h-3.5 w-3.5 mr-1" />
                          Legg til refleksjon
                        </router-link>
                        <span class="text-blue-600 font-medium flex items-center">
                          Se detaljer
                          <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4 ml-1" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7" />
                          </svg>
                        </span>
                      </div>
                    </div>
                  </div>
                </router-link>
              </div>
            </div>
          </div>

          <!-- Horizontal News Section -->
          <div class="bg-white rounded-lg shadow-sm border border-gray-200 mb-6">
            <div class="p-5 border-b flex justify-between items-center">
              <h2 class="text-xl font-semibold text-gray-800">Siste nyheter</h2>
              <router-link to="/nyheter" class="text-blue-600 font-medium hover:underline inline-flex items-center">
                Se alle nyheter
                <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4 ml-1" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7" />
                </svg>
              </router-link>
            </div>

            <div class="p-5">
              <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
                <router-link to="/nyheter" class="block p-4 border rounded-lg hover:shadow-sm transition">
                  <h3 class="font-medium text-gray-800 mb-1">Ny opplæringsplan for kriseberedskap</h3>
                  <p class="text-sm text-gray-600 mb-2 line-clamp-2">Myndighetene har lansert en ny opplæringsplan for hvordan innbyggere kan forberede seg på kriser.</p>
                  <p class="text-xs text-gray-500">23. april 2025</p>
                </router-link>

                <router-link to="/nyheter" class="block p-4 border rounded-lg hover:shadow-sm transition">
                  <h3 class="font-medium text-gray-800 mb-1">Varslingssystem oppgradert</h3>
                  <p class="text-sm text-gray-600 mb-2 line-clamp-2">Det nasjonale varslingssystemet for krisesituasjoner har blitt oppgradert for å gi raskere og mer presis informasjon.</p>
                  <p class="text-xs text-gray-500">18. april 2025</p>
                </router-link>

                <router-link to="/nyheter" class="block p-4 border rounded-lg hover:shadow-sm transition">
                  <h3 class="font-medium text-gray-800 mb-1">Øvelse avslørte sårbarheter</h3>
                  <p class="text-sm text-gray-600 mb-2 line-clamp-2">En nylig gjennomført beredskapsøvelse avslørte svakheter i kommunikasjonen mellom etater under krisesituasjoner.</p>
                  <p class="text-xs text-gray-500">10. april 2025</p>
                </router-link>
              </div>
            </div>
          </div>

          <!-- Resources Section - Horizontal Grid -->
          <div class="bg-white rounded-lg shadow-sm border border-gray-200">
            <div class="p-5 border-b">
              <h2 class="text-xl font-semibold text-gray-800">Ressurser for kriseberedskap</h2>
            </div>

            <div class="p-5">
              <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
                <router-link to="/info/for-krisen" class="block p-4 border rounded-lg hover:shadow-sm transition">
                  <div class="flex items-center mb-2">
                    <div class="h-10 w-10 bg-blue-100 rounded-full flex items-center justify-center mr-3">
                      <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 text-blue-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
                      </svg>
                    </div>
                    <h3 class="font-medium text-gray-800">Før krisen</h3>
                  </div>
                  <p class="text-sm text-gray-600">Hvordan forberede deg før en krise</p>
                </router-link>

                <router-link to="/info/under-krisen" class="block p-4 border rounded-lg hover:shadow-sm transition">
                  <div class="flex items-center mb-2">
                    <div class="h-10 w-10 bg-yellow-100 rounded-full flex items-center justify-center mr-3">
                      <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 text-yellow-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
                      </svg>
                    </div>
                    <h3 class="font-medium text-gray-800">Under krisen</h3>
                  </div>
                  <p class="text-sm text-gray-600">Hva du bør gjøre under en krisesituasjon</p>
                </router-link>

                <router-link to="/info/etter-krisen" class="block p-4 border rounded-lg hover:shadow-sm transition">
                  <div class="flex items-center mb-2">
                    <div class="h-10 w-10 bg-green-100 rounded-full flex items-center justify-center mr-3">
                      <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 text-green-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7" />
                      </svg>
                    </div>
                    <h3 class="font-medium text-gray-800">Etter krisen</h3>
                  </div>
                  <p class="text-sm text-gray-600">Gjenoppbygging og normalisering</p>
                </router-link>
              </div>
            </div>
          </div>
        </div>

        <!-- Sidebar content - Map -->
        <div class="lg:col-span-4 space-y-6">
          <!-- Map Widget -->
          <div class="bg-white rounded-lg shadow-sm border border-gray-200 overflow-hidden">
            <div class="p-5 border-b">
              <h2 class="text-xl font-semibold text-gray-800">Krisekart</h2>
            </div>
            <div class="aspect-square bg-gray-100 relative">
              <!-- Map placeholder - In a real app, this would be your map component -->
              <div class="absolute inset-0 bg-gray-200">
                <!-- Map rendering would go here - this is just a placeholder -->
                <div class="h-full w-full flex items-center justify-center">
                  <router-link to="/kart" class="text-blue-600 font-medium flex flex-col items-center">
                    <svg xmlns="http://www.w3.org/2000/svg" class="h-12 w-12 mb-2 text-blue-500" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 20l-5.447-2.724A1 1 0 013 16.382V5.618a1 1 0 011.447-.894L9 7m0 13l6-3m-6 3V7m6 10l4.553 2.276A1 1 0 0021 18.382V7.618a1 1 0 00-.553-.894L15 4m0 13V4m0 0L9 7" />
                    </svg>
                    Se fullstendig kart
                  </router-link>
                </div>
              </div>
            </div>
            <div class="p-4 border-t">
              <router-link to="/kart" class="text-blue-600 font-medium hover:underline inline-flex items-center w-full justify-center">
                Åpne detaljert kart
                <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4 ml-1" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7" />
                </svg>
              </router-link>
            </div>
          </div>

          <!-- Scenarios Preview -->
          <div class="bg-white rounded-lg shadow-sm border border-gray-200">
            <div class="p-5 border-b flex justify-between items-center">
              <h2 class="text-xl font-semibold text-gray-800">Krisescenarioer</h2>
              <router-link to="/scenarioer" class="text-blue-600 font-medium hover:underline inline-flex items-center">
                Se alle
                <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4 ml-1" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7" />
                </svg>
              </router-link>
            </div>
            <div class="divide-y divide-gray-100">
              <!-- Loading state for scenarios -->
              <div v-if="isLoadingScenarios" class="p-4 text-center text-gray-500">
                Laster scenarioer...
              </div>
              <!-- Error state for scenarios -->
              <div v-else-if="scenariosError" class="p-4 text-center text-red-500">
                <p>Kunne ikke laste scenarioer: {{ scenariosErrorMessage }}</p>
              </div>
              <!-- Display scenarios -->
              <div v-else-if="scenarios && scenarios.length > 0">
                <router-link
                  v-for="scenario in scenarios.slice(0, 3)"
                  :key="scenario.id"
                  :to="{ name: 'scenario-detail', params: { id: scenario.id } }"
                  class="block p-4 hover:bg-gray-50 transition-colors"
                >
                  <h3 class="font-medium text-gray-800 mb-1">{{ scenario.title }}</h3>
                  <p class="text-sm text-gray-600 line-clamp-2" v-if="scenario.content">{{ stripHtml(scenario.content) }}</p>
                </router-link>
                <div v-if="scenarios.length === 0 && !isLoadingScenarios && !scenariosError" class="p-4 text-center text-gray-500">
                  Ingen scenarioer funnet.
                </div>
              </div>
               <div v-else class="p-4 text-center text-gray-500">
                Ingen scenarioer tilgjengelig for øyeblikket.
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
/* Custom scrollbar styling */
.overflow-y-auto {
  scrollbar-width: thin;
  scrollbar-color: rgba(59, 130, 246, 0.5) rgba(219, 234, 254, 0.7);
}

.overflow-y-auto::-webkit-scrollbar {
  width: 8px;
}

.overflow-y-auto::-webkit-scrollbar-track {
  background: rgba(219, 234, 254, 0.7);
  border-radius: 10px;
}

.overflow-y-auto::-webkit-scrollbar-thumb {
  background-color: rgba(59, 130, 246, 0.5);
  border-radius: 10px;
  border: 2px solid rgba(219, 234, 254, 0.7);
}

.overflow-y-auto::-webkit-scrollbar-thumb:hover {
  background-color: rgba(59, 130, 246, 0.7);
}

.line-clamp-2 {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

/* Hide scrollbar for Chrome, Safari and Opera */
.scrollbar-hide::-webkit-scrollbar {
  display: none;
}

/* Hide scrollbar for IE, Edge and Firefox */
.scrollbar-hide {
  -ms-overflow-style: none;  /* IE and Edge */
  scrollbar-width: none;  /* Firefox */
}
</style>
