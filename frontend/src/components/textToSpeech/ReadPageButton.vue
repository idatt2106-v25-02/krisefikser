<script setup lang="ts">
import { useAccessibilityStore } from '@/stores/tts/accessibilityStore';
import speechService from '@/services/tts/speechService';
import { onMounted, onUnmounted, ref } from 'vue';

const store = useAccessibilityStore();
const isReading = ref(false);

const readPageContent = () => {
  // If TTS is not enabled, don't do anything
  if (!store.ttsEnabled) {
    return;
  }

  // If already reading, stop the reading
  if (isReading.value) {
    speechService.cancel();
    isReading.value = false;
    return;
  }

  // Get the main content area
  const mainContent = document.querySelector('#main-content');
  if (!mainContent) return;

  // Get all text content, excluding buttons and navigation
  const textContent = Array.from(mainContent.querySelectorAll('p, h1, h2, h3, h4, h5, h6, li, td, th'))
    .map(element => {
      // Skip if element is hidden or has aria-hidden
      if (element.getAttribute('aria-hidden') === 'true' ||
        window.getComputedStyle(element).display === 'none') {
        return '';
      }
      return element.textContent?.trim() || '';
    })
    .filter(text => text.length > 0)
    .join('. ');

  if (textContent) {
    isReading.value = true;
    speechService.speak(textContent);

    // Listen for the end of speech
    const checkSpeechEnd = setInterval(() => {
      if (!window.speechSynthesis.speaking) {
        isReading.value = false;
        clearInterval(checkSpeechEnd);
      }
    }, 100);
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
  // Add listener for the readPage custom event
  document.addEventListener('readPage', readPageContent);
});

onUnmounted(() => {
  // Remove listener for the readPage custom event
  document.removeEventListener('readPage', readPageContent);
});
</script>

<template>
  <div class="fixed bottom-20 right-4 z-50">
    <button
      v-if="store.ttsEnabled"
      @click="readPageContent"
      @touchstart="handleTouchStart"
      @touchend="handleTouchEnd"
      class="bg-blue-500 hover:bg-blue-600 text-white p-4 rounded-full shadow-lg focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2 group relative transition-all min-h-[48px] min-w-[48px] flex items-center justify-center sm:bottom-20 sm:right-4 bottom-20 right-4"
      :class="{ 'animate-[pulse_2s_cubic-bezier(0.4,0,0.6,1)_infinite]': isReading }"
      :aria-label="isReading ? 'Stopp lesing (Ctrl + Shift + X)' : 'Les innholdet på siden (Ctrl + Shift + X)'">
      <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 11a7 7 0 01-7 7m0 0a7 7 0 01-7-7m7 7v4m0 0H8m4 0h4m-4-8a3 3 0 01-3-3V5a3 3 0 116 0v6a3 3 0 01-3 3z" />
      </svg>
      <!-- Tooltip -->
      <span class="absolute bottom-full right-0 mb-2 px-3 py-2 bg-gray-900 text-white text-sm rounded opacity-0 group-hover:opacity-100 transition-opacity whitespace-nowrap">
        {{ isReading ? 'Stopp lesing (Ctrl + Shift + X)' : 'Les innholdet på siden (Ctrl + Shift + X)' }}
      </span>
    </button>
  </div>
</template>
