<template>
  <div
    class="floating-ai-fab"
    :style="{ left: position.x + 'px', top: position.y + 'px' }"
    @mousedown="startDrag"
    @touchstart="startDrag"
  >
    <div class="ai-fab-btn" @click="handleClick">
      <span class="ai-fab-icon">💡</span>
      <span class="ai-fab-text">AI</span>
    </div>
    <a-drawer
      title="AI 对话"
      placement="right"
      :width="360"
      :open="drawerVisible"
      @close="drawerVisible = false"
    >
      <AIFloatMenu />
    </a-drawer>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue';
import AIFloatMenu from '@/components/AIFloatMenu.vue';

interface Position {
  x: number;
  y: number;
}

const drawerVisible = ref(false);
const position = ref<Position>({ x: window.innerWidth - 100, y: window.innerHeight - 180 });
const isDragging = ref(false);
const dragOffset = ref<Position>({ x: 0, y: 0 });
const dragStartPosition = ref<Position>({ x: 0, y: 0 });
const hasDragged = ref(false);

onMounted(() => {
  const savedPosition = localStorage.getItem('aiFabPosition');
  if (savedPosition) {
    try {
      position.value = JSON.parse(savedPosition);
    } catch (e) {
      position.value = { x: window.innerWidth - 100, y: window.innerHeight - 180 };
    }
  }
});

function savePosition() {
  localStorage.setItem('aiFabPosition', JSON.stringify(position.value));
}

function startDrag(event: MouseEvent | TouchEvent) {
  isDragging.value = true;
  hasDragged.value = false;
  const clientX = 'touches' in event ? event.touches[0].clientX : event.clientX;
  const clientY = 'touches' in event ? event.touches[0].clientY : event.clientY;
  dragStartPosition.value = { x: clientX, y: clientY };
  dragOffset.value = {
    x: clientX - position.value.x,
    y: clientY - position.value.y
  };
  document.addEventListener('mousemove', onDrag);
  document.addEventListener('touchmove', onDrag);
  document.addEventListener('mouseup', stopDrag);
  document.addEventListener('touchend', stopDrag);
  event.preventDefault();
}

function onDrag(event: MouseEvent | TouchEvent) {
  if (!isDragging.value) return;
  const clientX = 'touches' in event ? event.touches[0].clientX : event.clientX;
  const clientY = 'touches' in event ? event.touches[0].clientY : event.clientY;
  const dragDistance = Math.sqrt(
    Math.pow(clientX - dragStartPosition.value.x, 2) +
    Math.pow(clientY - dragStartPosition.value.y, 2)
  );
  if (dragDistance > 5) {
    hasDragged.value = true;
  }
  const newX = clientX - dragOffset.value.x;
  const newY = clientY - dragOffset.value.y;
  const maxX = window.innerWidth - 60;
  const maxY = window.innerHeight - 60;
  position.value = {
    x: Math.max(0, Math.min(newX, maxX)),
    y: Math.max(0, Math.min(newY, maxY))
  };
  event.preventDefault();
}

function stopDrag() {
  if (isDragging.value) {
    isDragging.value = false;
    savePosition();
  }
  document.removeEventListener('mousemove', onDrag);
  document.removeEventListener('touchmove', onDrag);
  document.removeEventListener('mouseup', stopDrag);
  document.removeEventListener('touchend', stopDrag);
}

function handleClick(event: MouseEvent) {
  if (isDragging.value || hasDragged.value) {
    return;
  }
  drawerVisible.value = true;
}

onUnmounted(() => {
  document.removeEventListener('mousemove', onDrag);
  document.removeEventListener('touchmove', onDrag);
  document.removeEventListener('mouseup', stopDrag);
  document.removeEventListener('touchend', stopDrag);
});
</script>

<style scoped>
.floating-ai-fab {
  position: fixed;
  z-index: 1000;
  user-select: none;
  cursor: grab;
}
.floating-ai-fab:active {
  cursor: grabbing;
}
.ai-fab-btn {
  width: 64px;
  height: 64px;
  background: linear-gradient(135deg, #5b86e5 0%, #36d1c4 100%);
  border-radius: 50%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  box-shadow: 0 8px 32px rgba(91,134,229,0.25), 0 2px 8px rgba(54,209,196,0.15);
  transition: all 0.25s cubic-bezier(.4,2,.6,1);
  color: #fff;
  font-size: 15px;
  border: 2.5px solid #fff;
  outline: none;
  position: relative;
  overflow: visible;
}
.ai-fab-btn:hover {
  transform: scale(1.13) rotate(8deg);
  box-shadow: 0 16px 40px rgba(91,134,229,0.32), 0 4px 16px rgba(54,209,196,0.18);
  filter: brightness(1.10);
}
.ai-fab-icon {
  font-size: 28px;
  margin-bottom: 2px;
  text-shadow: 0 0 8px #36d1c4, 0 0 16px #5b86e5;
}
.ai-fab-text {
  font-size: 13px;
  letter-spacing: 2px;
  font-weight: bold;
  text-shadow: 0 0 4px #fff, 0 0 8px #36d1c4;
}
</style> 