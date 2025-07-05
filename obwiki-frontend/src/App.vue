<template>
  <a-layout>
    <the-header />

    <router-view />

    <the-footer />

    <!-- 右下角悬浮按钮 -->
    <a-button
      type="primary"
      shape="circle"
      size="large"
      style="position: fixed; right: 32px; bottom: 32px; z-index: 1000;"
      @click="drawerVisible = true"
    >AI</a-button>

    <!-- 右侧抽屉 -->
    <a-drawer
      title="AI 对话"
      placement="right"
      :width="360"
      :open="drawerVisible"
      @close="drawerVisible = false"
    >
      <AIFloatMenu />
    </a-drawer>

    <FloatingMusicPlayer />
  </a-layout>
</template>

<script lang="ts" setup>
import AIFloatMenu from "@/components/AIFloatMenu.vue";
import TheFooter from "@/components/the-footer.vue";
import TheHeader from "@/components/the-header.vue";
import FloatingMusicPlayer from '@/components/FloatingMusicPlayer.vue';
import { ref } from 'vue';
const drawerVisible = ref(false);
</script>

<style scoped>
.logo {
  float: left;
  width: 120px;
  height: 31px;
  margin: 16px 24px 16px 0;
  background: rgba(255, 255, 255, 0.3);
}

.ant-row-rtl #components-layout-demo-top-side .logo {
  float: right;
  margin: 16px 0 16px 24px;
}

.site-layout-background {
  background: #fff;
}
</style>
