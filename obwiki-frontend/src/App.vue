<template>
  <a-layout>
    <the-header />

    <router-view />

    <the-footer />

    <FloatingAIFab />

    <FloatingMusicPlayer />
  </a-layout>
</template>

<script lang="ts" setup>
import TheFooter from "@/components/the-footer.vue";
import TheHeader from "@/components/the-header.vue";
import FloatingMusicPlayer from '@/components/FloatingMusicPlayer.vue';
import FloatingAIFab from '@/components/FloatingAIFab.vue';
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
