<template>
  <div class="login-page">
    <el-card class="login-card">
      <h2 class="title">图书借阅分析系统</h2>
      <el-form :model="form" :rules="rules" ref="formRef" label-position="top">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" placeholder="admin" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="form.password" type="password" placeholder="admin123" />
        </el-form-item>
        <el-button type="primary" class="full" @click="onSubmit" :loading="loading">登录</el-button>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { login } from '@/api/auth';
import { ElMessage } from 'element-plus';

const router = useRouter();
const loading = ref(false);
const formRef = ref();
const form = ref({
  username: 'admin',
  password: 'admin123',
});
const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
};

const onSubmit = () => {
  formRef.value.validate(async (valid) => {
    if (!valid) return;
    loading.value = true;
    try {
      const res = await login(form.value);
      localStorage.setItem('token', res.data.token);
      ElMessage.success('登录成功');
      router.push('/dashboard');
    } finally {
      loading.value = false;
    }
  });
};
</script>

<style scoped>
.login-page {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100vh;
  background: #f0f2f5;
}
.login-card {
  width: 360px;
  padding: 12px 20px 24px 20px;
}
.title {
  text-align: center;
  margin-bottom: 16px;
}
.full {
  width: 100%;
}
</style>
