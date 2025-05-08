import { useAccessibilityStore } from '@/stores/tts/accessibilityStore.ts'
import speechService from './speechService.ts'

// Add type declarations for Web Speech API
interface SpeechRecognitionEvent extends Event {
  results: SpeechRecognitionResultList
  resultIndex: number
}

interface SpeechRecognitionErrorEvent extends Event {
  error: string
  message: string
}

interface SpeechRecognitionResultList {
  length: number

  item(index: number): SpeechRecognitionResult

  [index: number]: SpeechRecognitionResult
}

interface SpeechRecognitionResult {
  isFinal: boolean
  length: number

  item(index: number): SpeechRecognitionAlternative

  [index: number]: SpeechRecognitionAlternative
}

interface SpeechRecognitionAlternative {
  transcript: string
  confidence: number
}

interface SpeechRecognition extends EventTarget {
  continuous: boolean
  interimResults: boolean
  lang: string
  onresult: (event: SpeechRecognitionEvent) => void
  onerror: (event: SpeechRecognitionErrorEvent) => void
  onend: () => void

  start(): void

  stop(): void

  abort(): void
}

declare global {
  interface Window {
    SpeechRecognition: new () => SpeechRecognition
    webkitSpeechRecognition: new () => SpeechRecognition
  }
}

class VoiceCommandService {
  private recognition: SpeechRecognition | null = null
  private isListening: boolean = false
  private store: ReturnType<typeof useAccessibilityStore> | null = null

  constructor() {
    if ('SpeechRecognition' in window || 'webkitSpeechRecognition' in window) {
      const SpeechRecognition = window.SpeechRecognition || window.webkitSpeechRecognition
      this.recognition = new SpeechRecognition()
      this.setupRecognition()
    }
  }

  public startListening() {
    if (!this.recognition) {
      speechService.speak('Stemmeoppkjenning er ikke støttet i denne nettleseren')
      return
    }

    try {
      this.isListening = true
      this.recognition.start()
      speechService.speak('Stemmeoppkjenning er aktivert')
    } catch (error) {
      console.error('Error starting voice recognition:', error)
      this.isListening = false
    }
  }

  public stopListening() {
    if (!this.recognition) return

    this.isListening = false
    this.recognition.stop()
    speechService.speak('Stemmeoppkjenning er deaktivert')
  }

  public toggleListening() {
    if (this.isListening) {
      this.stopListening()
    } else {
      this.startListening()
    }
  }

  public isVoiceRecognitionSupported(): boolean {
    return 'SpeechRecognition' in window || 'webkitSpeechRecognition' in window
  }

  private initializeStore() {
    if (!this.store) {
      this.store = useAccessibilityStore()
    }
    return this.store
  }

  private setupRecognition() {
    if (!this.recognition) return

    this.recognition.continuous = true
    this.recognition.interimResults = false
    this.recognition.lang = 'nb-NO' // Norwegian

    this.recognition.onresult = (event: SpeechRecognitionEvent) => {
      const command = event.results[event.results.length - 1][0].transcript.toLowerCase()
      this.handleCommand(command)
    }

    this.recognition.onerror = (event: SpeechRecognitionErrorEvent) => {
      console.error('Voice recognition error:', event.error)
      this.stopListening()
    }

    this.recognition.onend = () => {
      if (this.isListening) {
        this.startListening() // Restart if we're still supposed to be listening
      }
    }
  }

  private handleCommand(command: string) {
    const store = this.initializeStore()
    if (!store) return

    // TTS commands
    if (command.includes('aktiver talesyntese') || command.includes('skru på talesyntese')) {
      if (!store.ttsEnabled) {
        store.toggleTTS()
        speechService.speak('Talesyntese er nå aktivert')
      }
    } else if (
      command.includes('deaktiver talesyntese') ||
      command.includes('skru av talesyntese')
    ) {
      if (store.ttsEnabled) {
        store.toggleTTS()
        speechService.cancel()
      }
    } else if (command.includes('les siden') || command.includes('les innhold')) {
      if (store.ttsEnabled) {
        // Trigger read page functionality using the correct custom event
        const readPageEvent = new CustomEvent('readPage')
        document.dispatchEvent(readPageEvent)
      }
    } else if (command.includes('stopp lesing')) {
      speechService.cancel()
    }

    // Speech rate commands
    if (command.includes('snakk raskere')) {
      const newRate = Math.min(store.speechRate + 0.2, 2)
      store.setSpeechRate(newRate)
      speechService.speak(`Talehastighet er nå ${newRate.toFixed(1)}`)
    } else if (command.includes('snakk saktere')) {
      const newRate = Math.max(store.speechRate - 0.2, 0.5)
      store.setSpeechRate(newRate)
      speechService.speak(`Talehastighet er nå ${newRate.toFixed(1)}`)
    }
  }
}

// Create a singleton instance
const voiceCommandService = new VoiceCommandService()
export default voiceCommandService
