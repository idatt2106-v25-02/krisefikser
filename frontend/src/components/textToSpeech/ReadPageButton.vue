<script setup lang="ts">
import { useAccessibilityStore } from '@/stores/accessibilityStore.ts';
import speechService from '../../services/speechService.ts';
import { onMounted, onUnmounted } from 'vue';

const store = useAccessibilityStore();

const readPageContent = () => {
  if (!store.ttsEnabled) {
    store.toggleTTS();
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
    speechService.speak(textContent);
  }
};

// Handle keyboard shortcut
const handleKeydown = (event: KeyboardEvent) => {

  // Check for Option + R, including the "Dead" key that macOS generates
  if ((event.altKey || event.metaKey) &&
    (event.key.toLowerCase() === 'r' ||
      event.key === '®' ||
      event.key === 'Dead')) {
    event.preventDefault();
    console.log('Read page shortcut detected: Alt/Option + R');
    readPageContent();
  }
};

onMounted(() => {
  document.addEventListener('keydown', handleKeydown);
});

onUnmounted(() => {
  document.removeEventListener('keydown', handleKeydown);
});
</script>

<template>
  <div class="fixed bottom-20 right-5 z-50">
    <button
      v-if="store.ttsEnabled"
      @click="readPageContent"
      class="bg-blue-500 hover:bg-blue-600 text-white p-3 rounded-full shadow-lg focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2 group relative"
      aria-label="Les innholdet på siden (Alt/Option + R)"
    >
      <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 11a7 7 0 01-7 7m0 0a7 7 0 01-7-7m7 7v4m0 0H8m4 0h4m-4-8a3 3 0 01-3-3V5a3 3 0 116 0v6a3 3 0 01-3 3z" />
      </svg>
      <!-- Tooltip -->
      <span class="absolute bottom-full right-0 mb-2 px-2 py-1 bg-gray-900 text-white text-xs rounded opacity-0 group-hover:opacity-100 transition-opacity whitespace-nowrap">
        Les innholdet på siden (Alt/Option + R)
      </span>
    </button>
  </div>
</template>
