import 'leaflet'

declare module 'leaflet' {
  export interface Map {
    layers: Layer[]
  }

  export interface Layer {
    _map: Map
  }
}
