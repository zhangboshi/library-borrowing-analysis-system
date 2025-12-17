import { createRouter, createWebHistory } from 'vue-router';
import Login from '@/views/Login.vue';
import Dashboard from '@/views/Dashboard.vue';
import BorrowRecords from '@/views/BorrowRecords.vue';
import { isAuthenticated } from '@/utils/auth';
import { useUserStore } from '@/stores/user';

const routes = [
  { path: '/', redirect: '/dashboard' },
  { path: '/login', component: Login },
  { path: '/dashboard', component: Dashboard, meta: { requiresAuth: true, roles: ['ADMIN', 'STAFF', 'VIEWER'] } },
  { path: '/borrow-records', component: BorrowRecords, meta: { requiresAuth: true, roles: ['ADMIN', 'STAFF', 'VIEWER'] } },
];

const router = createRouter({
  history: createWebHistory(),
  routes,
});

router.beforeEach(async (to, from, next) => {
  const userStore = useUserStore();
  if (to.path === '/login' && isAuthenticated()) {
    return next('/dashboard');
  }
  if (to.meta.requiresAuth && !isAuthenticated()) {
    return next({ path: '/login', query: { redirect: to.fullPath } });
  }
  if (to.meta.requiresAuth) {
    if (!userStore.user) {
      try {
        await userStore.loadUser();
      } catch (e) {
        return next({ path: '/login', query: { redirect: to.fullPath } });
      }
    }
    if (to.meta.roles && userStore.user && !to.meta.roles.includes(userStore.user.role)) {
      return next('/dashboard');
    }
  }
  next();
});

export default router;
