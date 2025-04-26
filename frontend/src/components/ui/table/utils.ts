import type { Ref } from 'vue'

// Define our own Updater type instead of importing from @tanstack/vue-table
export type Updater<T> = T | ((old: T) => T)

export function valueUpdater<T extends Updater<any>>(updaterOrValue: T, ref: Ref) {
  ref.value
    = typeof updaterOrValue === 'function'
      ? updaterOrValue(ref.value)
      : updaterOrValue
}
