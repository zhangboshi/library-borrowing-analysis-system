import { createRouter, createWebHistory } from 'vue-router';
import Login from '@/views/Login.vue';
import Dashboard from '@/views/Dashboard.vue';
import BorrowRecords from '@/views/BorrowRecords.vue';
import { isAuthenticated } from '@/utils/auth';

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
  if (to.path === '/login' && isAuthenticated()) {
    return next('/dashboard');
  }
  if (to.meta.requiresAuth && !isAuthenticated()) {
    return next({ path: '/login', query: { redirect: to.fullPath } });
  }
  next();
});

export default router;
