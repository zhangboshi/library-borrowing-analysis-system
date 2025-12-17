import { defineStore } from 'pinia';
import { ref } from 'vue';
import { fetchMe, logoutApi } from '@/api/auth';
import { clearToken } from '@/utils/auth';

export const useUserStore = defineStore('user', () => {
  const user = ref(null);

  const loadUser = async () => {
    const res = await fetchMe();
    user.value = res.data;
  };

  const logout = () => {
    logoutApi().catch(() => {});
    clearToken();
    user.value = null;
  };

  const role = () => user.value?.role;

  return { user, role, loadUser, logout };
});
