<script lang="ts">
import { defineComponent, computed } from 'vue';
import { useAuthStore } from '@/stores/auth/useAuthStore';

export default defineComponent({
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
    // Description for authenticated users
    authDescription: {
      type: String,
      default: ''
    },
    // Default buttons for unauthenticated users
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
      default: '/logg-inn'
    },
    // Buttons for authenticated users
    authPrimaryButtonText: {
      type: String,
      default: 'Min husstand'
    },
    authPrimaryButtonRoute: {
      type: String,
      default: '/husstand'
    },
    authSecondaryButtonText: {
      type: String,
      default: 'Min profil'
    },
    authSecondaryButtonRoute: {
      type: String,
      default: '/dashboard'
    },
    // Title for authenticated users
    authTitle: {
      type: String,
      default: ''
    },
    colorTheme: {
      type: String as () => 'blue' | 'yellow' | 'green',
      default: 'blue',
      validator: (value: string) => ['blue', 'yellow', 'green'].includes(value)
    },
    // Option to hide the component when user is authenticated
    hideWhenAuthenticated: {
      type: Boolean,
      default: false
    }
  },
  setup(props) {
    const authStore = useAuthStore();

    const backgroundColor = computed((): string => {
      const colors: Record<'blue' | 'yellow' | 'green', string> = {
        blue: 'bg-blue-600',
        yellow: 'bg-yellow-600',
        green: 'bg-green-600'
      };
      return colors[props.colorTheme];
    });

    const primaryButtonColor = computed((): string => {
      const colors: Record<'blue' | 'yellow' | 'green', string> = {
        blue: 'text-blue-600 hover:bg-blue-50',
        yellow: 'text-yellow-600 hover:bg-yellow-50',
        green: 'text-green-600 hover:bg-green-50'
      };
      return colors[props.colorTheme];
    });

    const secondaryButtonColor = computed((): string => {
      const colors: Record<'blue' | 'yellow' | 'green', string> = {
        blue: 'hover:bg-blue-700',
        yellow: 'hover:bg-yellow-700',
        green: 'hover:bg-green-700'
      };
      return colors[props.colorTheme];
    });

    // Using the appropriate button text and routes based on authentication
    const activePrimaryButtonText = computed(() => {
      return authStore.isAuthenticated ? props.authPrimaryButtonText : props.primaryButtonText;
    });

    const activePrimaryButtonRoute = computed(() => {
      return authStore.isAuthenticated ? props.authPrimaryButtonRoute : props.primaryButtonRoute;
    });

    const activeSecondaryButtonText = computed(() => {
      return authStore.isAuthenticated ? props.authSecondaryButtonText : props.secondaryButtonText;
    });

    const activeSecondaryButtonRoute = computed(() => {
      return authStore.isAuthenticated ? props.authSecondaryButtonRoute : props.secondaryButtonRoute;
    });

    // Active description based on authentication status
    const activeDescription = computed(() => {
      if (authStore.isAuthenticated && props.authDescription) {
        return props.authDescription;
      }
      return props.description;
    });

    // Active title based on authentication status
    const activeTitle = computed(() => {
      if (authStore.isAuthenticated && props.authTitle) {
        return props.authTitle;
      }
      return props.title;
    });

    // Determines if the component should be displayed
    const shouldDisplay = computed(() => {
      if (props.hideWhenAuthenticated && authStore.isAuthenticated) {
        return false;
      }
      return true;
    });

    return {
      authStore,
      backgroundColor,
      primaryButtonColor,
      secondaryButtonColor,
      activePrimaryButtonText,
      activePrimaryButtonRoute,
      activeSecondaryButtonText,
      activeSecondaryButtonRoute,
      activeDescription,
      activeTitle,
      shouldDisplay
    };
  }
});
</script>

<template>
  <div v-if="shouldDisplay" :class="[backgroundColor, 'text-white rounded-lg shadow-md p-8 mb-8']">
    <h3 class="text-2xl font-bold mb-4">{{ activeTitle }}</h3>
    <p class="mb-6">{{ activeDescription }}</p>
    <div class="flex flex-col sm:flex-row gap-4">
      <router-link
        :to="activePrimaryButtonRoute"
        class="bg-white font-medium px-6 py-2 rounded-md transition text-center"
        :class="primaryButtonColor"
      >
        {{ activePrimaryButtonText }}
      </router-link>
      <router-link
        :to="activeSecondaryButtonRoute"
        class="border border-white text-white px-6 py-2 rounded-md transition text-center"
        :class="secondaryButtonColor"
      >
        {{ activeSecondaryButtonText }}
      </router-link>
    </div>
  </div>
</template>
