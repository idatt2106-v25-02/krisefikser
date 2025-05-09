<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue';
import type { PropType } from 'vue';
import { CheckCircle, XCircle, Info, ChevronLeft, Trophy, Award, ThumbsUp } from 'lucide-vue-next';
import { useAccessibilityStore } from '@/stores/tts/accessibilityStore';

// Define interfaces for type safety
interface Question {
  question: string;
  options: string[];
  correctAnswer: number;
  explanation?: string;
}

type ColorTheme = 'blue' | 'yellow' | 'green';

const props = defineProps({
  title: {
    type: String as PropType<string>,
    default: 'Test din kunnskap'
  },
  description: {
    type: String as PropType<string>,
    default: 'Svar på spørsmålene for å teste din kunnskap om krisehåndtering.'
  },
  questions: {
    type: Array as PropType<Question[]>,
    required: true
  },
  colorTheme: {
    type: String as PropType<ColorTheme>,
    default: 'blue',
    validator: (value: string): boolean => ['blue', 'yellow', 'green'].includes(value as ColorTheme)
  }
});

// State management
const currentState = ref<'intro' | 'question' | 'results' | 'review'>('intro');
const currentQuestionIndex = ref(1);
const userAnswers = ref<(number | null)[]>(Array(props.questions.length).fill(null));
const selectedAnswerIndex = ref<number | null>(null);
const score = ref(0);
const answerSubmitted = ref(false);
const answeredQuestions = ref<boolean[]>(Array(props.questions.length).fill(false));

// Computed properties
const currentQuestion = computed(() => props.questions[currentQuestionIndex.value - 1]);
const isLastQuestion = computed(() => currentQuestionIndex.value === props.questions.length);
const progressValue = computed(() => (currentQuestionIndex.value / props.questions.length) * 100);

const buttonColorClass = computed(() => {
  const colors: Record<ColorTheme, string> = {
    blue: 'bg-blue-600 hover:bg-blue-700',
    yellow: 'bg-yellow-600 hover:bg-yellow-700',
    green: 'bg-green-600 hover:bg-green-700'
  };
  return colors[props.colorTheme as ColorTheme];
});

const progressBarColorClass = computed(() => {
  const colors: Record<ColorTheme, string> = {
    blue: 'bg-blue-600',
    yellow: 'bg-yellow-600',
    green: 'bg-green-600'
  };
  return colors[props.colorTheme as ColorTheme];
});

const resultCircleColorClass = computed(() => {
  const colors: Record<ColorTheme, string> = {
    blue: 'bg-blue-600',
    yellow: 'bg-yellow-600',
    green: 'bg-green-600'
  };
  return colors[props.colorTheme as ColorTheme];
});

const resultFeedback = computed(() => {
  const percentage = (score.value / props.questions.length) * 100;

  if (percentage === 100) {
    return 'Fantastisk! Du har full kontroll på dette temaet.';
  } else if (percentage >= 75) {
    return 'Veldig bra! Du har god kunnskap om dette temaet.';
  } else if (percentage >= 50) {
    return 'Bra! Du har grunnleggende kunnskap, men det er rom for forbedring.';
  } else {
    return 'Du har tatt et godt første steg. Les mer om temaet for å øke kunnskapen din.';
  }
});

const store = useAccessibilityStore();

// Speech synthesis setup
const speechRate = ref(store.speechRate);
const synth = window.speechSynthesis;

// Keyboard event handler for speech rate
const handleKeyPress = (event: KeyboardEvent) => {
  if (event.altKey) {
    if (event.key === 'ArrowUp') {
      speechRate.value = Math.min(speechRate.value + 0.1, 2.0);
      speakText(`Talefart økt til ${Math.round(speechRate.value * 100)} prosent`);
    } else if (event.key === 'ArrowDown') {
      speechRate.value = Math.max(speechRate.value - 0.1, 0.5);
      speakText(`Talefart redusert til ${Math.round(speechRate.value * 100)} prosent`);
    }
  } else if (currentState.value === 'question' && !answerSubmitted.value) {
    // Number keys 1-4 for selecting answers
    const numKey = parseInt(event.key);
    if (numKey >= 1 && numKey <= currentQuestion.value.options.length) {
      selectAnswer(numKey - 1);
    }
  }
};

// Speech function
const speakText = (text: string) => {
  if (!store.ttsEnabled) return;

  if (synth.speaking) {
    synth.cancel();
  }
  const utterance = new SpeechSynthesisUtterance(text);
  utterance.lang = 'nb-NO';
  utterance.rate = speechRate.value;
  synth.speak(utterance);
};

