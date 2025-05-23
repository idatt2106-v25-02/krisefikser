/**
 * Generated by orval v7.9.0 🍺
 * Do not edit manually.
 * Krisefikser API
 * API for the Krisefikser application
 * OpenAPI spec version: 1.0
 */
import type { ReflectionResponseVisibility } from './reflectionResponseVisibility'

export interface ReflectionResponse {
  id?: string
  title?: string
  content?: string
  authorId?: string
  authorName?: string
  visibility?: ReflectionResponseVisibility
  householdId?: string
  householdName?: string
  eventId?: number
  createdAt?: string
  updatedAt?: string
}
