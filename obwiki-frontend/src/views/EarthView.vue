<template>
  <div style="position: relative;">
    <div style="position: absolute; top: 20px; right: 30px; z-index: 10; display: flex; gap: 12px;">
      <a-select v-model:value="colorType" style="width: 140px;">
        <a-select-option value="single">单色</a-select-option>
        <a-select-option value="multi">多色</a-select-option>
      </a-select>
      <a-select v-model:value="heightType" style="width: 140px;">
        <a-select-option value="flat">贴地</a-select-option>
        <a-select-option value="bar">柱状</a-select-option>
      </a-select>
    </div>
    <div ref="globeContainer" class="earth-container"></div>
  </div>
</template>

<script setup lang="ts">
import api from '@/api/index';
import { onBeforeUnmount, onMounted, ref, watch } from 'vue';
// @ts-ignore
import Globe from 'globe.gl';

const globeContainer = ref<HTMLElement | null>(null);
let globeInstance: any = null;

const markers = ref<any[]>([]);
const colorType = ref('single'); // 单色 or 多色
const heightType = ref('flat');  // 贴地 or 柱状

const updateGlobeStyle = () => {
  if (!globeInstance) return;
  // 动态切换高度
  if (heightType.value === 'bar') {
    globeInstance.pointAltitude(() => 0.2);
  } else {
    globeInstance.pointAltitude(() => 0);
  }
  // 动态切换颜色
  globeInstance.pointColor((d: any) => {
    if (colorType.value === 'multi') {
      const colors = ['red', 'blue', 'green', 'orange', 'purple', 'cyan'];
      // 用下标分配颜色更稳妥
      return colors[d._idx % colors.length];
    }
    return d.color || 'blue';
  });
};

onMounted(() => {
  api.get('/doc/all').then(resp => {
    const docs = resp.data.content || [];
    markers.value = docs
      .filter((item: any) => item.lat && item.lng)
      .map((item: any, idx: number) => ({
        lat: item.lat,
        lng: item.lng,
        color: 'blue',
        label: item.name,
        value: item.viewCount || 0,
        id: item.id,
        _idx: idx // 用于多色分配
      }));
    if (globeContainer.value) {
      globeInstance = (Globe as any)()(globeContainer.value)
        .globeImageUrl('//unpkg.com/three-globe/example/img/earth-blue-marble.jpg')
        .pointsData(markers.value)
        .pointLat('lat')
        .pointLng('lng')
        .pointColor('blue')
        .pointLabel('label')
        .pointRadius(0.6)
        .onPointClick((point: any) => {
          alert(`${point.label} 数据值: ${point.value}`);
        });
      globeInstance.controls().autoRotate = true;
      globeInstance.controls().autoRotateSpeed = 0.5;
      updateGlobeStyle();
    }
  });
});

watch([colorType, heightType], () => {
  updateGlobeStyle();
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