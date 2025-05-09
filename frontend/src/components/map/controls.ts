import L from 'leaflet'

// Custom zoom control HTML
export const setupCustomControls = (map: L.Map, mapCenter: [number, number]) => {
  // Create custom zoom in control
  // @ts-expect-error - Leaflet types are not correctly detecting L.control as callable
  const zoomInControl = L.control({ position: 'bottomleft' })
  zoomInControl.onAdd = function () {
    const div = L.DomUtil.create('div', 'custom-map-control zoom-in')
    div.innerHTML = `
      <button aria-label="Zoom inn" class="bg-background text-foreground p-2 rounded-md shadow-md hover:bg-accent transition-colors">
        <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
          <line x1="12" y1="5" x2="12" y2="19"></line>
          <line x1="5" y1="12" x2="19" y2="12"></line>
        </svg>
      </button>
    `
    L.DomEvent.on(div, 'click', function (e) {
      L.DomEvent.stopPropagation(e)
      map.zoomIn()
    })
    return div
  }
  zoomInControl.addTo(map)

  // Create custom zoom out control
  // @ts-expect-error - Leaflet types are not correctly detecting L.control as callable
  const zoomOutControl = L.control({ position: 'bottomleft' })
  zoomOutControl.onAdd = function () {
    const div = L.DomUtil.create('div', 'custom-map-control zoom-out')
    div.innerHTML = `
      <button aria-label="Zoom ut" class="bg-background text-foreground p-2 rounded-md shadow-md hover:bg-accent transition-colors">
        <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
          <line x1="5" y1="12" x2="19" y2="12"></line>
        </svg>
      </button>
    `
    L.DomEvent.on(div, 'click', function (e) {
      L.DomEvent.stopPropagation(e)
      map.zoomOut()
    })
    return div
  }
  zoomOutControl.addTo(map)

  // Create home button to reset view
  // @ts-expect-error - Leaflet types are not correctly detecting L.control as callable
  const homeControl = L.control({ position: 'bottomleft' })
  homeControl.onAdd = function () {
    const div = L.DomUtil.create('div', 'custom-map-control home')
    div.innerHTML = `
      <button aria-label="Reset visning" class="bg-background text-foreground p-2 rounded-md shadow-md hover:bg-accent transition-colors">
        <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
          <path d="m3 9 9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"></path>
          <polyline points="9 22 9 12 15 12 15 22"></polyline>
        </svg>
      </button>
    `
    L.DomEvent.on(div, 'click', function (e) {
      L.DomEvent.stopPropagation(e)
      map.setView(mapCenter, props.initialZoom ?? 13)
    })
    return div
  }
  homeControl.addTo(map)
}