// Cleanup on component unmount
onUnmounted(() => {
  if (synth.speaking) {
    synth.cancel();
  }
  window.removeEventListener('keydown', handleKeyPress);
});

// Setup keyboard listener
onMounted(() => {
  window.addEventListener('keydown', handleKeyPress);
});

// Methods
const startQuiz = () => {
  currentState.value = 'question';
  speakText(`Quiz startet. ${currentQuestion.value.question}`);
};

const resetQuiz = () => {
  currentState.value = 'intro';
  currentQuestionIndex.value = 1;
  userAnswers.value = Array(props.questions.length).fill(null);
  selectedAnswerIndex.value = null;
  score.value = 0;
  answerSubmitted.value = false;
  answeredQuestions.value = Array(props.questions.length).fill(false);
};

const selectAnswer = (index: number) => {
  // Only allow selection if answer hasn't been submitted yet
  if (!answerSubmitted.value) {
    selectedAnswerIndex.value = index;
    speakText(`Valgt alternativ ${index + 1}: ${currentQuestion.value.options[index]}`);
  }
};

const submitAnswer = () => {
  if (selectedAnswerIndex.value !== null) {
    answerSubmitted.value = true;
    answeredQuestions.value[currentQuestionIndex.value - 1] = true;

    // Save the answer
    userAnswers.value[currentQuestionIndex.value - 1] = selectedAnswerIndex.value;

    // Update score if correct and provide voice feedback
    if (selectedAnswerIndex.value === currentQuestion.value.correctAnswer) {
      score.value++;
      speakText(`Riktig svar! Du valgte: ${currentQuestion.value.options[selectedAnswerIndex.value]}. ${currentQuestion.value.explanation || ''}`);
    } else {
      const correctAnswer = currentQuestion.value.options[currentQuestion.value.correctAnswer];
      speakText(`Feil svar. Du valgte: ${currentQuestion.value.options[selectedAnswerIndex.value]}. Riktig svar var: ${correctAnswer}. ${currentQuestion.value.explanation || ''}`);
    }
  }
};

const previousQuestion = () => {
  if (currentQuestionIndex.value > 1) {
    currentQuestionIndex.value--;
    selectedAnswerIndex.value = userAnswers.value[currentQuestionIndex.value - 1];
    answerSubmitted.value = answeredQuestions.value[currentQuestionIndex.value - 1];
    // Read the previous question and options
    readQuestionAndOptions();
  }
};

const nextQuestion = () => {
  if (answerSubmitted.value) {
    if (isLastQuestion.value) {
      calculateResults();
      speakText(`Quiz fullført. Du fikk ${score.value} av ${props.questions.length} riktige svar. ${resultFeedback.value}`);
    } else {
      // Move to next question
      currentQuestionIndex.value++;
      // Set selected answer to the previously saved answer (if any)
      selectedAnswerIndex.value = userAnswers.value[currentQuestionIndex.value - 1];
      // Reset submission status for the new question
      answerSubmitted.value = answeredQuestions.value[currentQuestionIndex.value - 1];
      // Read the new question and options
      readQuestionAndOptions();
    }
  }
};

const calculateResults = () => {
  currentState.value = 'results';
};

const viewAnswers = () => {
  currentState.value = 'review';
};

const getOptionClass = (index: number) => {
  if (!answerSubmitted.value) {
    // Before answer submission
    return selectedAnswerIndex.value === index
      ? `bg-${props.colorTheme}-50 border-${props.colorTheme}-300`
      : 'border-gray-200 hover:border-gray-300 bg-white';
  } else {
    // After answer submission
    if (index === currentQuestion.value.correctAnswer) {
      // Correct answer
      return 'bg-green-50 border-green-500';
    } else if (index === selectedAnswerIndex.value) {
      // Selected but wrong answer
      return 'bg-red-50 border-red-500';
    } else {
      // Unselected and wrong
      return 'border-gray-200 bg-white';
    }
  }
};

const getOptionIconClass = (index: number) => {
  if (!answerSubmitted.value) {
    // Before answer submission
    return selectedAnswerIndex.value === index
      ? `border-${props.colorTheme}-500 bg-${props.colorTheme}-500`
      : 'border-gray-300';
  } else {
    // After answer submission
    if (index === currentQuestion.value.correctAnswer) {
      // Correct answer
      return 'border-green-500 bg-green-500';
    } else if (index === selectedAnswerIndex.value) {
      // Selected but wrong answer
      return 'border-red-500 bg-red-500';
    } else {
      // Unselected and wrong
      return 'border-gray-300';
    }
  }
};

// Add function to read question and options
const readQuestionAndOptions = () => {
  const question = currentQuestion.value;
  let text = `Spørsmål ${currentQuestionIndex.value}: ${question.question}. Alternativer: `;
  question.options.forEach((option, index) => {
    text += `${index + 1}: ${option}. `;
  });
  speakText(text);
};
</script>

