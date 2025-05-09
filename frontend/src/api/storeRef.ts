import type { useAuthStore } from '@/stores/auth/useAuthStore'

let storeRef: ReturnType<typeof useAuthStore> | null = null

export function setStoreRef(store: ReturnType<typeof useAuthStore>) {
  storeRef = store
}

export function getStoreRef() {
  return storeRef
}
