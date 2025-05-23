/**
 * Generated by orval v7.9.0 🍺
 * Do not edit manually.
 * Krisefikser API
 * API for the Krisefikser application
 * OpenAPI spec version: 1.0
 */
import type { CreateReflectionRequestVisibility } from './createReflectionRequestVisibility'

/**
 * Reflection data
 */
export interface CreateReflectionRequest {
  title: string
  content: string
  visibility: CreateReflectionRequestVisibility
  householdId?: string
  eventId?: number
}
