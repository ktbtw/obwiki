<template>
  <a-layout-header class="header">
    <div class="logo" />
    <a-menu
      theme="dark"
      mode="horizontal"
      :style="{ lineHeight: '64px' }"
    >
      <a-menu-item key="/">
        <router-link to="/">首页</router-link>
      </a-menu-item>
      <a-menu-item key="/admin/ebook" >
        <router-link to="/admin/ebook">海洋生物种类管理</router-link>
      </a-menu-item>

      <a-menu-item key="/admin/category" >
        <router-link to="/admin/category">海洋生物分类管理</router-link>
      </a-menu-item>

      <a-menu-item key="/about" >
        <router-link to="/about">关于我们</router-link>
      </a-menu-item>
    </a-menu>
  </a-layout-header>
</template>
<script lang="ts" setup>

</script>

<style scoped>
.logo {
  float: left;
  width: 120px;
  height: 31px;
  margin: 16px 24px 16px 0;
  background: rgba(255, 255, 255, 0.3);
}
</style>
