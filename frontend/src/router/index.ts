import { createRouter, createWebHistory } from 'vue-router'
import HomeView from "@/views/nonRegistered/HomeView.vue";

// Auth views
import LoginView from "@/views/auth/LoginView.vue";
import ResetPasswordView from "@/views/auth/ResetPasswordView.vue";

// Error views
import NotFoundView from "@/views/errors/NotFoundView.vue";

// Admin views
import AdminDashboardView from "@/views/admin/AdminDashboardView.vue";
import AdminEventsView from "@/views/admin/AdminEventsView.vue";
import AdminMapView from "@/views/admin/AdminMapView.vue";
import AdminSearchView from "@/views/admin/AdminSearchView.vue";
import AdminResetPasswordLink from "@/views/admin/AdminResetPasswordLink.vue";
import AdminRegisterView from '@/views/admin/AdminRegisterView.vue';

// Super Admin views
import SuperAdminDashboardView from "@/views/superAdmin/SuperAdminDashboardView.vue";
import ManageAdminsView from "@/views/superAdmin/ManageAdminsView.vue";

// Registered User views
import DashboardView from "@/views/registered/DashboardView.vue";
import AddItemView from "@/views/registered/AddItemView.vue";
import HouseholdView from "@/views/registered/HouseholdView.vue";
import HouseholdDetailsView from "@/views/registered/HouseholdDetailsView.vue";
import HouseholdInventoryView from "@/views/registered/HouseholdInventoryView.vue";
import InviteView from "@/views/registered/InviteView.vue";
import SearchView from "@/views/registered/SearchView.vue";
import HomeAddressView from "@/views/registered/HomeAddressView.vue";
import NewHouseholdView from "@/views/registered/NewHousehold.vue";
// Non-Registered User views
import NonRegisteredHomeView from "@/views/nonRegistered/HomeView.vue";
import JoinOrCreateHouseholdView from "@/views/nonRegistered/JoinOrCreateHouseholdView.vue";
import MapView from "@/views/nonRegistered/MapView.vue";
import RegisterView from "@/views/nonRegistered/RegisterView.vue";
import PrivacyPolicyView from "@/views/nonRegistered/PrivacyPolicyView.vue";
import NewsView from "@/views/nonRegistered/NewsView.vue";
import ArticleView from "@/views/nonRegistered/ArticleView.vue";
import AboutUsView from "@/views/nonRegistered/AboutUsView.vue";
import ForgotPasswordView from "@/views/auth/ForgotPasswordView.vue";

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
      name: 'glemt-passord',
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
      path: '/admin/register',
      name: 'admin-register',
      component: AdminRegisterView,
    },
    {
      path: '/admin/kart',
      name: 'admin-map',
      component: AdminMapView,
    },
    {
      path: '/admin/reset-passord-link',
      name : 'admin-reset-passord-link',
      component: AdminResetPasswordLink,
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
      path: '/husstand/:id',
      name: 'household-details',
      component: HouseholdDetailsView,
    },

    {
      path: '/husstand/:id/beredskapslager',
      name: 'household-emergency-stock',
      component: HouseholdInventoryView,
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
    {
      path: '/adresse',
      name: 'home-address',
      component: HomeAddressView,
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
      path: '/husstand/opprett',
      name: 'new-household',
      component: NewHouseholdView,
    },

    {
      path: '/info/under-krisen',
      name: 'during-crisis',
      component: DuringCrisisView,
    },

    {
      path: '/info/for-krisen',
      name: 'before-crisis',
      component: BeforeCrisisView,
    },

    {
      path: '/info/etter-krisen',
      name: 'after-crisis',
      component: AfterCrisisView,
    },

    {
      path: '/om-oss',
      name: 'om-oss',
      component: AboutUsView,
    },

    {
      path: '/personvern',
      name: 'personvern',
      component: PrivacyPolicyView,
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
