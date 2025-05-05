<template>
  <Teleport to="body">
    <Transition name="modal-fade">
      <div v-if="isOpen" class="fixed inset-0 z-50 flex items-center justify-center">
        <div
          class="absolute inset-0 bg-black bg-opacity-50"
          @click="close"
        ></div>

        <div
          class="relative bg-white rounded-lg shadow-xl max-w-md w-full mx-4 overflow-hidden"
          :class="{
            'border-l-4 border-red-500': type === 'crisis',
            'border-l-4 border-yellow-500': type === 'expiry',
            'border-l-4 border-blue-500': type === 'update'
          }"
        >
          <!-- Header -->
          <div
            class="px-6 py-4 border-b flex items-center justify-between"
            :class="{
              'bg-red-50': type === 'crisis',
              'bg-yellow-50': type === 'expiry',
              'bg-blue-50': type === 'update'
            }"
          >
            <div class="flex items-center">
              <div
                class="rounded-full p-2 mr-3"
                :class="{
                  'bg-red-100 text-red-600': type === 'crisis',
                  'bg-yellow-100 text-yellow-600': type === 'expiry',
                  'bg-blue-100 text-blue-600': type === 'update'
                }"
              >
                <AlertTriangle v-if="type === 'crisis'" class="h-5 w-5" />
                <Calendar v-else-if="type === 'expiry'" class="h-5 w-5" />
                <Bell v-else class="h-5 w-5" />
              </div>
              <h3 class="font-medium text-lg">{{ title }}</h3>
            </div>
            <button
              @click="close"
              class="text-gray-400 hover:text-gray-600 focus:outline-none"
              aria-label="Lukk"
            >
              <X class="h-5 w-5" />
            </button>
          </div>

          <!-- Body -->
          <div class="p-6">
            <p class="text-gray-700">{{ message }}</p>

            <div class="mt-6">
              <div v-if="type === 'crisis'" class="bg-red-50 p-4 rounded-md">
                <div class="flex">
                  <div class="flex-shrink-0">
                    <AlertTriangle class="h-5 w-5 text-red-600" />
                  </div>
                  <div class="ml-3">
                    <h3 class="text-sm font-medium text-red-800">Viktig informasjon</h3>
                    <div class="mt-2 text-sm text-red-700">
                      <p>Hold deg oppdatert om situasjonen og følg råd fra lokale myndigheter.</p>
                    </div>
                  </div>
                </div>
              </div>

              <div v-else-if="type === 'expiry'" class="bg-yellow-50 p-4 rounded-md">
                <div class="flex">
                  <div class="flex-shrink-0">
                    <Calendar class="h-5 w-5 text-yellow-600" />
                  </div>
                  <div class="ml-3">
                    <h3 class="text-sm font-medium text-yellow-800">Påminnelse</h3>
                    <div class="mt-2 text-sm text-yellow-700">
                      <p>Sjekk ditt beredskapslager og oppdater utløpte varer for å holde deg forberedt.</p>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- Footer -->
          <div class="px-6 py-4 bg-gray-50 border-t flex justify-end space-x-3">
            <button
              @click="close"
              class="px-4 py-2 border border-gray-300 rounded-md text-gray-700 hover:bg-gray-100"
            >
              Lukk
            </button>

            <button
              v-if="actionRoute"
              @click="handleAction"
              class="px-4 py-2 rounded-md text-white"
              :class="{
                'bg-red-600 hover:bg-red-700': type === 'crisis',
                'bg-yellow-600 hover:bg-yellow-700': type === 'expiry',
                'bg-blue-600 hover:bg-blue-700': type === 'update'
              }"
            >
              {{ actionText }}
            </button>
          </div>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<script lang="ts">
import { defineComponent, ref, watch } from 'vue';
import { useRouter } from 'vue-router';
import { AlertTriangle, Calendar, Bell, X } from 'lucide-vue-next';

export default defineComponent({
  name: 'NotificationModal',
  components: {
    AlertTriangle,
    Calendar,
    Bell,
    X
  },
  props: {
    show: {
      type: Boolean,
      default: false
    },
    type: {
      type: String,
      default: 'update',
      validator: (value: string) => ['crisis', 'expiry', 'update'].includes(value)
    },
    title: {
      type: String,
      required: true
    },
    message: {
      type: String,
      required: true
    },
    actionRoute: {
      type: String,
      default: ''
    },
    actionText: {
      type: String,
      default: 'Se detaljer'
    },
    onClose: {
      type: Function,
      default: () => {}
    }
  },
  setup(props, { emit }) {
    const router = useRouter();
    const isOpen = ref(props.show);

    watch(() => props.show, (value) => {
      isOpen.value = value;
    });

    const close = () => {
      isOpen.value = false;
      emit('update:show', false);
      props.onClose();
    };

    const handleAction = () => {
      if (props.actionRoute) {
        router.push(props.actionRoute);
      }
      close();
    };

    return {
      isOpen,
      close,
      handleAction
    };
  }
});
</script>

<style scoped>
.modal-fade-enter-active,
.modal-fade-leave-active {
  transition: opacity 0.3s ease;
}

.modal-fade-enter-from,
.modal-fade-leave-to {
  opacity: 0;
}
</style>
