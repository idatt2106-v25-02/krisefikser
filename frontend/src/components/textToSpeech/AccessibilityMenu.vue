<script setup lang="ts">
import { ref, onMounted, watch, onUnmounted } from 'vue';
import { useAccessibilityStore } from '@/stores/accessibilityStore.ts';
import speechService from '../../services/speechService.ts';
import voiceCommandService from '@/services/voiceCommandService';

const store = useAccessibilityStore();
const isMenuOpen = ref(false);
const ttsEnabled = ref(store.ttsEnabled);
const speechRate = ref(store.speechRate);
const selectedVoice = ref(store.selectedVoice);
const voices = ref(store.voices);
const isVoiceCommandsEnabled = ref(false);

const toggleMenu = () => {
  isMenuOpen.value = !isMenuOpen.value;

  if (isMenuOpen.value) {
    // Combine the heading and list items into a single string
    const menuContent = `
      Tilgjengelighetsalternativer.
      Fn + T for å aktivere/deaktivere tekst-til-tale.
      Fn + R for å skru av/på lesing av alt.
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

  // If TTS is being disabled, cancel any ongoing speech
  if (!store.ttsEnabled) {
    speechService.cancel();
  } else {
    // Test speech when enabled
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

const toggleVoiceCommands = () => {
  if (!voiceCommandService.isVoiceRecognitionSupported()) {
    speechService.speak('Stemmeoppkjenning er ikke støttet i denne nettleseren');
    return;
  }

  isVoiceCommandsEnabled.value = !isVoiceCommandsEnabled.value;
  if (isVoiceCommandsEnabled.value) {
    voiceCommandService.startListening();
  } else {
    voiceCommandService.stopListening();
  }
};

// Add touch feedback
const handleTouchStart = () => {
  if ('vibrate' in navigator) {
    navigator.vibrate(50); // 50ms vibration for touch feedback
  }
};

const handleTouchEnd = () => {
  if ('vibrate' in navigator) {
    navigator.vibrate(0); // Stop vibration
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

// Add cleanup on component unmount
onUnmounted(() => {
  if (isVoiceCommandsEnabled.value) {
    voiceCommandService.stopListening();
  }
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
      class="bg-gray-800 text-white p-3.5 rounded-full shadow-lg hover:bg-gray-700 focus:outline-none focus:ring-2 focus:ring-blue-500 min-h-[44px] min-w-[44px] flex items-center justify-center"
      aria-label="Tilgjengelighetsalternativer"
      @touchstart="handleTouchStart"
      @touchend="handleTouchEnd"
    >
     <svg xmlns="http://www.w3.org/2000/svg" class="h-7 w-7" fill="none" viewBox="0 0 24 24" stroke="currentColor">
       <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1" d="M11.25 3.018a.75.75 0 00-.53.22L6.72 7.238H4.5A2.25 2.25 0 002.25 9.488v5.025a2.25 2.25 0 002.25 2.25h2.22l4 4.001a.75.75 0 001.28-.53V3.748a.75.75 0 00-.75-.75zm6.72 3.03a.75.75 0 00-1.06 1.06 6.75 6.75 0 010 9.546.75.75 0 101.06 1.06 8.25 8.25 0 000-11.667zm-2.72 2.72a.75.75 0 00-1.06 1.06 3.75 3.75 0 010 5.304.75.75 0 101.06 1.06 5.25 5.25 0 000-7.425z" />
     </svg>
    </button>

    <div v-if="isMenuOpen" class="absolute bottom-16 right-0 bg-white rounded-lg shadow-xl p-4 w-85 max-h-[80vh] overflow-y-auto">
      <h3 class="font-medium text-gray-800 mb-3 text-xl">Tilgjengelighetsalternativer</h3>
      
      <ul class="text-base text-gray-600 mb-3 space-y-2">
        <li class="flex items-center">
          <kbd class="px-2 py-1 bg-gray-100 border border-gray-300 rounded text-sm">Fn</kbd>
          <span class="mx-1">+</span>
          <kbd class="px-2 py-1 bg-gray-100 border border-gray-300 rounded text-sm">T</kbd>
          <span class="ml-2">for å aktivere/deaktivere tekst-til-tale</span>
        </li>
        <li class="flex items-center">
          <kbd class="px-2 py-1 bg-gray-100 border border-gray-300 rounded text-sm">Fn</kbd>
          <span class="mx-1">+</span>
          <kbd class="px-2 py-1 bg-gray-100 border border-gray-300 rounded text-sm">R</kbd>
          <span class="ml-2">for å skru av/på lesing av alt</span>
        </li>
        <li class="flex items-center">
          <kbd class="px-2 py-1 bg-gray-100 border border-gray-300 rounded text-sm">↑ / ↓</kbd>
          <span class="ml-2">for å navigere frem og tilbake</span>
        </li>
        <li class="flex items-center">
          <kbd class="px-2 py-1 bg-gray-100 border border-gray-300 rounded text-sm">Alt/Option</kbd>
          <span class="mx-1">+</span>
          <kbd class="px-2 py-1 bg-gray-100 border border-gray-300 rounded text-sm">↑</kbd>
          <span class="mx-1">/</span>
          <kbd class="px-2 py-1 bg-gray-100 border border-gray-300 rounded text-sm">↓</kbd>
          <span class="ml-2">for å justere talehastighet</span>
        </li>
        <li class="flex items-center">
          <kbd class="px-2 py-1 bg-gray-100 border border-gray-300 rounded text-sm">1-4</kbd>
          <span class="ml-2">for å velge svar på quiz</span>
        </li>
      </ul>

      <!-- Voice commands section -->
      <div class="mb-3">
        <label class="flex items-center cursor-pointer">
          <input
            type="checkbox"
            class="form-checkbox h-5 w-5 text-blue-600"
            :checked="isVoiceCommandsEnabled"
            @change="toggleVoiceCommands" />
          <span class="ml-2 text-gray-700">Stemmeoppkjenning</span>
        </label>
        <p class="mt-1 text-sm text-gray-500">
          Støttede kommandoer:
          <br>- "Aktiver talesyntese"
          <br>- "Deaktiver talesyntese"
          <br>- "Les siden"
          <br>- "Stopp lesing"
          <br>- "Snakk raskere"
          <br>- "Snakk saktere"
        </p>
      </div>

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
            <span class="text-sm text-gray-500">Sakte</span>
            <input
              type="range"
              min="0.5"
              max="2"
              step="0.1"
              class="mx-2 flex-grow h-6"
              v-model="speechRate"
              @change="updateSpeechRate" />
            <span class="text-sm text-gray-500">Rask</span>
          </div>
        </div>

        <div>
          <label class="block text-sm text-gray-700 mb-1">Stemme</label>
          <select
            v-model="selectedVoice"
            @change="updateVoice"
            class="block w-full px-2 py-1.5 text-sm border border-gray-300 rounded-md">
            <option v-for="voice in voices" :key="voice.name" :value="voice">
              {{ voice.name }} ({{ voice.lang }})
            </option>
          </select>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
/* Mobile-specific styles */
@media (max-width: 640px) {
  .w-85 {
    width: calc(100vw - 2rem);
    max-width: 400px;
  }
}
</style>
