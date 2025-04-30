import type { Ref } from 'vue'

// Define our own Updater type instead of importing from @tanstack/vue-table
export type Updater<T> = T | ((old: T) => T)

export function valueUpdater<T, U extends Updater<T>>(updaterOrValue: U, ref: Ref<T>) {
  ref.value =
    typeof updaterOrValue === 'function'
      ? (updaterOrValue as (old: T) => T)(ref.value)
      : (updaterOrValue as T)
}
