interface GuardMeta {
  requiresAuth?: boolean
  requiresAdmin?: boolean
  requiresSuperAdmin?: boolean
  requiresGuest?: boolean
}

interface GuardTarget {
  meta: GuardMeta
  fullPath: string
  path: string
  matched: unknown[]
}

interface GuardAuth {
  isAuthenticated: boolean
  isAdmin: boolean
  isSuperAdmin: boolean
  currentUser: unknown
  refetchUser: () => Promise<unknown>
}

type NextFn = (target?: unknown) => void

export function applyAuthGuards(to: GuardTarget, auth: GuardAuth, next: NextFn): boolean {
  if (to.meta.requiresAuth && !auth.isAuthenticated) {
    next({
      name: 'login',
      query: { redirect: to.fullPath },
    })
    return true
  }

  if (to.meta.requiresAdmin) {
    if (!auth.currentUser) {
      auth.refetchUser().then(() => {
        if (!auth.isAdmin) {
          next({ name: 'not-found' })
        } else {
          next()
        }
      })
      return true
    }

    if (!auth.isAdmin) {
      next({ name: 'not-found' })
      return true
    }
  }

  if (to.meta.requiresSuperAdmin) {
    if (!auth.currentUser) {
      auth.refetchUser().then(() => {
        if (!auth.isSuperAdmin) {
          next({ name: 'not-found' })
        } else {
          next()
        }
      })
      return true
    }

    if (!auth.isSuperAdmin) {
      next({ name: 'not-found' })
      return true
    }
  }

  if (to.meta.requiresGuest && auth.isAuthenticated) {
    next({ name: 'dashboard' })
    return true
  }

  if (to.path.startsWith('/admin') && !to.matched.length) {
    next({ name: 'admin-dashboard' })
    return true
  }

  return false
}
