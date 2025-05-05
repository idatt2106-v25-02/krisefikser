import type { useAuthStore } from '@/stores/useAuthStore'

let storeRef: ReturnType<typeof useAuthStore> | null = null

export function setStoreRef(store: ReturnType<typeof useAuthStore>) {
  storeRef = store
}

export function getStoreRef() {
  return storeRef
}
