<script setup lang="ts">
import { computed, watch } from 'vue'
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from '@/components/ui/dialog'
import {
  cookieSettingsModalOpen,
  closeCookieSettings,
} from '@/plugins/docs/cookie-settings-ui'
import {
  hasAnalyticsConsent,
  PRIVACY_POLICY_VERSION,
  saveAnalyticsConsent,
} from '@/plugins/docs/posthog-consent'
import { ref } from 'vue'

const analyticsEnabled = ref(false)

const open = computed({
  get: () => cookieSettingsModalOpen.value,
  set: (value: boolean) => {
    cookieSettingsModalOpen.value = value
    if (!value) {
      closeCookieSettings()
    }
  },
})

watch(
  () => cookieSettingsModalOpen.value,
  (isOpen) => {
    if (isOpen) {
      analyticsEnabled.value = hasAnalyticsConsent()
    }
  },
)

function handleSave(): void {
  saveAnalyticsConsent(analyticsEnabled.value, 'settings')
  closeCookieSettings()
}
</script>

<template>
  <Dialog v-model:open="open">
    <DialogContent class="sm:max-w-lg">
      <DialogHeader>
        <DialogTitle>Informasjonskapsler og analyse</DialogTitle>
        <DialogDescription>
          Velg hvilke typer informasjonskapsler du tillater. Nødvendige cookies brukes for
          innlogging og sikkerhet. Analyse (PostHog) er valgfritt og starter først etter at du
          slår det på og lagrer.
        </DialogDescription>
      </DialogHeader>

      <div class="space-y-4 py-2">
        <div class="rounded-lg border border-gray-200 p-4 bg-gray-50">
          <div class="flex items-start justify-between gap-3">
            <div>
              <p class="font-medium text-gray-900">Nødvendige</p>
              <p class="text-sm text-gray-600 mt-1">
                Kreves for at nettstedet skal fungere (f.eks. innlogging). Kan ikke slås av.
              </p>
            </div>
            <input
              type="checkbox"
              checked
              disabled
              class="mt-1 h-4 w-4 rounded border-gray-300"
              aria-label="Nødvendige informasjonskapsler (alltid på)"
            />
          </div>
        </div>

        <div class="rounded-lg border border-gray-200 p-4">
          <div class="flex items-start justify-between gap-3">
            <div>
              <p class="font-medium text-gray-900">Analyse (PostHog)</p>
              <p class="text-sm text-gray-600 mt-1">
                Hjelper oss å forstå bruk av siden (aggregert). Behandles etter
                <router-link to="/personvern" class="text-blue-600 underline" @click="closeCookieSettings"
                  >personvernerklæringen</router-link
                >
                (versjon {{ PRIVACY_POLICY_VERSION }}).
              </p>
            </div>
            <input
              v-model="analyticsEnabled"
              type="checkbox"
              class="mt-1 h-4 w-4 rounded border-gray-300 text-blue-600 focus:ring-blue-500"
              aria-label="Tillat analyse med PostHog"
            />
          </div>
        </div>
      </div>

      <DialogFooter class="gap-2 sm:gap-0">
        <button
          type="button"
          class="inline-flex justify-center rounded-md border border-gray-300 bg-white px-4 py-2 text-sm font-medium text-gray-700 shadow-sm hover:bg-gray-50"
          @click="closeCookieSettings"
        >
          Avbryt
        </button>
        <button
          type="button"
          class="inline-flex justify-center rounded-md border border-transparent bg-blue-600 px-4 py-2 text-sm font-medium text-white shadow-sm hover:bg-blue-700"
          @click="handleSave"
        >
          Lagre valg
        </button>
      </DialogFooter>
    </DialogContent>
  </Dialog>
</template>
