<!-- src/components/AccessibilityMenu.vue -->
<template>
  <div class="fixed bottom-4 right-4 z-50">
    <button
      @click="toggleMenu"
      class="bg-gray-800 text-white p-2 rounded-full shadow-lg hover:bg-gray-700 focus:outline-none focus:ring-2 focus:ring-blue-500"
      aria-label="Accessibility options">
      <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2" />
      </svg>
    </button>

    <div v-if="isMenuOpen" class="absolute bottom-12 right-0 bg-white rounded-lg shadow-xl p-4 w-64">
      <h3 class="font-medium text-gray-800 mb-3">Accessibility Options</h3>

      <div class="mb-3">
        <label class="flex items-center cursor-pointer">
          <input
            type="checkbox"
            class="form-checkbox h-5 w-5 text-blue-600"
            v-model="ttsEnabled"
            @change="toggleTTS" />
          <span class="ml-2 text-gray-700">Text-to-Speech</span>
        </label>
      </div>

      <div v-if="ttsEnabled" class="space-y-3">
        <div>
          <label class="block text-sm text-gray-700 mb-1">Speech Rate</label>
          <div class="flex items-center">
            <span class="text-xs text-gray-500">Slow</span>
            <input
              type="range"
              min="0.5"
              max="2"
              step="0.1"
              class="mx-2 flex-grow"
              v-model="speechRate"
              @change="updateSpeechRate" />
            <span class="text-xs text-gray-500">Fast</span>
          </div>
        </div>

        <div>
          <label class="block text-sm text-gray-700 mb-1">Voice</label>
          <select
            v-model="selectedVoice"
            @change="updateVoice"
            class="block w-full px-3 py-1.5 text-sm border border-gray-300 rounded-md">
            <option v-for="voice in voices" :key="voice.name" :value="voice">
              {{ voice.name }}
            </option>
          </select>
        </div>
      </div>
    </div>
  </div>
</template>

<script lang="ts">
import { defineComponent, ref, onMounted, watch } from 'vue';
import { useAccessibilityStore } from '../stores/accessibilityStore';
import speechService from '../services/speechService';

export default defineComponent({
  name: 'AccessibilityMenu',
  setup() {
    const store = useAccessibilityStore();
    const isMenuOpen = ref(false);
    const ttsEnabled = ref(store.ttsEnabled);
    const speechRate = ref(store.speechRate);
    const selectedVoice = ref(store.selectedVoice);
    const voices = ref(store.voices);

    const toggleMenu = () => {
      isMenuOpen.value = !isMenuOpen.value;
    };

    const toggleTTS = () => {
      store.toggleTTS();
    };

    const updateSpeechRate = () => {
      store.setSpeechRate(speechRate.value);
    };

    const updateVoice = () => {
      if (selectedVoice.value) {
        store.setVoice(selectedVoice.value);
      }
    };

    onMounted(() => {
      // Close menu when clicking outside
      document.addEventListener('click', (event) => {
        const target = event.target as HTMLElement;
        if (isMenuOpen.value && !target.closest('.accessibility-menu')) {
          isMenuOpen.value = false;
        }
      });

      // Announce when opened
      watch(isMenuOpen, (newVal) => {
        if (newVal && store.ttsEnabled) {
          speechService.speak('Accessibility menu opened');
        }
      });
    });

    // Keep local refs in sync with store
    watch(() => store.ttsEnabled, (newVal) => {
      ttsEnabled.value = newVal;
    });

    watch(() => store.speechRate, (newVal) => {
      speechRate.value = newVal;
    });

    watch(() => store.selectedVoice, (newVal) => {
      selectedVoice.value = newVal;
    });

    watch(() => store.voices, (newVal) => {
      voices.value = newVal;
    });

    return {
      isMenuOpen,
      ttsEnabled,
      speechRate,
      selectedVoice,
      voices,
      toggleMenu,
      toggleTTS,
      updateSpeechRate,
      updateVoice
    };
  }
});
</script>
