<template>
  <el-container class="layout-shell">
    <el-aside width="200px" class="aside">
      <div class="logo">Library Analytics</div>
      <el-menu :default-active="active" class="menu" router>
        <el-menu-item index="/dashboard">仪表盘</el-menu-item>
        <el-menu-item index="/borrow-records">借阅记录</el-menu-item>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header class="header">
        <div class="title">图书借阅行为统计与可视化</div>
        <el-button type="text" @click="logout">退出</el-button>
      </el-header>
      <el-main class="main">
        <slot />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { useRoute, useRouter } from 'vue-router';

const route = useRoute();
const router = useRouter();
const active = route.path;

const logout = () => {
  localStorage.removeItem('token');
  router.push('/login');
};
</script>

<style scoped>
.layout-shell {
  min-height: 100vh;
}
.aside {
  background: #1f2d3d;
  color: #fff;
}
.logo {
  padding: 16px;
  font-weight: 700;
  color: #fff;
}
.menu {
  border-right: none;
}
.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 16px;
  background: #fff;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.08);
}
.main {
  background: #f5f7fa;
}
.title {
  font-size: 16px;
  font-weight: 600;
}
</style>
