<template>
  <div
    class="floating-ai-fab"
    :style="{ left: position.x + 'px', top: position.y + 'px' }"
  >
    <div 
      class="ai-fab-btn" 
      @mousedown="startDrag"
      @touchstart="startDrag"
      @click="handleFabClick"
    >
      <span class="ai-fab-wave"></span>
      <div class="ai-fab-inner">
        <span class="ai-fab-icon">💡</span>
        <span class="ai-fab-text">AI</span>
      </div>
    </div>
    <!-- 自定义弹窗，紧挨按钮右侧展开 -->
    <div v-if="showChat" class="ai-popup" :style="popupStyle">
      <div class="ai-modal-bg">
        <div class="ai-waves">
          <div class="wave wave1"></div>
          <div class="wave wave2"></div>
          <div class="wave wave3"></div>
        </div>
        <div class="ai-bubbles">
          <div class="bubble b1"></div>
          <div class="bubble b2"></div>
          <div class="bubble b3"></div>
        </div>
      </div>
      <div class="ai-content">
        <!-- 关闭按钮 -->
        <button class="ai-popup-close" @click="closeChat">✕</button>
        <!-- 标题区 -->
        <div class="ai-title">
          <span class="ai-title-icon">🤖</span>
          <span class="ai-title-text">AI 智能助手</span>
        </div>
        <!-- AI对话内容 -->
        <div class="ai-chat-container">
          <AIFloatMenu />
        </div>
        <!-- 拉伸手柄 -->
        <div 
          class="ai-resize-handle"
          @mousedown="startResize"
          @touchstart="startResize"
        ></div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue';
import AIFloatMenu from './AIFloatMenu.vue';

interface Position {
  x: number;
  y: number;
}

interface Size {
  width: number;
  height: number;
}

const showChat = ref(false);
const position = ref<Position>({ x: window.innerWidth - 100, y: window.innerHeight - 180 });
const size = ref<Size>({ width: 480, height: 600 });
const isDragging = ref(false);
const isResizing = ref(false);
const dragOffset = ref<Position>({ x: 0, y: 0 });
const dragStartPosition = ref<Position>({ x: 0, y: 0 });
const hasDragged = ref(false);
const resizeStartSize = ref<Size>({ width: 0, height: 0 });
const resizeStartPosition = ref<Position>({ x: 0, y: 0 });

onMounted(() => {
  const savedPosition = localStorage.getItem('aiFabPosition');
  if (savedPosition) {
    try {
      position.value = JSON.parse(savedPosition);
    } catch (e) {
      position.value = { x: window.innerWidth - 100, y: window.innerHeight - 180 };
    }
  }
  
  const savedSize = localStorage.getItem('aiFabSize');
  if (savedSize) {
    try {
      size.value = JSON.parse(savedSize);
    } catch (e) {
      size.value = { width: 480, height: 600 };
    }
  }
});

function savePosition() {
  localStorage.setItem('aiFabPosition', JSON.stringify(position.value));
}

