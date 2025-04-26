// stores/useAuthModeStore.ts
import { defineStore } from 'pinia'
import { ref, watch } from 'vue'

export const useAuthModeStore = defineStore('authMode', () => {
  const isAdmin = ref(localStorage.getItem('isAdmin') === 'true')

  // Automatically persist to localStorage on change
  watch(isAdmin, (newValue) => {
    localStorage.setItem('isAdmin', String(newValue))
  })

  function toggle() {
    isAdmin.value = !isAdmin.value
  }

  return { isAdmin, toggle }
})
