import type { App } from 'vue'
import type { Router } from 'vue-router'
import { watch } from 'vue'
import posthog from 'posthog-js'
import { useAuthStore } from '@/stores/auth/useAuthStore'

type AuthStore = ReturnType<typeof useAuthStore>

let hooksRegistered = false

export function setupPostHogAppHooks(app: App, router: Router, authStore: AuthStore): void {
  if (hooksRegistered) {
    return
  }
  hooksRegistered = true

  router.afterEach((to) => {
    posthog.capture('$pageview', {
      path: to.path,
      routeName: typeof to.name === 'string' ? to.name : null,
      query: to.query,
    })
  })

  watch(
    () => authStore.currentUser,
    (currentUser) => {
      if (!currentUser?.id) {
        return
      }

      posthog.identify(currentUser.id, {
        email: currentUser.email,
        roles: currentUser.roles,
      })
    },
    { immediate: true },
  )

  watch(
    () => authStore.isAuthenticated,
    (isAuthenticated) => {
      if (!isAuthenticated) {
        posthog.reset()
      }
    },
    { immediate: true },
  )

  // Expose for optional use in components
  app.config.globalProperties.$posthog = posthog
}
