<template>
  <div class="container mx-auto px-4 py-8">
    <h1 class="text-2xl font-bold text-gray-900 mb-6">Varsler Demo</h1>

    <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
      <!-- Notification System Overview -->
      <div class="bg-white p-6 rounded-lg shadow-md">
        <h2 class="text-xl font-semibold mb-4">Varslingsystemet</h2>
        <p class="text-gray-700 mb-4">
          Dette demoet viser hvordan varslingsystemet fungerer i Krisefikser.app. Bruk knappene under for å simulere forskjellige varsler.
        </p>

        <div class="space-y-3">
          <div>
            <h3 class="font-medium text-gray-800 mb-1">Varsler implementert:</h3>
            <ul class="list-disc list-inside text-gray-600 text-sm">
              <li>Varsler om kriseoppdateringer</li>
              <li>Varsler om kriser i nærheten</li>
              <li>Varsler om utløpsdatoer på beredskapslager</li>
              <li>Deling av varsler med husstanden</li>
            </ul>
          </div>

          <div class="pt-2">
            <h3 class="font-medium text-gray-800 mb-1">Hvor varsler vises:</h3>
            <ul class="list-disc list-inside text-gray-600 text-sm">
              <li>Varselsikon i navigasjonslinjen</li>
              <li>Varselsmoduler for viktige hendelser</li>
              <li>Dedikert varselsside</li>
            </ul>
          </div>
        </div>
      </div>

      <!-- Notification Triggers -->
      <div class="bg-white p-6 rounded-lg shadow-md">
        <h2 class="text-xl font-semibold mb-4">Test Varsler</h2>
        <p class="text-gray-700 mb-4">
          Trykk på en knapp for å simulere et nytt varsel.
        </p>

        <div class="space-y-3">
          <button
            @click="triggerCrisisNotification"
            class="w-full bg-red-600 hover:bg-red-700 text-white py-2 px-4 rounded-md flex items-center justify-center"
          >
            <AlertTriangle class="h-5 w-5 mr-2" />
            <span>Krise i nærheten</span>
          </button>

          <button
            @click="triggerExpiryNotification"
            class="w-full bg-yellow-600 hover:bg-yellow-700 text-white py-2 px-4 rounded-md flex items-center justify-center"
          >
            <Calendar class="h-5 w-5 mr-2" />
            <span>Utløpsdato på beredskapslager</span>
          </button>

          <button
            @click="triggerUpdateNotification"
            class="w-full bg-blue-600 hover:bg-blue-700 text-white py-2 px-4 rounded-md flex items-center justify-center"
          >
            <Bell class="h-5 w-5 mr-2" />
            <span>Oppdatering på krise</span>
          </button>

          <button
            @click="showModalNotification"
            class="w-full bg-green-600 hover:bg-green-700 text-white py-2 px-4 rounded-md flex items-center justify-center"
          >
            <AlertOctagon class="h-5 w-5 mr-2" />
            <span>Vis viktig varselmodul</span>
          </button>
        </div>
      </div>

      <!-- Current Notifications -->
      <div class="md:col-span-2 bg-white p-6 rounded-lg shadow-md">
        <div class="flex justify-between items-center mb-4">
          <h2 class="text-xl font-semibold">Aktive Varsler</h2>
          <div>
            <button
              @click="refreshNotifications"
              class="text-blue-600 hover:text-blue-800 mr-4"
            >
              <RefreshCw class="h-5 w-5" />
            </button>
            <button
              v-if="notificationStore.notifications.length > 0"
              @click="notificationStore.markAllAsRead"
              class="text-blue-600 hover:text-blue-800"
            >
              <Check class="h-5 w-5" />
            </button>
          </div>
        </div>

        <!-- Sample notifications display -->
        <div class="space-y-3">
          <div v-if="notificationStore.loading" class="text-center py-4">
            <div class="inline-block animate-spin rounded-full h-5 w-5 border-b-2 border-blue-600"></div>
            <span class="ml-2 text-gray-500">Laster varsler...</span>
          </div>

          <div v-else-if="notificationStore.notifications.length === 0" class="text-center py-8 bg-gray-50 rounded-md">
            <Bell class="mx-auto h-8 w-8 text-gray-400" />
            <p class="mt-2 text-gray-500">Ingen aktive varsler</p>
          </div>

          <div
            v-for="notification in notificationStore.sortedNotifications"
            :key="notification.id"
            class="bg-gray-50 p-3 rounded-md border-l-4 flex items-start"
            :class="{
              'border-red-500': notification.type === 'crisis',
              'border-yellow-500': notification.type === 'expiry',
              'border-blue-500': notification.type === 'update',
              'bg-white': notification.read,
              'bg-blue-50': !notification.read
            }"
          >
            <div
              class="rounded-full p-2 mr-3"
              :class="{
                'bg-red-100 text-red-600': notification.type === 'crisis',
                'bg-yellow-100 text-yellow-600': notification.type === 'expiry',
                'bg-blue-100 text-blue-600': notification.type === 'update'
              }"
            >
              <AlertTriangle v-if="notification.type === 'crisis'" class="h-4 w-4" />
              <Calendar v-else-if="notification.type === 'expiry'" class="h-4 w-4" />
              <Bell v-else class="h-4 w-4" />
            </div>
            <div class="flex-grow">
              <div class="flex justify-between">
                <h3 class="font-medium text-gray-900">{{ notification.title }}</h3>
                <span class="text-xs text-gray-500">{{ formatDate(notification.createdAt) }}</span>
              </div>
              <p class="text-sm text-gray-600 mt-1">{{ notification.message }}</p>
            </div>
          </div>
        </div>

        <div class="mt-4">
          <router-link to="/varsler" class="text-blue-600 hover:text-blue-800 text-sm flex items-center">
            <ArrowRight class="h-4 w-4 mr-1" />
            <span>Se alle varsler</span>
          </router-link>
        </div>
      </div>
    </div>

    <!-- Notification Modal Component -->
    <NotificationModal
      v-model:show="showModal"
      :type="modalData.type"
      :title="modalData.title"
      :message="modalData.message"
      :action-route="modalData.actionRoute"
      :action-text="modalData.actionText"
      @close="showModal = false"
    />
  </div>
