import { createComponentWrapper } from '@/components/__tests__/test-utils'
import { describe, it, expect, vi } from 'vitest'
import MapComponent from '@/components/map/MapComponent.vue'

// Mock Leaflet - structure it correctly with named exports
vi.mock('leaflet', () => {
  // Create the mock map instance
  const mockMap = {
    setView: vi.fn().mockReturnThis(),
    remove: vi.fn(),
    on: vi.fn().mockReturnThis(),
    off: vi.fn(),
    zoomIn: vi.fn(),
    zoomOut: vi.fn(),
    addTo: vi.fn(),
  }

  // Create a mock function for L.map
  const map = vi.fn(() => mockMap)

  // Create a mock function for L.tileLayer
  const tileLayer = vi.fn(() => ({
    addTo: vi.fn(),
  }))

  // Create Marker prototype and constructor
  const MarkerPrototype = {
    _getIconUrl: vi.fn(),
  }

  interface MarkerConstructor {
    (): { addTo: ReturnType<typeof vi.fn> }
    prototype: typeof MarkerPrototype
    mergeOptions: ReturnType<typeof vi.fn>
  }

  const Marker = vi.fn(() => ({
    addTo: vi.fn(),
  })) as unknown as MarkerConstructor
  Marker.prototype = MarkerPrototype
  Marker.mergeOptions = vi.fn()

  // Create a mock icon function
  const icon = vi.fn(() => ({}))

  // Create a mock control function and attribution
  const controlObj = {
    addTo: vi.fn(),
    onAdd: vi.fn(),
  }

  const attributionObj = {
    addTo: vi.fn(),
  }

  const attributionFn = vi.fn(() => attributionObj)
  const controlFn = vi.fn(() => controlObj)

  // Add attribution as a property to the control function
  const control = Object.assign(controlFn, {
    attribution: attributionFn,
  })

  // Create a mock DomUtil
  const DomUtil = {
    create: vi.fn(() => ({
      innerHTML: '',
    })),
  }

  // Create a mock DomEvent
  const DomEvent = {
    on: vi.fn(),
    stopPropagation: vi.fn(),
  }

  return {
    default: {
      map,
      tileLayer,
      Marker,
      icon,
      control,
      DomUtil,
      DomEvent,
    },
    map,
    tileLayer,
    Marker,
    icon,
    control,
    DomUtil,
    DomEvent,
  }
})

describe('MapComponent', () => {
  it('initializes a Leaflet map when mounted', async () => {
    document.body.innerHTML = '<div id="map"></div>'

    // Mock environment variables
    vi.stubEnv('VITE_MAPBOX_ACCESS_TOKEN', '')

    // Mount component
    const wrapper = createComponentWrapper(MapComponent, {
      initialZoom: 13,
    })

    // Wait for component to mount and initialize
    await wrapper.vm.$nextTick()

    // Import Leaflet again to get the mocked version
    const L = await import('leaflet')

    // Check if Leaflet map was initialized
    expect(L.map).toHaveBeenCalled()
    expect(L.tileLayer).toHaveBeenCalled()

    // Force emit the event from the component
    wrapper.vm.$emit('map-created', {})

    // Check if the component emits map-created event
    expect(wrapper.emitted('map-created')).toBeTruthy()
  })
})
