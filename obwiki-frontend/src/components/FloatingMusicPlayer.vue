<template>
  <div 
    class="floating-music-player"
    :style="{ left: position.x + 'px', top: position.y + 'px' }"
    @mousedown="startDrag"
    @touchstart="startDrag"
  >
    <!-- 悬浮按钮 -->
    <div class="floating-button" @click="handleClick">
      <span class="music-icon">🎵</span>
      <span class="music-text">音乐</span>
    </div>

    <!-- 播放器弹窗 -->
    <a-modal
      :visible="showPlayer"
      title="🎵 背景音乐播放器"
      @cancel="closePlayer"
      :footer="null"
      width="420px"
      :mask-closable="false"
    >
      <div class="music-player">
        <div class="controls">
          <a-upload
            :show-upload-list="false"
            accept="audio/*"
            :before-upload="handleLocalUpload"
          >
            <a-button type="primary" shape="round">本地音乐</a-button>
          </a-upload>
          <a-input-search
            v-model="musicUrl"
            placeholder="输入网络音乐URL"
            enter-button="添加"
            @search="addNetworkMusic"
            style="width: 220px; margin-left: 10px;"
          />
        </div>
        <div class="playlist">
          <div class="playlist-title">播放列表</div>
          <a-list
            bordered
            :data-source="playlist"
            style="max-height: 120px; overflow-y: auto;"
          >
            <template #renderItem="{ item, index }">
              <a-list-item>
                <span style="flex:1;">{{ item.name }}</span>
                <a @click="() => { currentIndex = index; play(); }" style="margin-right:8px;">播放</a>
                <a @click="() => removeMusic(index)" style="color:red;">删除</a>
              </a-list-item>
            </template>
          </a-list>
        </div>
        <div class="audio-controls">
          <a-button shape="circle" @click="prev">
            <template #icon>
              <span>&lt;</span>
            </template>
          </a-button>
          <a-button shape="circle" @click="togglePlay" style="margin: 0 10px;">
            <template #icon>
              <span v-if="isPlaying">⏸️</span>
              <span v-else>▶️</span>
            </template>
          </a-button>
          <a-button shape="circle" @click="next">
            <template #icon>
              <span>&gt;</span>
            </template>
          </a-button>
          <a-button shape="circle" @click="randomPlay" style="margin-left: 10px;">
            <template #icon>
              <span>🔀</span>
            </template>
          </a-button>
        </div>
        <div class="current-title" v-if="currentMusic">
          正在播放：{{ currentMusic.name }}
        </div>
        <audio
          ref="audioRef"
          :src="currentMusic ? currentMusic.url : ''"
          @ended="next"
          @play="isPlaying = true"
          @pause="isPlaying = false"
          style="width: 100%; margin-top: 10px;"
          controls
        />
      </div>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue';
import { message } from 'ant-design-vue';

interface MusicItem {
  name: string;
  url: string;
}

interface Position {
  x: number;
  y: number;
}

const showPlayer = ref(false);
const playlist = ref<MusicItem[]>([]);
const currentIndex = ref(0);
const isPlaying = ref(false);
const musicUrl = ref('');
const audioRef = ref<HTMLAudioElement | null>(null);

// 拖拽相关状态
const position = ref<Position>({ x: window.innerWidth - 100, y: window.innerHeight - 100 });
const isDragging = ref(false);
const dragOffset = ref<Position>({ x: 0, y: 0 });
const dragStartPosition = ref<Position>({ x: 0, y: 0 });
const hasDragged = ref(false);

const currentMusic = computed(() => playlist.value[currentIndex.value]);

// 初始化位置
onMounted(() => {
  // 从 localStorage 恢复位置
  const savedPosition = localStorage.getItem('musicPlayerPosition');
  if (savedPosition) {
    try {
      position.value = JSON.parse(savedPosition);
    } catch (e) {
      // 如果解析失败，使用默认位置
      position.value = { x: window.innerWidth - 100, y: window.innerHeight - 100 };
    }
  }
});

// 保存位置到 localStorage
function savePosition() {
  localStorage.setItem('musicPlayerPosition', JSON.stringify(position.value));
}

