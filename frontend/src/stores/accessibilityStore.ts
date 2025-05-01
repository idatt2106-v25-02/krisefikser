// src/stores/accessibilityStore.ts
import { defineStore } from 'pinia';

interface AccessibilityState {
  ttsEnabled: boolean;
  speechRate: number;
  selectedVoice: SpeechSynthesisVoice | null;
  voices: SpeechSynthesisVoice[];
}

export const useAccessibilityStore = defineStore('accessibility', {
  state: (): AccessibilityState => ({
    ttsEnabled: localStorage.getItem('ttsEnabled') === 'true',
    speechRate: Number(localStorage.getItem('speechRate')) || 1,
    selectedVoice: null,
    voices: []
  }),

  actions: {
    toggleTTS() {
      this.ttsEnabled = !this.ttsEnabled;
      localStorage.setItem('ttsEnabled', String(this.ttsEnabled));
    },

    setVoices(voices: SpeechSynthesisVoice[]) {
      this.voices = voices;

      // Set default voice if not already set
      if (!this.selectedVoice && voices.length > 0) {
        // Try to find a Norwegian voice first - website is already in Norwegian
        const norwegianVoice = voices.find(v =>
          v.lang.toLowerCase().includes('no') ||
          v.lang.toLowerCase().includes('nb') ||
          v.lang.toLowerCase().includes('nn')
        );

        // If no Norwegian voice, try other Scandinavian languages
        const scandinavianVoice = !norwegianVoice ?
          voices.find(v =>
            v.lang.toLowerCase().includes('sv') ||
            v.lang.toLowerCase().includes('da')
          ) : null;

        // Fallback to browser language or first voice
        const userLangVoice = !norwegianVoice && !scandinavianVoice ?
          voices.find(v => v.lang.startsWith(navigator.language.split('-')[0])) : null;

        this.selectedVoice = norwegianVoice || scandinavianVoice || userLangVoice || voices[0];

        // Save the selected voice
        localStorage.setItem('selectedVoiceName', this.selectedVoice.name);
      }
    },

    setSpeechRate(rate: number) {
      this.speechRate = rate;
      localStorage.setItem('speechRate', String(rate));
    }
  }
});
