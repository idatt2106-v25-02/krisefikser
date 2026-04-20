import { ref } from 'vue'

/** Opens the cookie / analytics settings dialog from anywhere (e.g. footer). */
export const cookieSettingsModalOpen = ref(false)

export function openCookieSettings(): void {
  cookieSettingsModalOpen.value = true
}

export function closeCookieSettings(): void {
  cookieSettingsModalOpen.value = false
}
