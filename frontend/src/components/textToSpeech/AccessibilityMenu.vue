<script setup lang="ts">
import { ref, onMounted, watch } from 'vue';
import { useAccessibilityStore } from '@/stores/accessibilityStore.ts';
import speechService from '../../services/speechService.ts';

const store = useAccessibilityStore();
const isMenuOpen = ref(false);
const ttsEnabled = ref(store.ttsEnabled);
const speechRate = ref(store.speechRate);
const selectedVoice = ref(store.selectedVoice);
const voices = ref(store.voices);

const toggleMenu = () => {
  isMenuOpen.value = !isMenuOpen.value;

  if (isMenuOpen.value) {
    // Combine the heading and list items into a single string
    const menuContent = `
      Tilgjengelighetsalternativer.
      Alt/Option + T for å aktivere/deaktivere tekst-til-tale.
      Alt/Option + R for å skru av/på lesing av alt.
      Pil opp / Pil ned for å navigere frem og tilbake.
      Alt/Option + Pil opp / Pil ned for å justere talehastighet.
      1 til 4 for å velge svar på quiz.
    `;

    // Speak the menu content
    speechService.speak(menuContent.trim());
  }
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
      class="bg-gray-800 text-white p-3 rounded-full shadow-lg hover:bg-gray-700 focus:outline-none focus:ring-2 focus:ring-blue-500"
      aria-label="Tilgjengelighetsalternativer">
     <svg xmlns="http://www.w3.org/2000/svg" class="h-8 w-8" fill="none" viewBox="0 0 24 24" stroke="currentColor">
       <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1" d="M11.25 3.018a.75.75 0 00-.53.22L6.72 7.238H4.5A2.25 2.25 0 002.25 9.488v5.025a2.25 2.25 0 002.25 2.25h2.22l4 4.001a.75.75 0 001.28-.53V3.748a.75.75 0 00-.75-.75zm6.72 3.03a.75.75 0 00-1.06 1.06 6.75 6.75 0 010 9.546.75.75 0 101.06 1.06 8.25 8.25 0 000-11.667zm-2.72 2.72a.75.75 0 00-1.06 1.06 3.75 3.75 0 010 5.304.75.75 0 101.06 1.06 5.25 5.25 0 000-7.425z" />
     </svg>
    </button>

    <div v-if="isMenuOpen" class="absolute bottom-16 right-0 bg-white rounded-lg shadow-xl p-6 w-85">
      <h3 class="font-medium text-gray-800 mb-4 text-2xl">Tilgjengelighetsalternativer</h3>
      <ul class="text-lg text-gray-600 mb-4 space-y-2">
        <li><kbd class="px-2 py-1 bg-gray-100 border border-gray-300 rounded">Alt/Option</kbd> + <kbd class="px-2 py-1 bg-gray-100 border border-gray-300 rounded">T</kbd> for å aktivere/deaktivere tekst-til-tale</li>
        <li><kbd class="px-2 py-1 bg-gray-100 border border-gray-300 rounded">Alt/Option</kbd> + <kbd class="px-2 py-1 bg-gray-100 border border-gray-300 rounded">R</kbd> for å skru av/på lesing av alt</li>
        <li><kbd class="px-2 py-1 bg-gray-100 border border-gray-300 rounded">↑ / ↓</kbd> for å navigere frem og tilbake</li>
        <li><kbd class="px-2 py-1 bg-gray-100 border border-gray-300 rounded">Alt/Option</kbd> + <kbd class="px-2 py-1 bg-gray-100 border border-gray-300 rounded">↑</kbd> / <kbd class="px-2 py-1 bg-gray-100 border border-gray-300 rounded">↓</kbd> for å justere talehastighet</li>
        <li><kbd class="px-2 py-1 bg-gray-100 border border-gray-300 rounded">1-4</kbd> for å velge svar på quiz</li>
      </ul>

      <div class="mb-4">
        <label class="flex items-center cursor-pointer">
          <input
            type="checkbox"
            class="form-checkbox h-6 w-6 text-blue-600"
            v-model="ttsEnabled"
            @change="toggleTTS" />
          <span class="ml-3 text-gray-700 text-xl">Tekst-til-tale</span>
        </label>
      </div>

      <div v-if="ttsEnabled" class="space-y-4">
        <div>
          <label class="block text-xl text-gray-700 mb-2">Talehastighet</label>
          <div class="flex items-center">
            <span class="text-base text-gray-500">Sakte</span>
            <input
              type="range"
              min="0.5"
              max="2"
              step="0.1"
              class="mx-4 flex-grow"
              v-model="speechRate"
              @change="updateSpeechRate" />
            <span class="text-base text-gray-500">Rask</span>
          </div>
        </div>

        <div>
          <label class="block text-xl text-gray-700 mb-2">Stemme</label>
          <select
            v-model="selectedVoice"
            @change="updateVoice"
            class="block w-full px-4 py-2 text-lg border border-gray-300 rounded-md">
            <option v-for="voice in voices" :key="voice.name" :value="voice">
              {{ voice.name }} ({{ voice.lang }})
            </option>
          </select>
        </div>
      </div>
    </div>
  </div>
</template>
