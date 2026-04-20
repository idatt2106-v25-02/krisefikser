import { describe, expect, it } from 'vitest'
import { createMeetingPointMarker } from '../meetingPoint'

describe('createMeetingPointMarker', () => {
  it('returns marker when coordinates exist', () => {
    const marker = createMeetingPointMarker({
      id: 'm-1',
      name: 'Trygg plass',
      description: 'Ved skolen',
      latitude: 63.43,
      longitude: 10.39,
      householdId: 'h-1',
    })

    expect(marker).not.toBeNull()
    expect(marker?.type).toBe('meetingPoint')
    expect(marker?.popupContent).toContain('Trygg plass')
  })

  it('returns null when coordinates are missing', () => {
    const marker = createMeetingPointMarker({
      id: 'm-2',
      name: 'Ugyldig',
      description: 'Mangler koordinater',
      latitude: 0,
      longitude: 0,
      householdId: 'h-1',
    })

    expect(marker).toBeNull()
  })
})
