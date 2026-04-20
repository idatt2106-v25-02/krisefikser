declare module 'jest-axe' {
  import type { AxeResults, RunOptions } from 'axe-core'

  export function axe(node: Element | string, options?: RunOptions): Promise<AxeResults>
}
