const POLL_MS = 50

/**
 * Waits until Cloudflare's global script has registered {@link window.turnstile}.
 * Without this, calling {@code render} in {@code onMounted} often races {@code async defer} on the script tag.
 */
export function waitForTurnstile(timeoutMs = 20000): Promise<NonNullable<Window['turnstile']>> {
  return new Promise((resolve, reject) => {
    const deadline = Date.now() + timeoutMs
    const tick = () => {
      const api = window.turnstile
      if (api && typeof api.render === 'function') {
        resolve(api)
        return
      }
      if (Date.now() >= deadline) {
        reject(
          new Error(
            'Turnstile script did not load in time (check network, ad blocker, or CSP).',
          ),
        )
        return
      }
      window.setTimeout(tick, POLL_MS)
    }
    tick()
  })
}
