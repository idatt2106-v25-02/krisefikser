import { describe, it, expect } from 'vitest'
import { cn } from '../utils'

describe('Utility functions', () => {
  describe('cn function', () => {
    it('merges class names correctly', () => {
      expect(cn('class1', 'class2')).toBe('class1 class2')
    })

    it('handles conditional classes', () => {
      expect(cn('class1', true && 'class2', false && 'class3')).toBe('class1 class2')
    })

    it('handles array inputs', () => {
      expect(cn('class1', ['class2', 'class3'])).toBe('class1 class2 class3')
    })

    it('handles object inputs', () => {
      expect(cn('class1', { class2: true, class3: false })).toBe('class1 class2')
    })
  })
})
