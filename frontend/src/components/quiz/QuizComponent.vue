<script lang="ts">
import { defineComponent } from 'vue';
import type { PropType } from 'vue';
import { CheckCircle, XCircle, Info, ChevronLeft, Trophy, Award, ThumbsUp } from 'lucide-vue-next';

// Define interfaces for type safety
interface Question {
  question: string;
  options: string[];
  correctAnswer: number;
  explanation?: string;
}

type ColorTheme = 'blue' | 'yellow' | 'green';

export default defineComponent({
  name: 'QuizComponent',
  components: {
    CheckCircle,
    XCircle,
    Info,
    ChevronLeft,
    Trophy,
    Award,
    ThumbsUp
  },
  props: {
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
  },
  data() {
    return {
      currentState: 'intro' as 'intro' | 'question' | 'results' | 'review',
      currentQuestionIndex: 1,
      userAnswers: Array(this.questions.length).fill(null) as (number | null)[],
      selectedAnswerIndex: null as number | null,
      score: 0,
      answerSubmitted: false,
      answeredQuestions: Array(this.questions.length).fill(false) as boolean[]
    }
  },
  computed: {
    currentQuestion(): Question {
      return this.questions[this.currentQuestionIndex - 1];
    },
    isLastQuestion(): boolean {
      return this.currentQuestionIndex === this.questions.length;
    },
    buttonColorClass(): string {
      const colors: Record<ColorTheme, string> = {
        blue: 'bg-blue-600 hover:bg-blue-700',
        yellow: 'bg-yellow-600 hover:bg-yellow-700',
        green: 'bg-green-600 hover:bg-green-700'
      };
      return colors[this.colorTheme as ColorTheme];
    },
    progressBarColorClass(): string {
      const colors: Record<ColorTheme, string> = {
        blue: 'bg-blue-600',
        yellow: 'bg-yellow-600',
        green: 'bg-green-600'
      };
      return colors[this.colorTheme as ColorTheme];
    },
    resultCircleColorClass(): string {
      const colors: Record<ColorTheme, string> = {
        blue: 'bg-blue-600',
        yellow: 'bg-yellow-600',
        green: 'bg-green-600'
      };
      return colors[this.colorTheme as ColorTheme];
    },
    resultFeedback(): string {
      const percentage = (this.score / this.questions.length) * 100;

      if (percentage === 100) {
        return 'Fantastisk! Du har full kontroll på dette temaet.';
      } else if (percentage >= 75) {
        return 'Veldig bra! Du har god kunnskap om dette temaet.';
      } else if (percentage >= 50) {
        return 'Bra! Du har grunnleggende kunnskap, men det er rom for forbedring.';
      } else {
        return 'Du har tatt et godt første steg. Les mer om temaet for å øke kunnskapen din.';
      }
    }
  },
  methods: {
    startQuiz(): void {
      this.currentState = 'question';
    },
    resetQuiz(): void {
      this.currentState = 'intro';
      this.currentQuestionIndex = 1;
      this.userAnswers = Array(this.questions.length).fill(null);
      this.selectedAnswerIndex = null;
      this.score = 0;
      this.answerSubmitted = false;
      this.answeredQuestions = Array(this.questions.length).fill(false);
    },
    selectAnswer(index: number): void {
      // Only allow selection if answer hasn't been submitted yet
      if (!this.answerSubmitted) {
        this.selectedAnswerIndex = index;
      }
    },
    submitAnswer(): void {
      if (this.selectedAnswerIndex !== null) {
        this.answerSubmitted = true;
        this.answeredQuestions[this.currentQuestionIndex - 1] = true;

        // Save the answer
        this.userAnswers[this.currentQuestionIndex - 1] = this.selectedAnswerIndex;

        // Update score if correct
        if (this.selectedAnswerIndex === this.currentQuestion.correctAnswer) {
          this.score++;
        }
      }
    },
    previousQuestion(): void {
      if (this.currentQuestionIndex > 1) {
        this.currentQuestionIndex--;
        this.selectedAnswerIndex = this.userAnswers[this.currentQuestionIndex - 1];
        this.answerSubmitted = this.answeredQuestions[this.currentQuestionIndex - 1];
      }
    },
    nextQuestion(): void {
      if (this.answerSubmitted) {
        if (this.isLastQuestion) {
          this.calculateResults();
        } else {
          // Move to next question
          this.currentQuestionIndex++;
          // Set selected answer to the previously saved answer (if any)
          this.selectedAnswerIndex = this.userAnswers[this.currentQuestionIndex - 1];
          // Reset submission status for the new question
          this.answerSubmitted = this.answeredQuestions[this.currentQuestionIndex - 1];
        }
      }
    },
    calculateResults(): void {
      this.currentState = 'results';
    },
    viewAnswers(): void {
      this.currentState = 'review';
    },
    getOptionClass(index: number): string {
      if (!this.answerSubmitted) {
        // Before answer submission
        return this.selectedAnswerIndex === index
          ? `bg-${this.colorTheme}-50 border-${this.colorTheme}-300`
          : 'border-gray-200 hover:border-gray-300 bg-white';
      } else {
        // After answer submission
        if (index === this.currentQuestion.correctAnswer) {
          // Correct answer
          return 'bg-green-50 border-green-500';
        } else if (index === this.selectedAnswerIndex) {
          // Selected but wrong answer
          return 'bg-red-50 border-red-500';
        } else {
          // Unselected and wrong
          return 'border-gray-200 bg-white';
        }
      }
    },
    getOptionIconClass(index: number): string {
      if (!this.answerSubmitted) {
        // Before answer submission
        return this.selectedAnswerIndex === index
          ? `border-${this.colorTheme}-500 bg-${this.colorTheme}-500`
          : 'border-gray-300';
      } else {
        // After answer submission
        if (index === this.currentQuestion.correctAnswer) {
          // Correct answer
          return 'border-green-500 bg-green-500';
        } else if (index === this.selectedAnswerIndex) {
          // Selected but wrong answer
          return 'border-red-500 bg-red-500';
        } else {
          // Unselected and wrong
          return 'border-gray-300';
        }
      }
    }
  }
});
</script>
<template>
  <div class="quiz-container">
    <!-- Quiz intro -->
    <div v-if="currentState === 'intro'" class="text-center mb-8">
      <h3 class="text-xl font-semibold text-gray-800 mb-4">{{ title }}</h3>
      <p class="text-gray-700 mb-6">{{ description }}</p>
      <button
        @click="startQuiz"
        class="px-6 py-2 rounded-md transition text-white font-medium"
        :class="buttonColorClass"
      >
        Start Quiz
      </button>
    </div>

    <!-- Question display -->
    <div v-else-if="currentState === 'question'" class="quiz-content">
      <!-- Progress bar -->
      <div class="mb-6">
        <div class="w-full h-2 bg-gray-200 rounded-full">
          <div
            class="h-2 rounded-full transition-all duration-300"
            :class="progressBarColorClass"
            :style="{ width: `${(currentQuestionIndex / questions.length) * 100}%` }"
          ></div>
        </div>
        <div class="flex justify-between text-sm text-gray-500 mt-1">
          <span>Spørsmål {{ currentQuestionIndex }} av {{ questions.length }}</span>
          <span>{{ Math.round((currentQuestionIndex / questions.length) * 100) }}% fullført</span>
        </div>
      </div>

      <!-- Current question -->
      <div class="bg-white rounded-lg shadow-md p-6 mb-6">
        <h4 class="font-medium text-gray-800 text-lg mb-4">
          {{ currentQuestion.question }}
        </h4>

        <div class="space-y-3">
          <div
            v-for="(option, index) in currentQuestion.options"
            :key="index"
            @click="selectAnswer(index)"
            class="p-3 rounded-lg border transition-all cursor-pointer"
            :class="getOptionClass(index)"
          >
            <div class="flex items-center">
              <div
                class="w-5 h-5 rounded-full flex items-center justify-center mr-3 border"
                :class="getOptionIconClass(index)"
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
              <span class="text-gray-800">{{ option }}</span>
            </div>
          </div>
        </div>

        <div v-if="answerSubmitted" class="mt-4 p-3 bg-blue-50 text-blue-800 rounded-md">
          <div class="flex items-start">
            <Info class="w-4 h-4 mt-1 mr-2 flex-shrink-0" />
            <p class="text-sm">{{ currentQuestion.explanation }}</p>
          </div>
        </div>

        <div class="mt-6 flex justify-between">
          <button
            v-if="currentQuestionIndex > 1"
            @click="previousQuestion"
            class="text-gray-600 flex items-center hover:text-gray-800"
          >
            <ChevronLeft class="w-4 h-4 mr-1" />
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
            >
              Sjekk svar
            </button>
            <button
              v-else
              @click="nextQuestion"
              class="px-6 py-2 rounded-md transition text-white font-medium"
              :class="buttonColorClass"
            >
              {{ isLastQuestion ? 'Se resultater' : 'Neste spørsmål' }}
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- Results display -->
    <div v-else-if="currentState === 'results'" class="quiz-results">
      <div class="bg-white rounded-lg shadow-md p-6 mb-6 text-center">
        <div class="mb-6">
          <div
            class="w-24 h-24 rounded-full mx-auto flex items-center justify-center"
            :class="resultCircleColorClass"
          >
            <Trophy v-if="score === questions.length" class="w-10 h-10 text-white" />
            <Award v-else-if="score > questions.length / 2" class="w-10 h-10 text-white" />
            <ThumbsUp v-else class="w-10 h-10 text-white" />
          </div>

          <h3 class="text-2xl font-bold mt-4">
            {{ score }} av {{ questions.length }} riktige svar!
          </h3>

          <p class="text-gray-600 mt-2">
            {{ resultFeedback }}
          </p>
        </div>

        <div class="flex justify-center space-x-4">
          <button
            @click="resetQuiz"
            class="border border-gray-300 text-gray-700 px-6 py-2 rounded-md hover:bg-gray-50 transition"
          >
            Prøv igjen
          </button>
          <button
            @click="viewAnswers"
            class="px-6 py-2 rounded-md transition text-white font-medium"
            :class="buttonColorClass"
          >
            Se svarene
          </button>
        </div>
      </div>
    </div>

    <!-- Answers review -->
    <div v-else-if="currentState === 'review'" class="quiz-review">
      <div class="mb-6 flex justify-between items-center">
        <h3 class="text-xl font-semibold text-gray-800">Dine svar</h3>
        <button
          @click="resetQuiz"
          class="px-6 py-2 rounded-md transition text-white font-medium"
          :class="buttonColorClass"
        >
          Prøv igjen
        </button>
      </div>

      <div class="space-y-6">
        <div
          v-for="(question, qIndex) in questions"
          :key="qIndex"
          class="bg-white rounded-lg shadow-md overflow-hidden"
        >
          <div
            class="p-4 text-white font-medium"
            :class="[
              userAnswers[qIndex] === question.correctAnswer ? 'bg-green-600' : 'bg-red-600'
            ]"
          >
            <div class="flex items-start">
              <div class="mr-3 mt-1">
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
            >
              <div class="flex items-center">
                <div
                  v-if="oIndex === question.correctAnswer"
                  class="mr-2 text-green-600"
                >
                  <CheckCircle class="w-4 h-4" />
                </div>
                <div
                  v-else-if="oIndex === userAnswers[qIndex] && oIndex !== question.correctAnswer"
                  class="mr-2 text-red-600"
                >
                  <XCircle class="w-4 h-4" />
                </div>
                <div v-else class="w-4 h-4 mr-2"></div>
                {{ option }}
              </div>
            </div>

            <div v-if="question.explanation" class="mt-3 p-3 bg-blue-50 text-blue-800 rounded-md">
              <div class="flex items-start">
                <Info class="w-4 h-4 mt-1 mr-2 flex-shrink-0" />
                <p class="text-sm">{{ question.explanation }}</p>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>


