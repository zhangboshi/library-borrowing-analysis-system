import { createRouter, createWebHistory } from 'vue-router';
import Login from '@/views/Login.vue';
import Dashboard from '@/views/Dashboard.vue';
import BorrowRecords from '@/views/BorrowRecords.vue';
import { isAuthenticated } from '@/utils/auth';
import { useUserStore } from '@/stores/user';

const routes = [
  { path: '/', redirect: '/dashboard' },
  { path: '/login', component: Login },
  { path: '/dashboard', component: Dashboard, meta: { requiresAuth: true } },
  { path: '/borrow-records', component: BorrowRecords, meta: { requiresAuth: true } },
];

const router = createRouter({
  history: createWebHistory(),
  routes,
});

router.beforeEach((to, from, next) => {
  const userStore = useUserStore();
  if (to.path === '/login' && isAuthenticated()) {
    return next('/dashboard');
  }
  if (to.meta.requiresAuth && !isAuthenticated()) {
    return next({ path: '/login', query: { redirect: to.fullPath } });
  }
  if (isAuthenticated() && !userStore.user) {
    userStore.loadUser().finally(() => next());
    return;
  }
  next();
});

export default router;
