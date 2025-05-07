// src/services/speechService.ts
export interface SpeechOptions {
  voice?: SpeechSynthesisVoice;
  rate?: number;
  pitch?: number;
  volume?: number;
}

class SpeechService {
  private currentUtterance: SpeechSynthesisUtterance | null = null;
  private initialized = false;

  constructor() {
    // Defer initialization to when the service is actually used
  }

  initialize(): void {
    if (this.initialized) return;
    this.initialized = true;

    // Initialize voices when available
    if (window.speechSynthesis) {
      if (speechSynthesis.getVoices().length > 0) {
        this.updateVoices();
      }

      speechSynthesis.onvoiceschanged = () => {
        this.updateVoices();
      };
    }
  }

  private updateVoices(): void {
    // We'll use a function to dynamically import the store
    const getStore = async () => {
      const module = await import('../../stores/tts/accessibilityStore.ts');
      return module.useAccessibilityStore();
    };

    getStore().then(accessibilityStore => {
      const voices = speechSynthesis.getVoices();
      accessibilityStore.setVoices(voices);

      // Restore voice if previously selected
      const savedVoiceName = localStorage.getItem('selectedVoiceName');
      if (savedVoiceName) {
        const savedVoice = voices.find(v => v.name === savedVoiceName);
        if (savedVoice) {
          accessibilityStore.setSelectedVoice(savedVoice);
        }
      }
    });
  }

  speak(text: string, options?: SpeechOptions): void {
    // Make sure we're initialized
    this.initialize();
    if (!('speechSynthesis' in window) || !text) {
      console.warn('Speech synthesis not supported or no text provided');
      return;
    }

    // We'll use a function to dynamically import the store
    const getStore = async () => {
      const module = await import('../../stores/tts/accessibilityStore.ts');
      return module.useAccessibilityStore();
    };

    getStore().then(accessibilityStore => {
      if (!accessibilityStore.ttsEnabled) {
        return;
      }

      // Cancel any existing speech
      this.cancel();

      const utterance = new SpeechSynthesisUtterance(text);

      // Apply store settings
      if (accessibilityStore.selectedVoice) {
        utterance.voice = accessibilityStore.selectedVoice;
      }
      utterance.rate = accessibilityStore.speechRate;

      // Override with any provided options
      if (options?.voice) utterance.voice = options.voice;
      if (options?.rate) utterance.rate = options.rate;
      if (options?.pitch) utterance.pitch = options.pitch;
      if (options?.volume) utterance.volume = options.volume;

      this.currentUtterance = utterance;

      // Handle Chrome's requirement for user interaction
      if (window.speechSynthesis.speaking) {
        window.speechSynthesis.cancel();
      }

      // Force a small delay to ensure Chrome is ready
      setTimeout(() => {
        window.speechSynthesis.speak(utterance);
      }, 50);
    });
  }

  cancel(): void {
    if (window.speechSynthesis) {
      window.speechSynthesis.cancel();
      this.currentUtterance = null;
    }
  }
}

// Create service instance but don't initialize it yet
export const speechService = new SpeechService();
export default speechService;
