import { createRouter, createWebHistory } from 'vue-router'
import HomeView from "@/views/HomeView.vue";

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
      path: '/login',
      name: 'login',
      component: LoginView,
    },
    {
      path: '/forgot-password',
      name: 'forgot-password',
      component: ForgotPasswordView,
    },
    {
      path: '/reset-password',
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
      path: '/admin/events',
      name: 'admin-events',
      component: AdminEventsView,
    },
    {
      path: '/admin/map',
      name: 'admin-map',
      component: AdminMapView,
    },
    {
      path: '/admin/search',
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
      path: '/super-admin/manage-admins',
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
      path: '/add-item',
      name: 'add-item',
      component: AddItemView,
    },
    {
      path: '/household',
      name: 'household',
      component: HouseholdView,
    },
    {
      path: '/inventory',
      name: 'inventory',
      component: InventoryView,
    },
    {
      path: '/invite',
      name: 'invite',
      component: InviteView,
    },
    {
      path: '/search',
      name: 'search',
      component: SearchView,
    },
    
    // Non-Registered User routes
    {
      path: '/welcome',
      name: 'non-registered-home',
      component: NonRegisteredHomeView,
    },
    {
      path: '/join-create-household',
      name: 'join-create-household',
      component: JoinOrCreateHouseholdView,
    },
    {
      path: '/map',
      name: 'map',
      component: MapView,
    },
    {
      path: '/register',
      name: 'register',
      component: RegisterView,
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
