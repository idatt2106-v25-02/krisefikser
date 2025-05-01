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
      const module = await import('../stores/accessibilityStore');
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
          accessibilityStore.setVoice(savedVoice);
        }
      }
    });
  }

  speak(text: string, options?: SpeechOptions): void {
    // Make sure we're initialized
    this.initialize();

    if (!('speechSynthesis' in window) || !text) {
      return;
    }

    // We'll use a function to dynamically import the store
    const getStore = async () => {
      const module = await import('../stores/accessibilityStore');
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
      window.speechSynthesis.speak(utterance);
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
