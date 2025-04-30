declare namespace turnstile {
  interface TurnstileOptions {
    sitekey: string
    callback?: (token: string) => void
    'error-callback'?: () => void
    'expired-callback'?: () => void
    theme?: 'light' | 'dark' | 'auto'
    size?: 'normal' | 'compact'
    tabindex?: number
    'response-field'?: boolean
    'response-field-name'?: string
  }

  function render(container: string | HTMLElement, options: TurnstileOptions): string
  function reset(widgetId?: string): void
  function getResponse(widgetId?: string): string
  function remove(widgetId?: string): void
}
