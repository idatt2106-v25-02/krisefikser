import { describe, expect, it } from 'vitest'
import { createEventMarkers } from '../event'
import { EventResponseLevel, EventResponseStatus } from '@/api/generated/model'

describe('createEventMarkers', () => {
  it('creates circle markers for valid events', () => {
    const markers = createEventMarkers([
      {
        id: 1,
        title: 'Storm',
        description: 'Strong wind',
        radius: 300,
        latitude: 63.43,
        longitude: 10.39,
        level: EventResponseLevel.RED,
        status: EventResponseStatus.ONGOING,
        startTime: '2026-04-20T09:00:00Z',
      },
    ])

    expect(markers).toHaveLength(1)
    expect(markers[0].isCircle).toBe(true)
    expect(markers[0].type).toBe('event')
  })

  it('filters out events without coordinates or radius', () => {
    const markers = createEventMarkers([
      {
        id: 2,
        title: 'Missing',
        description: 'Missing lat',
        radius: 100,
        latitude: 0,
        longitude: 10.39,
        level: EventResponseLevel.YELLOW,
        status: EventResponseStatus.UPCOMING,
        startTime: '2026-04-20T09:00:00Z',
      },
    ])

    expect(markers).toHaveLength(0)
  })
})
