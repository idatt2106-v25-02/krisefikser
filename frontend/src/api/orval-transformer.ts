type OrvalVerb = {
  route: string
  [key: string]: unknown
}

const ABSOLUTE_URL_PREFIX = /^https?:\/\/[^/]+/i

/**
 * Ensures generated clients use relative API paths so runtime
 * environment/proxy decides backend host instead of hardcoded localhost.
 */
export default function orvalTransformer(verb: OrvalVerb): OrvalVerb {
  return {
    ...verb,
    route: verb.route.replace(ABSOLUTE_URL_PREFIX, ''),
  }
}
