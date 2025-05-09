//esl
import {toRaw} from 'vue'

/**
 * Safely extracts an array from a Vue reactive proxy object
 * @param proxy The proxy object potentially containing an array
 * @returns The extracted array or an empty array if extraction fails
 */
function extractArrayFromProxy(proxy: unknown): number[] {
  try {
    // First try direct unwrapping with toRaw
    const rawValue = toRaw(proxy as object)

    // If it's already an array, return it
    if (Array.isArray(rawValue)) {
      return [...rawValue]
    }

    // Try to access the target property if it exists
    //eslint-disable-next-line @typescript-eslint/no-explicit-any
    const anyRawValue = rawValue as any
    if (anyRawValue && typeof anyRawValue === 'object' && 'target' in anyRawValue) {
      return extractArrayFromProxy(anyRawValue.target)
    }

    // Last resort, try to stringify and parse (works for some proxy types)
    const jsonSafe = JSON.parse(JSON.stringify(rawValue))
    if (Array.isArray(jsonSafe)) {
      return jsonSafe
    }

    return []
  } catch {
    return []
  }
}

function arrayToDate(proxyString: string) {
  const dateArray = extractArrayFromProxy(proxyString)
  if (!dateArray || !Array.isArray(dateArray) || dateArray.length < 3) return null
  // Note: Month in JavaScript is 0-based, so we subtract 1 from the month
  return new Date(
    dateArray[0],
    dateArray[1] - 1,
    dateArray[2],
    dateArray[3] || 0,
    dateArray[4] || 0,
    dateArray[5] || 0,
  )
}

function formatDate(proxyString: string) {
  const date = arrayToDate(proxyString)
  if (!date || isNaN(date.getTime())) return ''

  try {
    return date.toLocaleDateString('nb-NO', {
      day: 'numeric',
      month: 'long',
      year: 'numeric',
    })
  } catch (e) {
    console.error('Error formatting date:', e)
    // Fallback to English if Norwegian locale is not supported
    return date.toLocaleDateString('en', {
      day: 'numeric',
      month: 'long',
      year: 'numeric',
    })
  }
}

export { extractArrayFromProxy, arrayToDate, formatDate }