<template>
  <div class="quiz-container" role="region" :aria-label="title">
    <!-- Add keyboard shortcut info -->
    <div class="text-sm text-gray-500 mb-4">
      <p>Trykk Alt + ↑ for raskere tale, Alt + ↓ for saktere tale</p>
      <p>Bruk talltastene 1-4 for å velge svar</p>
    </div>

    <!-- Quiz intro -->
    <div v-if="currentState === 'intro'" class="text-center mb-8" role="region" aria-label="Quiz introduksjon">
      <h3 class="text-xl font-semibold text-gray-800 mb-4">{{ title }}</h3>
      <p class="text-gray-700 mb-6">{{ description }}</p>
      <button
        @click="startQuiz"
        class="px-6 py-2 rounded-md transition text-white font-medium"
        :class="buttonColorClass"
        aria-label="Start quizen"
      >
        Start Quiz
      </button>
    </div>

    <!-- Question display -->
    <div v-else-if="currentState === 'question'" class="quiz-content" role="form" :aria-label="'Spørsmål ' + currentQuestionIndex + ' av ' + questions.length">
      <div class="mb-6" role="progressbar" :aria-valuenow="progressValue" :aria-valuemin="0" :aria-valuemax="100">
        <div class="w-full h-2 bg-gray-200 rounded-full">
          <div
            class="h-2 rounded-full transition-all duration-300"
            :class="progressBarColorClass"
            :style="{ width: `${progressValue}%` }"
          ></div>
        </div>
        <div class="flex justify-between text-sm text-gray-500 mt-1">
          <span>Spørsmål {{ currentQuestionIndex }} av {{ questions.length }}</span>
          <span>{{ Math.round(progressValue) }}% fullført</span>
        </div>
      </div>

      <!-- Current question -->
      <div class="bg-white rounded-lg shadow-md p-6 mb-6">
        <h4 class="font-medium text-gray-800 text-lg mb-4" id="current-question">
          {{ currentQuestion.question }}
        </h4>

        <div class="space-y-3" role="radiogroup" :aria-labelledby="'current-question'">
          <div
            v-for="(option, index) in currentQuestion.options"
            :key="index"
            @click="selectAnswer(index)"
            @keydown.space.prevent="selectAnswer(index)"
            @keydown.enter.prevent="selectAnswer(index)"
            class="p-3 rounded-lg border transition-all cursor-pointer"
            :class="getOptionClass(index)"
            role="radio"
            :aria-checked="selectedAnswerIndex === index"
            tabindex="0"
          >
            <div class="flex items-center">
              <div
                class="w-5 h-5 rounded-full flex items-center justify-center mr-3 border"
                :class="getOptionIconClass(index)"
                aria-hidden="true"
              >
                <CheckCircle
                  v-if="selectedAnswerIndex === index && (!answerSubmitted || index === currentQuestion.correctAnswer)"
                  class="w-4 h-4"
                  :class="answerSubmitted && index === currentQuestion.correctAnswer ? 'text-white' : 'text-white'"
                />
                <XCircle
                  v-if="answerSubmitted && selectedAnswerIndex === index && index !== currentQuestion.correctAnswer"
                  class="w-4 h-4 text-white"
                />
              </div>
              <span>{{ option }}</span>
            </div>
          </div>
        </div>

        <div v-if="answerSubmitted" class="mt-4 p-3 bg-blue-50 text-blue-800 rounded-md" role="alert">
          <div class="flex items-start">
            <Info class="w-4 h-4 mt-1 mr-2 flex-shrink-0" aria-hidden="true" />
            <p class="text-sm">{{ currentQuestion.explanation }}</p>
          </div>
        </div>

        <div class="mt-6 flex justify-between">
          <button
            v-if="currentQuestionIndex > 1"
            @click="previousQuestion"
            class="text-gray-600 flex items-center hover:text-gray-800"
            aria-label="Gå til forrige spørsmål"
          >
            <ChevronLeft class="w-4 h-4 mr-1" aria-hidden="true" />
            Forrige
          </button>
          <div v-else></div>

          <div class="flex space-x-3">
            <button
              v-if="!answerSubmitted"
              @click="submitAnswer"
              class="px-6 py-2 rounded-md transition text-white font-medium"
              :class="[
                selectedAnswerIndex !== null ? buttonColorClass : 'bg-gray-300 cursor-not-allowed'
              ]"
              :disabled="selectedAnswerIndex === null"
              :aria-label="selectedAnswerIndex === null ? 'Velg et svar før du sjekker' : 'Sjekk svaret ditt'"
            >
              Sjekk svar
            </button>
            <button
              v-else
              @click="nextQuestion"
              class="px-6 py-2 rounded-md transition text-white font-medium"
              :class="buttonColorClass"
              :aria-label="isLastQuestion ? 'Se resultater' : 'Gå til neste spørsmål'"
            >
              {{ isLastQuestion ? 'Se resultater' : 'Neste spørsmål' }}
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- Results display -->
    <div v-else-if="currentState === 'results'" class="quiz-results" role="region" aria-label="Quiz resultater">
      <div class="bg-white rounded-lg shadow-md p-6 mb-6 text-center">
        <div class="mb-6">
          <div
            class="w-24 h-24 rounded-full mx-auto flex items-center justify-center"
            :class="resultCircleColorClass"
            aria-hidden="true"
          >
            <Trophy v-if="score === questions.length" class="w-10 h-10 text-white" />
            <Award v-else-if="score > questions.length / 2" class="w-10 h-10 text-white" />
            <ThumbsUp v-else class="w-10 h-10 text-white" />
          </div>

          <h3 class="text-2xl font-bold mt-4" role="tab">
            {{ score }} av {{ questions.length }} riktige svar!
          </h3>

          <p class="text-gray-600 mt-2" role="status">
            {{ resultFeedback }}
          </p>
        </div>

        <div class="flex justify-center space-x-4">
          <button
            @click="resetQuiz"
            class="border border-gray-300 text-gray-700 px-6 py-2 rounded-md hover:bg-gray-50 transition"
            aria-label="Start quizen på nytt"
          >
            Prøv igjen
          </button>
          <button
            @click="viewAnswers"
            class="px-6 py-2 rounded-md transition text-white font-medium"
            :class="buttonColorClass"
            aria-label="Se gjennomgang av alle svarene"
          >
            Se svarene
          </button>
        </div>
      </div>
    </div>

    <!-- Answers review -->
    <div v-else-if="currentState === 'review'" class="quiz-review" role="region" aria-label="Gjennomgang av svar">
      <div class="mb-6 flex justify-between items-center">
        <h3 class="text-xl font-semibold text-gray-800">Dine svar</h3>
        <button
          @click="resetQuiz"
          class="px-6 py-2 rounded-md transition text-white font-medium"
          :class="buttonColorClass"
          aria-label="Start quizen på nytt"
        >
          Prøv igjen
        </button>
      </div>

      <div class="space-y-6">
        <div
          v-for="(question, qIndex) in questions"
          :key="qIndex"
          class="bg-white rounded-lg shadow-md overflow-hidden"
          role="region"
          :aria-label="'Spørsmål ' + (qIndex + 1)"
        >
          <div
            class="p-4 text-white font-medium"
            :class="[
              userAnswers[qIndex] === question.correctAnswer ? 'bg-green-600' : 'bg-red-600'
            ]"
          >
            <div class="flex items-start">
              <div class="mr-3 mt-1" aria-hidden="true">
                <CheckCircle v-if="userAnswers[qIndex] === question.correctAnswer" class="w-5 h-5" />
                <XCircle v-else class="w-5 h-5" />
              </div>
              <span>Spørsmål {{ qIndex + 1 }}: {{ question.question }}</span>
            </div>
          </div>

          <div class="p-4">
            <div
              v-for="(option, oIndex) in question.options"
              :key="oIndex"
              class="py-2 px-3 my-1 rounded-md"
              :class="[
                oIndex === question.correctAnswer ? 'bg-green-100 text-green-800' :
                oIndex === userAnswers[qIndex] && oIndex !== question.correctAnswer ? 'bg-red-100 text-red-800' : ''
              ]"
              role="listitem"
            >
              <div class="flex items-center">
                <div
                  v-if="oIndex === question.correctAnswer"
                  class="mr-2 text-green-600"
                  aria-hidden="true"
                >
                  <CheckCircle class="w-4 h-4" />
                </div>
                <div
                  v-else-if="oIndex === userAnswers[qIndex] && oIndex !== question.correctAnswer"
                  class="mr-2 text-red-600"
                  aria-hidden="true"
                >
                  <XCircle class="w-4 h-4" />
                </div>
                <div v-else class="w-4 h-4 mr-2"></div>
                {{ option }}
              </div>
            </div>

            <div v-if="question.explanation" class="mt-3 p-3 bg-blue-50 text-blue-800 rounded-md" role="alert">
              <div class="flex items-start">
                <Info class="w-4 h-4 mt-1 mr-2 flex-shrink-0" aria-hidden="true" />
                <p class="text-sm">{{ question.explanation }}</p>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

