export const DEFAULT_MAP_POINT_ICON = '/icons/map-pin.svg'

/** Maps legacy seeded URLs (no matching static files in the SPA) to public/icons assets. */
const LEGACY_ICON_URL_MAP: Record<string, string> = {
  '/images/icons/hospital.png': '/icons/hospital.svg',
  '/images/icons/shelter.png': '/icons/map/shelter.svg',
  '/images/icons/food.png': '/icons/utensils.svg',
  '/images/icons/water.png': '/icons/droplets.svg',
  '/images/icons/police.png': '/icons/shield.svg',
  '/images/icons/fire.png': '/icons/flame.svg',
  '/images/icons/pharmacy.png': '/icons/pill.svg',
  '/images/icons/school.png': '/icons/school.svg',
  '/images/icons/gas.png': '/icons/fuel.svg',
  '/images/icons/grocery.png': '/icons/shopping-cart.svg',
}

export function resolveMapPointIconUrl(iconUrl: string | undefined): string {
  if (!iconUrl) {
    return DEFAULT_MAP_POINT_ICON
  }
  const mapped = LEGACY_ICON_URL_MAP[iconUrl]
  if (mapped) {
    return mapped
  }
  if (iconUrl.startsWith('/images/icons/')) {
    return DEFAULT_MAP_POINT_ICON
  }
  return iconUrl
}
