import { createRouter, createWebHistory } from 'vue-router'
import HomeView from "@/views/nonRegistered/HomeView.vue";

// Auth views
import LoginView from "@/views/auth/LoginView.vue";
import ForgotPasswordView from "@/views/auth/ForgotPasswordView.vue";
import ResetPasswordView from "@/views/auth/ResetPasswordView.vue";

// Error views
import NotFoundView from "@/views/errors/NotFoundView.vue";

// Admin views
import AdminDashboardView from "@/views/admin/AdminDashboardView.vue";
import AdminEventsView from "@/views/admin/AdminEventsView.vue";
import AdminMapView from "@/views/admin/AdminMapView.vue";
import AdminSearchView from "@/views/admin/AdminSearchView.vue";
import AdminLoginView from "@/views/admin/AdminLoginView.vue";

// Super Admin views
import SuperAdminDashboardView from "@/views/superAdmin/SuperAdminDashboardView.vue";
import ManageAdminsView from "@/views/superAdmin/ManageAdminsView.vue";

// Registered User views
import DashboardView from "@/views/registered/DashboardView.vue";
import AddItemView from "@/views/registered/AddItemView.vue";
import HouseholdView from "@/views/registered/HouseholdView.vue";
import InventoryView from "@/views/registered/InventoryView.vue";
import InviteView from "@/views/registered/InviteView.vue";
import SearchView from "@/views/registered/SearchView.vue";

// Non-Registered User views
import NonRegisteredHomeView from "@/views/nonRegistered/HomeView.vue";
import JoinOrCreateHouseholdView from "@/views/nonRegistered/JoinOrCreateHouseholdView.vue";
import MapView from "@/views/nonRegistered/MapView.vue";
import RegisterView from "@/views/nonRegistered/RegisterView.vue";
import PrivacyPolicyView from "@/views/nonRegistered/PrivacyPolicyView.vue";
import NewsView from "@/views/nonRegistered/NewsView.vue";
import ArticleView from "@/views/nonRegistered/ArticleView.vue";
import AboutUsView from "@/views/nonRegistered/AboutUsView.vue";

// Crisis Information views
import BeforeCrisisView from "@/views/nonRegistered/info/BeforeCrisisView.vue";
import DuringCrisisView from "@/views/nonRegistered/info/DuringCrisisView.vue";
import AfterCrisisView from "@/views/nonRegistered/info/AfterCrisisView.vue";

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'home',
      component: HomeView,
    },
    // Auth routes
    {
      path: '/logg-inn',
      name: 'login',
      component: LoginView,
    },
    {
      path: '/glemt-passord',
      name: 'forgot-password',
      component: ForgotPasswordView,
    },
    {
      path: '/reset-passord',
      name: 'reset-password',
      component: ResetPasswordView,
    },

    // Admin routes
    {
      path: '/admin',
      name: 'admin-dashboard',
      component: AdminDashboardView,
    },
    {
      path: '/admin/login',
      name: 'admin-login',
      component: AdminLoginView,
    },
    {
      path: '/admin/hendelser',
      name: 'admin-events',
      component: AdminEventsView,
    },
    {
      path: '/admin/kart',
      name: 'admin-map',
      component: AdminMapView,
    },
    {
      path: '/admin/sok',
      name: 'admin-search',
      component: AdminSearchView,
    },

    // Super Admin routes
    {
      path: '/super-admin',
      name: 'super-admin-dashboard',
      component: SuperAdminDashboardView,
    },
    {
      path: '/super-admin/behandle-administratorer',
      name: 'manage-admins',
      component: ManageAdminsView,
    },

    // Registered User routes
    {
      path: '/dashboard',
      name: 'dashboard',
      component: DashboardView,
    },
    {
      path: '/legg-til-vare',
      name: 'add-item',
      component: AddItemView,
    },
    {
      path: '/husstand',
      name: 'household',
      component: HouseholdView,
    },
    {
      path: '/inventar',
      name: 'inventory',
      component: InventoryView,
    },
    {
      path: '/inviter-medlemmer',
      name: 'invite',
      component: InviteView,
    },
    {
      path: '/sok',
      name: 'search',
      component: SearchView,
    },

    // Non-Registered User routes
    {
      path: '/velkommen',
      name: 'non-registered-home',
      component: NonRegisteredHomeView,
    },
    {
      path: '/bli-med-eller-opprett-husstand',
      name: 'join-create-household',
      component: JoinOrCreateHouseholdView,
    },
    {
      path: '/kart',
      name: 'map',
      component: MapView,
    },
    {
      path: '/register',
      name: 'register',
      component: RegisterView,
    },
    {
      path: '/personvern',
      name: 'privacy-policy',
      component: PrivacyPolicyView,
    },
    {
      path: '/nyheter',
      name: 'news',
      component: NewsView,
    },
    {
      path: '/nyheter/:id',
      name: 'article',
      component: ArticleView,
    },
    {
      path: '/om-oss',
      name: 'about-us',
      component: AboutUsView,
    },

    // Crisis Information routes
    {
      path: '/info/for-krisen',
      name: 'before-crisis',
      component: BeforeCrisisView,
    },
    {
      path: '/info/under-krisen',
      name: 'during-crisis',
      component: DuringCrisisView,
    },
    {
      path: '/info/etter-krisen',
      name: 'after-crisis',
      component: AfterCrisisView,
    },

    // Error routes - must be last
    {
      path: '/:pathMatch(.*)*',
      name: 'not-found',
      component: NotFoundView,
    },
  ],
})

export default router
