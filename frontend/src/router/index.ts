import { createRouter, createWebHistory } from 'vue-router'
import NonRegisteredHomeView from '@/views/nonRegistered/HomeView.vue'
import { useAuthStore } from '@/stores/auth/useAuthStore.ts'

// Auth views
import LoginView from '@/views/auth/login/LoginView.vue'
import ResetPasswordView from '@/views/auth/password/ResetPasswordView.vue'
import VerifyEmailView from "@/views/auth/VerifyEmailView.vue"

// Error views
import NotFoundView from '@/views/errors/NotFoundView.vue'

// Admin views
import AdminDashboardView from '@/views/admin/dashboard/AdminDashboardView.vue'
import AdminMapView from '@/views/admin/map/AdminMapView.vue'
import AdminResetPasswordLink from '@/views/admin/resetPassword/AdminResetPasswordLink.vue'
import AdminRegisterView from '@/views/admin/register/AdminRegisterView.vue'
import AdminScenariosView from '@/views/admin/scenario/ScenariosView.vue'
import ManageAdminsView from '@/views/admin/ManageAdminsView.vue'
import AdminInviteView from '@/views/admin/invite/AdminInviteView.vue'

// Registered User views
import DashboardView from '@/views/registered/dashboard/DashboardView.vue'
import HouseholdDetailsView from '@/views/registered/household/HouseholdView.vue'
import HouseholdInventoryView from '@/views/registered/inventory/HouseholdInventoryView.vue'
import HomeAddressView from '@/views/registered/household/HomeAddressView.vue'
import NewHouseholdView from '@/views/registered/household/NewHousehold.vue'
import HouseholdReflectionsPage from '@/views/registered/household/HouseholdReflectionsPage.vue';
const PublicReflectionsPage = () => import('@/views/registered/reflections/PublicReflectionsPage.vue');
import SendResetPasswordLinkView from '@/views/auth/password/SendResetPasswordLinkView.vue'
// Non-Registered User views
import JoinOrCreateHouseholdView from '@/views/nonRegistered/household/JoinOrCreateHouseholdView.vue'
import MapView from '@/views/nonRegistered/map/MapView.vue'
import RegisterView from '@/views/auth/register/RegisterView.vue'
import PrivacyPolicyView from '@/views/nonRegistered/static/PrivacyPolicyView.vue'
import NewsView from '@/views/nonRegistered/news/NewsView.vue'
import ArticleView from '@/views/nonRegistered/news/ArticleView.vue'
import AboutUsView from '@/views/nonRegistered/static/AboutUsView.vue'
import ForgotPasswordView from '@/views/auth/password/ForgotPasswordView.vue'
import VerifyPasswordReset from '@/views/VerifyPasswordReset.vue'
// Crisis Information views
import BeforeCrisisView from '@/views/nonRegistered/info/BeforeCrisisView.vue'
import DuringCrisisView from '@/views/nonRegistered/info/DuringCrisisView.vue'
import AfterCrisisView from '@/views/nonRegistered/info/AfterCrisisView.vue'

