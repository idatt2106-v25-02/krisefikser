/**
 * Generated by orval v7.9.0 🍺
 * Do not edit manually.
 * Krisefikser API
 * API for the Krisefikser application
 * OpenAPI spec version: 1.0
 */
import type { EventResponseLevel } from './eventResponseLevel'
import type { EventResponseStatus } from './eventResponseStatus'

export interface EventResponse {
  id: number
  title: string
  description: string
  radius: number
  latitude: number
  longitude: number
  level: EventResponseLevel
  startTime: string
  endTime?: string
  status: EventResponseStatus
}
