<script setup lang="ts">
import { ref, onMounted, watch } from 'vue';
import { useAccessibilityStore } from '../stores/accessibilityStore';
import speechService from '../services/speechService';

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

  // Test speech when enabled
  if (store.ttsEnabled) {
    // Speak a test message in Norwegian
    speechService.speak('Talesyntese er nå aktivert');
  }
};

const updateSpeechRate = () => {
  store.setSpeechRate(speechRate.value);
};

const updateVoice = () => {
  if (selectedVoice.value) {
    store.setSelectedVoice(selectedVoice.value);

    // Test the selected voice
    speechService.speak('Dette er en test av den valgte stemmen');
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
</script>

<template>
  <div class="fixed bottom-4 right-4 z-50 accessibility-menu">
    <button
      @click="toggleMenu"
      class="bg-gray-800 text-white p-2 rounded-full shadow-lg hover:bg-gray-700 focus:outline-none focus:ring-2 focus:ring-blue-500"
      aria-label="Tilgjengelighetsalternativer">
      <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2" />
      </svg>
    </button>

    <div v-if="isMenuOpen" class="absolute bottom-12 right-0 bg-white rounded-lg shadow-xl p-4 w-64">
      <h3 class="font-medium text-gray-800 mb-3">Tilgjengelighetsalternativer</h3>

      <div class="mb-3">
        <label class="flex items-center cursor-pointer">
          <input
            type="checkbox"
            class="form-checkbox h-5 w-5 text-blue-600"
            v-model="ttsEnabled"
            @change="toggleTTS" />
          <span class="ml-2 text-gray-700">Tekst-til-tale</span>
        </label>
      </div>

      <div v-if="ttsEnabled" class="space-y-3">
        <div>
          <label class="block text-sm text-gray-700 mb-1">Talehastighet</label>
          <div class="flex items-center">
            <span class="text-xs text-gray-500">Sakte</span>
            <input
              type="range"
              min="0.5"
              max="2"
              step="0.1"
              class="mx-2 flex-grow"
              v-model="speechRate"
              @change="updateSpeechRate" />
            <span class="text-xs text-gray-500">Rask</span>
          </div>
        </div>

        <div>
          <label class="block text-sm text-gray-700 mb-1">Stemme</label>
          <select
            v-model="selectedVoice"
            @change="updateVoice"
            class="block w-full px-3 py-1.5 text-sm border border-gray-300 rounded-md">
            <option v-for="voice in voices" :key="voice.name" :value="voice">
              {{ voice.name }} ({{ voice.lang }})
            </option>
          </select>
        </div>

        <button
          @click="speechService.speak('Dette er en test av tekst-til-tale funksjonaliteten')"
          class="mt-2 w-full bg-blue-500 hover:bg-blue-600 text-white py-1 px-3 rounded text-sm">
          Test tekst-til-tale
        </button>
      </div>
    </div>
  </div>
</template>