function saveSize() {
  localStorage.setItem('aiFabSize', JSON.stringify(size.value));
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

function startResize(event: MouseEvent | TouchEvent) {
  isResizing.value = true;
  const clientX = 'touches' in event ? event.touches[0].clientX : event.clientX;
  const clientY = 'touches' in event ? event.touches[0].clientY : event.clientY;
  resizeStartSize.value = { ...size.value };
  resizeStartPosition.value = { x: clientX, y: clientY };
  document.addEventListener('mousemove', onResize);
  document.addEventListener('touchmove', onResize);
  document.addEventListener('mouseup', stopResize);
  document.addEventListener('touchend', stopResize);
  event.preventDefault();
}

function onResize(event: MouseEvent | TouchEvent) {
  if (!isResizing.value) return;
  const clientX = 'touches' in event ? event.touches[0].clientX : event.clientX;
  const clientY = 'touches' in event ? event.touches[0].clientY : event.clientY;
  
  const deltaX = clientX - resizeStartPosition.value.x;
  const deltaY = clientY - resizeStartPosition.value.y;
  
  const newWidth = Math.max(400, Math.min(800, resizeStartSize.value.width + deltaX));
  const newHeight = Math.max(500, Math.min(800, resizeStartSize.value.height + deltaY));
  
  size.value = { width: newWidth, height: newHeight };
  event.preventDefault();
}

function stopResize() {
  if (isResizing.value) {
    isResizing.value = false;
    saveSize();
  }
  document.removeEventListener('mousemove', onResize);
  document.removeEventListener('touchmove', onResize);
  document.removeEventListener('mouseup', stopResize);
  document.removeEventListener('touchend', stopResize);
}

function handleFabClick() {
  // 如果正在拖拽或刚刚拖拽过，不触发点击
  if (isDragging.value || hasDragged.value) {
    hasDragged.value = false; // 重置
    return;
  }
  toggleChat();
}

function toggleChat() {
  showChat.value = !showChat.value;
}

function closeChat() {
  showChat.value = false;
}

const popupStyle = computed(() => {
  // 弹窗右侧展开，距离按钮8px
  const btnWidth = 64; // ai-fab-btn宽度
  const gap = 8;
  let left = position.value.x + btnWidth + gap;
  let top = position.value.y;
  // 防止超出右边界
  if (left + size.value.width > window.innerWidth) {
    left = window.innerWidth - size.value.width - 16;
  }
  // 防止超出下边界
  if (top + size.value.height > window.innerHeight) {
    top = window.innerHeight - size.value.height - 16;
  }
  if (top < 0) top = 16;
  return {
    position: 'fixed' as const,
    left: left + 'px',
    top: top + 'px',
    width: size.value.width + 'px',
    height: size.value.height + 'px',
    zIndex: 1100 as const
  };
});

onUnmounted(() => {
  document.removeEventListener('mousemove', onDrag);
  document.removeEventListener('touchmove', onDrag);
  document.removeEventListener('mouseup', stopDrag);
  document.removeEventListener('touchend', stopDrag);
  document.removeEventListener('mousemove', onResize);
  document.removeEventListener('touchmove', onResize);
  document.removeEventListener('mouseup', stopResize);
  document.removeEventListener('touchend', stopResize);
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
.ai-fab-wave {
  position: absolute;
  width: 90px;
  height: 90px;
  left: 50%;
  top: 50%;
  transform: translate(-50%, -50%);
  border-radius: 50%;
  background: radial-gradient(circle, rgba(91,134,229,0.18) 60%, transparent 100%);
  animation: aiFabWave 2.5s infinite linear;
  z-index: 0;
}
@keyframes aiFabWave {
  0% { opacity: 0.7; transform: translate(-50%, -50%) scale(1); }
  70% { opacity: 0.2; transform: translate(-50%, -50%) scale(1.3); }
  100% { opacity: 0; transform: translate(-50%, -50%) scale(1.5); }
}
.ai-fab-inner {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  z-index: 1;
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
.ai-popup {
  box-shadow: 0 20px 40px rgba(0,0,0,0.13);
  border-radius: 22px;
  overflow: visible;
  background: transparent;
  border: none;
  padding: 0;
  animation: aiPopupFadeIn 0.25s cubic-bezier(.4,2,.6,1);
}
@keyframes aiPopupFadeIn {
  from { opacity: 0; transform: translateY(20px) scale(0.98); }
  to { opacity: 1; transform: translateY(0) scale(1); }
}
.ai-popup-close {
  position: absolute;
  right: 18px;
  top: 18px;
  z-index: 2;
  background: rgba(91,134,229,0.13);
  border: none;
  color: #fff;
  font-size: 20px;
  border-radius: 50%;
  width: 36px;
  height: 36px;
  cursor: pointer;
  transition: background 0.2s;
}
.ai-popup-close:hover {
  background: rgba(255,107,107,0.23);
  color: #ff6b6b;
}
.ai-modal-bg {
  position: absolute;
  left: 0; right: 0; top: 0; bottom: 0;
  z-index: 0;
  pointer-events: none;
}
.ai-waves {
  position: absolute;
  left: 0; right: 0; top: 0; height: 90px;
  overflow: hidden;
}
.wave {
  position: absolute;
  width: 200%;
  height: 100px;
  left: -50%;
  background: rgba(91,134,229,0.18);
  border-radius: 43% 57% 60% 40% / 60% 40% 60% 40%;
  opacity: 0.7;
  animation: aiWave 8s linear infinite;
}
.wave1 { top: 0; animation-delay: 0s; }
.wave2 { top: 20px; opacity: 0.5; animation-delay: 2s; }
.wave3 { top: 40px; opacity: 0.3; animation-delay: 4s; }
@keyframes aiWave {
  0% { left: -50%; }
  100% { left: 0%; }
}
.ai-bubbles {
  position: absolute;
  left: 0; right: 0; bottom: 0; height: 100%;
  pointer-events: none;
}
.bubble {
  position: absolute;
  border-radius: 50%;
  background: rgba(255,255,255,0.13);
  animation: aiBubbleFloat 6s ease-in-out infinite;
}
.b1 { width: 36px; height: 36px; left: 10%; bottom: 20%; animation-delay: 0s; }
.b2 { width: 22px; height: 22px; left: 70%; bottom: 10%; animation-delay: 2s; }
.b3 { width: 28px; height: 28px; left: 40%; bottom: 30%; animation-delay: 4s; }
@keyframes aiBubbleFloat {
  0%,100% { transform: translateY(0); }
  50% { transform: translateY(-30px); }
}
.ai-content {
  position: relative;
  z-index: 1;
  padding: 40px 30px 30px 30px;
  background: linear-gradient(135deg, #2c3e50 60%, #5b86e5 100%);
  border-radius: 22px;
  height: 100%;
  box-shadow: 0 20px 40px rgba(0,0,0,0.13);
  border: 2.5px solid #5b86e5;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}
.ai-title {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 22px;
  font-weight: bold;
  color: #fff;
  margin-bottom: 18px;
  letter-spacing: 2px;
}
.ai-title-icon {
  font-size: 26px;
}
.ai-chat-container {
  flex: 1;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}
.ai-resize-handle {
  position: absolute;
  right: 0;
  bottom: 0;
  width: 30px;
  height: 30px;
  cursor: nw-resize;
  background: linear-gradient(-45deg, transparent 30%, rgba(91,134,229,0.4) 30%, rgba(91,134,229,0.4) 70%, transparent 70%);
  border-radius: 0 0 22px 0;
  z-index: 10;
  transition: all 0.2s;
  display: flex;
  align-items: center;
  justify-content: center;
}
.ai-resize-handle::before {
  content: '⤡';
  color: rgba(91,134,229,0.8);
  font-size: 16px;
  font-weight: bold;
}
.ai-resize-handle:hover {
  background: linear-gradient(-45deg, transparent 30%, rgba(91,134,229,0.6) 30%, rgba(91,134,229,0.6) 70%, transparent 70%);
  transform: scale(1.1);
}
.ai-resize-handle:hover::before {
  color: rgba(91,134,229,1);
}
</style> 