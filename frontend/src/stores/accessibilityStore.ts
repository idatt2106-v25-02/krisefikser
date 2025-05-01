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
        // Try to find a voice in user's language
        const userLang = navigator.language.split('-')[0];
        const matchingVoice = voices.find(v => v.lang.startsWith(userLang));
        this.selectedVoice = matchingVoice || voices[0];
      }
    },

    setVoice(voice: SpeechSynthesisVoice) {
      this.selectedVoice = voice;
      localStorage.setItem('selectedVoiceName', voice.name);
    },

    setSpeechRate(rate: number) {
      this.speechRate = rate;
      localStorage.setItem('speechRate', String(rate));
    }
  }
});