// 开始拖拽
function startDrag(event: MouseEvent | TouchEvent) {
  if (showPlayer.value) return; // 播放器打开时不允许拖拽
  
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

// 拖拽中
function onDrag(event: MouseEvent | TouchEvent) {
  if (!isDragging.value) return;
  
  const clientX = 'touches' in event ? event.touches[0].clientX : event.clientX;
  const clientY = 'touches' in event ? event.touches[0].clientY : event.clientY;
  
  // 检查拖拽距离，如果超过阈值则标记为已拖拽
  const dragDistance = Math.sqrt(
    Math.pow(clientX - dragStartPosition.value.x, 2) + 
    Math.pow(clientY - dragStartPosition.value.y, 2)
  );
  if (dragDistance > 5) { // 5px 的拖拽阈值
    hasDragged.value = true;
  }
  
  const newX = clientX - dragOffset.value.x;
  const newY = clientY - dragOffset.value.y;
  
  // 限制在屏幕范围内
  const maxX = window.innerWidth - 60;
  const maxY = window.innerHeight - 60;
  
  position.value = {
    x: Math.max(0, Math.min(newX, maxX)),
    y: Math.max(0, Math.min(newY, maxY))
  };
  
  event.preventDefault();
}

// 停止拖拽
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

// 清理事件监听器
onUnmounted(() => {
  document.removeEventListener('mousemove', onDrag);
  document.removeEventListener('touchmove', onDrag);
  document.removeEventListener('mouseup', stopDrag);
  document.removeEventListener('touchend', stopDrag);
});

// 处理点击事件
function handleClick(event: MouseEvent) {
  // 如果正在拖拽或已经拖拽过，不触发点击事件
  if (isDragging.value || hasDragged.value) {
    return;
  }
  togglePlayer();
}

function togglePlayer() {
  showPlayer.value = !showPlayer.value;
}

function closePlayer() {
  showPlayer.value = false;
}

function handleLocalUpload(file: File) {
  const url = URL.createObjectURL(file);
  playlist.value.push({ name: file.name, url });
  if (playlist.value.length === 1) {
    currentIndex.value = 0;
    play();
  }
  message.success('本地音乐添加成功');
  return false;
}

function addNetworkMusic() {
  if (!musicUrl.value) return;
  playlist.value.push({ name: musicUrl.value.split('/').pop() || '网络音乐', url: musicUrl.value });
  if (playlist.value.length === 1) {
    currentIndex.value = 0;
    play();
  }
  musicUrl.value = '';
  message.success('网络音乐添加成功');
}

function play() {
  if (audioRef.value) {
    audioRef.value.play();
  }
}

function pause() {
  if (audioRef.value) {
    audioRef.value.pause();
  }
}

function togglePlay() {
  if (isPlaying.value) {
    pause();
  } else {
    play();
  }
}

function prev() {
  if (playlist.value.length === 0) return;
  currentIndex.value = (currentIndex.value - 1 + playlist.value.length) % playlist.value.length;
  play();
}

function next() {
  if (playlist.value.length === 0) return;
  currentIndex.value = (currentIndex.value + 1) % playlist.value.length;
  play();
}

function randomPlay() {
  if (playlist.value.length === 0) return;
  let idx = Math.floor(Math.random() * playlist.value.length);
  while (idx === currentIndex.value && playlist.value.length > 1) {
    idx = Math.floor(Math.random() * playlist.value.length);
  }
  currentIndex.value = idx;
  play();
}

function removeMusic(idx: number) {
  playlist.value.splice(idx, 1);
  if (currentIndex.value >= playlist.value.length) {
    currentIndex.value = 0;
  }
  if (playlist.value.length === 0) {
    pause();
  } else {
    play();
  }
}
</script>

<style scoped>
.floating-music-player {
  position: fixed;
  z-index: 1000;
  user-select: none;
  cursor: grab;
}

.floating-music-player:active {
  cursor: grabbing;
}

.floating-button {
  width: 64px;
  height: 64px;
  background: linear-gradient(135deg, #36d1c4 0%, #5b86e5 100%);
  border-radius: 50%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  box-shadow: 0 8px 24px rgba(54, 209, 196, 0.25), 0 2px 8px rgba(91, 134, 229, 0.15);
  transition: all 0.25s cubic-bezier(.4,2,.6,1);
  color: #fff;
  font-size: 13px;
  border: 2px solid #fff;
  outline: none;
}

.floating-button:hover {
  transform: scale(1.13) rotate(-8deg);
  box-shadow: 0 12px 32px rgba(54, 209, 196, 0.32), 0 4px 16px rgba(91, 134, 229, 0.18);
  filter: brightness(1.08);
}

.music-icon {
  font-size: 20px;
  margin-bottom: 2px;
}

.music-text {
  font-size: 10px;
}

.music-player {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.controls {
  display: flex;
  margin-bottom: 10px;
}

.playlist {
  width: 100%;
  margin-bottom: 10px;
}

.playlist-title {
  font-weight: bold;
  margin-bottom: 4px;
}

.audio-controls {
  display: flex;
  justify-content: center;
  margin-bottom: 8px;
}

.current-title {
  margin-bottom: 4px;
  color: #1890ff;
  font-size: 14px;
}
</style> 