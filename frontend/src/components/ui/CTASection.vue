<script lang="ts">
export default {
  name: 'CTASection',
  props: {
    title: {
      type: String,
      required: true
    },
    description: {
      type: String,
      required: true
    },
    primaryButtonText: {
      type: String,
      default: 'Registrer deg nå'
    },
    primaryButtonRoute: {
      type: String,
      default: '/register'
    },
    secondaryButtonText: {
      type: String,
      default: 'Logg inn'
    },
    secondaryButtonRoute: {
      type: String,
      default: '/login'
    },
    colorTheme: {
      type: String as () => 'blue' | 'yellow' | 'green',
      default: 'blue',
      validator: (value: string) => ['blue', 'yellow', 'green'].includes(value)
    }
  },
  computed: {
    backgroundColor(): string {
      const colors: Record<'blue' | 'yellow' | 'green', string> = {
        blue: 'bg-blue-600',
        yellow: 'bg-yellow-600',
        green: 'bg-green-600'
      };
      return colors[this.colorTheme];
    },
    primaryButtonColor(): string {
      const colors: Record<'blue' | 'yellow' | 'green', string> = {
        blue: 'text-blue-600 hover:bg-blue-50',
        yellow: 'text-yellow-600 hover:bg-yellow-50',
        green: 'text-green-600 hover:bg-green-50'
      };
      return colors[this.colorTheme];
    },
    secondaryButtonColor(): string {
      const colors: Record<'blue' | 'yellow' | 'green', string> = {
        blue: 'hover:bg-blue-700',
        yellow: 'hover:bg-yellow-700',
        green: 'hover:bg-green-700'
      };
      return colors[this.colorTheme];
    }
  }
}
</script>
<template>
  <div :class="[backgroundColor, 'text-white rounded-lg shadow-md p-8 mb-8']">
    <h3 class="text-2xl font-bold mb-4">{{ title }}</h3>
    <p class="mb-6">{{ description }}</p>
    <div class="flex flex-col sm:flex-row gap-4">
      <router-link
        :to="primaryButtonRoute"
        class="bg-white font-medium px-6 py-2 rounded-md transition text-center"
        :class="primaryButtonColor"
      >
        {{ primaryButtonText }}
      </router-link>
      <router-link
        :to="secondaryButtonRoute"
        class="border border-white text-white px-6 py-2 rounded-md transition text-center"
        :class="secondaryButtonColor"
      >
        {{ secondaryButtonText }}
      </router-link>
    </div>
  </div>
</template>
