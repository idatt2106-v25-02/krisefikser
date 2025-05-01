// src/plugins/accessibility.ts
import type { App } from 'vue'; // Changed to type-only import
import { focusManager } from '../services/focusManager';
import { useAccessibilityStore } from '../stores/accessibilityStore';
import speechService from '../services/speechService';
import { onUnmounted } from 'vue';

// Define a proper type for the binding
interface SpeakDirectiveBinding {
  value?: string;
  modifiers: Record<string, boolean>;
}

interface ElementWithCleanup extends HTMLElement {
  _cleanup?: () => void;
}

export default {
  install: (app: App) => {
    // Make services available globally
    app.config.globalProperties.$speech = speechService;

    // Initialize focus manager
    focusManager.initialize();

    // Setup global keyboard shortcuts for accessibility
    const handleKeydown = (event: KeyboardEvent) => {
      console.log('Key pressed:', {
        key: event.key,
        altKey: event.altKey,
        metaKey: event.metaKey,
        ctrlKey: event.ctrlKey,
        shiftKey: event.shiftKey
      });

      // Check for both 't' and the special character '†' that Option+T generates on macOS
      if ((event.altKey || event.metaKey) && (event.key.toLowerCase() === 't' || event.key === '†')) {
        event.preventDefault();
        console.log('Accessibility shortcut detected: Alt/Option + T');

        const store = useAccessibilityStore();
        store.toggleTTS();

        const message = store.ttsEnabled
          ? 'Tekst-til-tale er nå aktivert'
          : 'Tekst-til-tale er nå deaktivert';

        console.log('Text-to-speech toggled:', store.ttsEnabled);
        speechService.speak(message);
      }
    };

    // Add global event listener
    document.addEventListener('keydown', handleKeydown);

    // Clean up listener when app is destroyed
    app.unmount = () => {
      document.removeEventListener('keydown', handleKeydown);
    };

    // Create custom directive for specific speech requirements
    app.directive('speak', {
      mounted(el: ElementWithCleanup, binding: SpeakDirectiveBinding) {
        const value = binding.value;
        const modifiers = binding.modifiers;

        // Make element focusable if it isn't already
        if (!el.hasAttribute('tabindex')) {
          el.setAttribute('tabindex', '0');
        }

        // Decide what text to read
        const getText = () => {
          if (typeof value === 'string') return value;
          return el.getAttribute('aria-label') || el.textContent?.trim() || '';
        };

        // Add event listener for manual reading
        const handleClick = () => {
          const store = useAccessibilityStore();
          if (store.ttsEnabled) {
            speechService.speak(getText());
          }
        };
        el.addEventListener('click', handleClick);

        // Add speech button if requested
        if (modifiers.button) {
          const button = document.createElement('button');
          button.innerHTML = `<span class="sr-only">Read aloud</span>
                             <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" viewBox="0 0 20 20" fill="currentColor">
                               <path fill-rule="evenodd" d="M9.383 3.076A1 1 0 0110 4v12a1 1 0 01-1.707.707L4.586 13H2a1 1 0 01-1-1V8a1 1 0 011-1h2.586l3.707-3.707a1 1 0 011.09-.217zM14.657 2.929a1 1 0 011.414 0A9.972 9.972 0 0119 10a9.972 9.972 0 01-2.929 7.071 1 1 0 01-1.414-1.414A7.971 7.971 0 0017 10c0-2.21-.894-4.208-2.343-5.657a1 1 0 010-1.414zm-2.829 2.828a1 1 0 011.415 0A5.983 5.983 0 0115 10a5.984 5.984 0 01-1.757 4.243 1 1 0 01-1.415-1.415A3.984 3.984 0 0013 10a3.983 3.983 0 00-1.172-2.828a1 1 0 010-1.415z" clip-rule="evenodd" />
                             </svg>`;
          button.className = 'ml-2 p-1 text-gray-500 hover:text-gray-700 focus:outline-none focus:ring-2 focus:ring-blue-500 rounded-full';
          button.addEventListener('click', (e) => {
            e.stopPropagation();
            speechService.speak(getText());
          });

          // Add button if parent position is not static
          const position = window.getComputedStyle(el).position;
          if (position === 'static') {
            el.style.position = 'relative';
          }

          el.appendChild(button);
        }

        // Cleanup on unmount
        el._cleanup = () => {
          el.removeEventListener('click', handleClick);
        }
      },
      unmounted(el: ElementWithCleanup) {
        if (el._cleanup) {
          el._cleanup();
        }
      }
    });
  }
};
