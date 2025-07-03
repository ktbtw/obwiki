<template>
  <div ref="globeContainer" class="earth-container"></div>
</template>

<script setup lang="ts">
import { onBeforeUnmount, onMounted, ref } from 'vue';
// @ts-ignore
import Globe from 'globe.gl';

const globeContainer = ref<HTMLElement | null>(null);
let globeInstance: any = null;

const markers = [
  { lat: 39.9042, lng: 116.4074, size: 0.2, color: 'red', label: '北京', value: 100 },
  { lat: 31.2304, lng: 121.4737, size: 0.2, color: 'blue', label: '上海', value: 80 },
  { lat: 34.0522, lng: -118.2437, size: 0.2, color: 'green', label: '洛杉矶', value: 60 },
  // 可继续添加更多标记点
];

onMounted(() => {
  if (globeContainer.value) {
    // @ts-ignore
    globeInstance = (Globe as any)()(globeContainer.value)
      .globeImageUrl('//unpkg.com/three-globe/example/img/earth-blue-marble.jpg')
      .pointsData(markers)
      .pointLat('lat')
      .pointLng('lng')
      .pointColor('color')
      .pointAltitude('size')
      .pointLabel('label')
      .pointRadius(0.6)
      .onPointClick((point: any) => {
        alert(`${point.label} 数据值: ${point.value}`);
      });
    globeInstance.controls().autoRotate = true;
    globeInstance.controls().autoRotateSpeed = 0.5;
  }
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