import ScenariosListView from '@/views/nonRegistered/scenario/ScenariosListView.vue'
import ScenarioDetailView from '@/views/nonRegistered/scenario/ScenarioDetailView.vue'
import KriserPage from '@/views/nonRegistered/event/KriserPage.vue';
import EventDetailPage from '@/views/nonRegistered/event/EventDetailPage.vue';
import MyReflectionsPage from '@/views/user/MyReflectionsPage.vue';
import ReflectionDetailView from '@/views/registered/reflections/ReflectionDetailView.vue';
import VerifyToken from '@/views/VerifyToken.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'home',
      component: NonRegisteredHomeView,
    },
    // New Kriser Route
    {
      path: '/kriser',
      name: 'kriser',
      component: KriserPage,
    },
    {
      path: '/kriser/:id',
      name: 'event-detail',
      component: EventDetailPage,
      props: true
    },
    // Auth routes
    {
      path: '/logg-inn',
      name: 'login',
      component: LoginView,
      meta: { requiresGuest: true }
    },
    {
      path: '/glemt-passord',
      name: 'glemt-passord',
      component: SendResetPasswordLinkView,
      meta: { requiresGuest: true }
    },
    {
      path: '/verifiser-passord-tilbakestilling',
      name: 'verify-password-reset',
      component: VerifyPasswordReset,
      meta: { requiresGuest: true }
    },
    {
      path: '/reset-passord',
      name: 'reset-password',
      component: ForgotPasswordView,
      meta: { requiresGuest: true }
    },

    // Admin routes
    {
      path: '/admin',
      name: 'admin-dashboard',
      component: AdminDashboardView,
      meta: { requiresAuth: true, requiresAdmin: true }
    },
    {
      path: '/admin/registrer',
      name: 'admin-register',
      component: AdminRegisterView,
      meta: { requiresAuth: true, requiresAdmin: true }
    },
    {
      path: '/admin/kart',
      name: 'admin-map',
      component: AdminMapView,
      meta: { requiresAuth: true, requiresAdmin: true }
    },
    {
      path: '/admin/reset-passord-link',
      name: 'admin-reset-passord-link',
      component: AdminResetPasswordLink,
      meta: { requiresAuth: true, requiresAdmin: true }
    },
    {
      path: '/admin/scenarios',
      name: 'admin-scenarios',
      component: AdminScenariosView,
      meta: { requiresAuth: true, requiresAdmin: true }
    },
    {
      path: '/admin/brukere',
      name: 'admin-users',
      component: ManageAdminsView,
      meta: { requiresAuth: true, requiresSuperAdmin: true }
    },
    {
      path: '/admin/invite',
      name: 'admin-invite',
      component: AdminInviteView,
      meta: { requiresAuth: true, requiresSuperAdmin: true }
    },
    {
      path: '/verify',
      name: 'verify',
      component: VerifyToken,
      meta: { requiresGuest: true }
    },

    // Registered User routes
    {
      path: '/dashboard',
      name: 'dashboard',
      component: DashboardView,
      meta: { requiresAuth: true }
    },
    {
      path: '/mine-refleksjoner',
      name: 'my-reflections',
      component: MyReflectionsPage,
      meta: { requiresAuth: true }
    },
    {
      path: '/refleksjon/:id',
      name: 'reflection-detail',
      component: ReflectionDetailView,
      props: true,
      meta: { requiresAuth: true }
    },
    {
      path: '/husstand',
      name: 'household',
      component: HouseholdDetailsView,
      meta: { requiresAuth: true }
    },
    {
      path: '/husstand/refleksjoner',
      name: 'HouseholdReflections',
      component: HouseholdReflectionsPage,
      meta: { requiresAuth: true }
    },
    {
      path: '/husstand/beredskapslager',
      name: 'household-emergency-stock',
      component: HouseholdInventoryView,
      meta: { requiresAuth: true }
    },
    {
      path: '/husstand/opprett',
      name: 'new-household',
      component: NewHouseholdView,
      meta: { requiresAuth: true }
    },
    {
      path: '/refleksjoner/offentlige',
      name: 'public-reflections',
      component: PublicReflectionsPage,
      meta: { requiresAuth: false }
    },
    {
      path: '/endre-passord',
      name: 'reset-password',
      component: ResetPasswordView,
      meta: { requiresAuth: true }
    },
    {
      path: '/adresse',
      name: 'home-address',
      component: HomeAddressView
    },
    // Non-Registered User routes
    {
      path: '/bli-med-eller-opprett-husstand',
      name: 'join-create-household',
      component: JoinOrCreateHouseholdView
    },
    {
      path: '/kart',
      name: 'map',
      component: MapView
    },
    {
      path: '/registrer',
      name: 'register',
      component: RegisterView
    },
    {
      path: '/bekreft-e-post',
      name: 'verify-email',
      component: VerifyEmailView
    },
    {
      path: '/personvern',
      name: 'privacy-policy',
      component: PrivacyPolicyView
    },
    {
      path: '/nyheter',
      name: 'news',
      component: NewsView
    },
    {
      path: '/artikkel/:id',
      name: 'article',
      component: ArticleView
    },
    {
      path: '/om-oss',
      name: 'about-us',
      component: AboutUsView
    },

    // Crisis Information routes
    {
      path: '/info/for-krisen',
      name: 'before-crisis',
      component: BeforeCrisisView
    },
    {
      path: '/info/under-krisen',
      name: 'during-crisis',
      component: DuringCrisisView
    },
    {
      path: '/info/etter-krisen',
      name: 'after-crisis',
      component: AfterCrisisView
    },
    {
      path: '/scenarios',
      name: 'scenarios-list',
      component: ScenariosListView
    },
    {
      path: '/scenario/:id',
      name: 'scenario-detail',
      component: ScenarioDetailView,
      props: true
    },
    {
      path: '/:pathMatch(.*)*',
      name: 'not-found',
      component: NotFoundView
    }
  ]
})

// Navigation guards
router.beforeEach((to, from, next) => {
  const auth = useAuthStore()

  // Check if the route requires authentication
  if (to.meta.requiresAuth && !auth.isAuthenticated) {
    // Redirect to login page with return URL
    return next({
      name: 'login',
      query: { redirect: to.fullPath },
    })
  }

  // Check if the route requires admin privileges
  if (to.meta.requiresAdmin) {
    // If currentUser is still loading, wait for it
    if (!auth.currentUser) {
      // Wait for the currentUser query to complete
      auth.refetchUser().then(() => {
        if (!auth.isAdmin) {
          next({ name: 'not-found' })
        } else {
          next()
        }
      })
      return
    }

    if (!auth.isAdmin) {
      return next({ name: 'not-found' })
    }
  }

  // Check if the route requires super admin privileges
  if (to.meta.requiresSuperAdmin) {
    // If currentUser is still loading, wait for it
    if (!auth.currentUser) {
      // Wait for the currentUser query to complete
      auth.refetchUser().then(() => {
        if (!auth.isSuperAdmin) {
          next({ name: 'not-found' })
        } else {
          next()
        }
      })
      return
    }

    if (!auth.isSuperAdmin) {
      return next({ name: 'not-found' })
    }
  }

  // Check if the route requires guest access (like login page)
  if (to.meta.requiresGuest && auth.isAuthenticated) {
    return next({ name: 'dashboard' })
  }

  // Add a catch-all route for admin paths
  if (to.path.startsWith('/admin') && !to.matched.length) {
    return next({ name: 'admin-dashboard' })
  }

  next()
})

export default router