</template>

<script lang="ts">
import { defineComponent, ref, reactive } from 'vue';
import { AlertTriangle, Calendar, Bell, Check, RefreshCw, ArrowRight, AlertOctagon } from 'lucide-vue-next';
import { useNotificationStore } from '@/stores/notification/useNotificationStore';
import NotificationModal from '@/components/notification/NotificationModal.vue';

export default defineComponent({
  name: 'NotificationsDemoPage',
  components: {
    AlertTriangle,
    Calendar,
    Bell,
    Check,
    RefreshCw,
    ArrowRight,
    AlertOctagon,
    NotificationModal
  },
  setup() {
    const notificationStore = useNotificationStore();
    const showModal = ref(false);
    const modalData = reactive({
      type: 'crisis',
      title: '',
      message: '',
      actionRoute: '',
      actionText: 'Se detaljer'
    });

    // Refresh notifications from the store
    const refreshNotifications = () => {
      notificationStore.fetchNotifications();
    };

    // Format the date for display
    const formatDate = (date: string) => {
      const now = new Date();
      const notificationDate = new Date(date);

      // Calculate the difference in days
      const diffInDays = Math.floor((now.getTime() - notificationDate.getTime()) / (1000 * 60 * 60 * 24));

      if (diffInDays === 0) {
        // If today, show the time
        return `I dag, ${notificationDate.getHours().toString().padStart(2, '0')}:${notificationDate.getMinutes().toString().padStart(2, '0')}`;
      } else if (diffInDays === 1) {
        return 'I går';
      } else if (diffInDays < 7) {
        const days = ['Søndag', 'Mandag', 'Tirsdag', 'Onsdag', 'Torsdag', 'Fredag', 'Lørdag'];
        return days[notificationDate.getDay()];
      } else {
        // For older notifications, show the date
        return `${notificationDate.getDate().toString().padStart(2, '0')}.${(notificationDate.getMonth() + 1).toString().padStart(2, '0')}.${notificationDate.getFullYear()}`;
      }
    };

    // Trigger a new crisis notification
    const triggerCrisisNotification = () => {
      const notification = {
        type: 'crisis' as const,
        title: 'Flomvarsel i ditt område',
        message: 'Det er utstedt nytt flomvarsel for Trondheim kommune. Vær forberedt på mulige evakueringer.',
        read: false,
        referenceId: 'crisis-' + Date.now()
      };

      notificationStore.addNotification(notification);
    };

    // Trigger a new expiry notification
    const triggerExpiryNotification = () => {
      const notification = {
        type: 'expiry' as const,
        title: 'Matvarer utløper snart',
        message: 'Noen varer i ditt beredskapslager utløper innen 7 dager. Vennligst sjekk og oppdater.',
        read: false,
        householdId: '123'
      };

      notificationStore.addNotification(notification);
    };

    // Trigger a new update notification
    const triggerUpdateNotification = () => {
      const notification = {
        type: 'update' as const,
        title: 'Oppdatering på krisesituasjon',
        message: 'Krisen i ditt område har blitt nedgradert. Myndighetene har nå kontroll på situasjonen.',
        read: false,
        referenceId: 'update-' + Date.now()
      };

      notificationStore.addNotification(notification);
    };

    // Show an important modal notification
    const showModalNotification = () => {
      modalData.type = 'crisis';
      modalData.title = 'Viktig varsel: Ekstremvær';
      modalData.message = 'Meteorologisk institutt har utstedt rødt farevarsel for Trøndelag. Kraftig nedbør og vind ventes de neste 24 timene. Hold deg innendørs og følg med på oppdateringer fra lokale myndigheter.';
      modalData.actionRoute = '/kart?crisis=modal-demo';
      modalData.actionText = 'Se på kartet';
      showModal.value = true;
    };

    return {
      notificationStore,
      showModal,
      modalData,
      refreshNotifications,
      formatDate,
      triggerCrisisNotification,
      triggerExpiryNotification,
      triggerUpdateNotification,
      showModalNotification
    };
  }
});
</script>
