<template>
  <div ref="globeContainer" class="earth-container"></div>
</template>

<script setup lang="ts">
import api from '@/api/index';
import { onBeforeUnmount, onMounted, ref } from 'vue';
// @ts-ignore
import Globe from 'globe.gl';

const globeContainer = ref<HTMLElement | null>(null);
let globeInstance: any = null;

const markers = ref<any[]>([]);

onMounted(() => {
  // 自动获取所有文档数据，提取有经纬度的文档作为地球点
  api.get('/doc/all').then(resp => {
    const docs = resp.data.content || [];
    markers.value = docs
      .filter((item: any) => item.lat && item.lng)
      .map((item: any) => ({
        lat: item.lat,
        lng: item.lng,
        color: 'blue',
        label: item.name,
        value: item.viewCount || 0
      }));
    // 渲染地球
    if (globeContainer.value) {
      globeInstance = (Globe as any)()(globeContainer.value)
        .globeImageUrl('//unpkg.com/three-globe/example/img/earth-blue-marble.jpg')
        .pointsData(markers.value)
        .pointLat('lat')
        .pointLng('lng')
        .pointColor('color')
        .pointLabel('label')
        .pointRadius(0.6)
        .onPointClick((point: any) => {
          alert(`${point.label} 数据值: ${point.value}`);
        });
      globeInstance.controls().autoRotate = true;
      globeInstance.controls().autoRotateSpeed = 0.5;
    }
  });
});

onBeforeUnmount(() => {
  if (globeInstance && globeInstance.renderer()) {
    globeInstance.renderer().dispose();
  }
});
</script>

<style scoped>
.earth-container {
  width: 100%;
  height: 600px;
  min-height: 400px;
  background: #e6f7ff;
  border-radius: 12px;
  margin: 0 auto;
}
</style> 