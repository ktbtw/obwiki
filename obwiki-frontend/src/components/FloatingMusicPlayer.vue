<template>
  <div
    class="ocean-floating-player"
    :style="{ left: position.x + 'px', top: position.y + 'px' }"
  >
    <!-- 海洋水滴悬浮按钮 -->
    <div
      class="ocean-fab"
      @mousedown="startDrag"
      @touchstart="startDrag"
      @click="handleFabClick"
    >
      <span class="fab-wave"></span>
      <div class="fab-inner">
        <span class="fab-icon">🌊</span>
        <span class="fab-text">音乐</span>
      </div>
    </div>
    <!-- 自定义弹窗，紧挨按钮右侧展开 -->
    <div v-if="showPlayer" class="ocean-popup" :style="popupStyle">
      <div class="ocean-modal-bg">
        <div class="ocean-waves">
          <div class="wave wave1"></div>
          <div class="wave wave2"></div>
          <div class="wave wave3"></div>
        </div>
        <div class="ocean-bubbles">
          <div class="bubble b1"></div>
          <div class="bubble b2"></div>
          <div class="bubble b3"></div>
        </div>
      </div>
      <div class="ocean-content">
        <!-- 关闭按钮 -->
        <button class="ocean-popup-close" @click="closePlayer">✕</button>
        <!-- 标题区 -->
        <div class="ocean-title">
          <span class="ocean-title-icon">🐚</span>
          <span class="ocean-title-text">海洋音乐播放器</span>
        </div>
        <!-- 播放信息区 -->
        <div class="ocean-nowplaying" v-if="currentMusic">
          <div class="ocean-disc-wrap">
            <div class="ocean-disc" :class="{ spinning: isPlaying }">
              <div class="ocean-disc-center"></div>
              <div class="ocean-disc-wave" v-if="isPlaying"></div>
            </div>
          </div>
          <div class="ocean-track-info">
            <div class="ocean-track-name">{{ currentMusic.name }}</div>
            <div class="ocean-track-status">
              <template v-if="isLoading">加载中...</template>
              <template v-else>{{ isPlaying ? '正在畅游' : '已暂停' }}</template>
            </div>
            <!-- 播放进度条 -->
            <div class="ocean-progress-bar">
              <span class="ocean-time">{{ formatTime(currentTime) }}</span>
              <input
                type="range"
                min="0"
                :max="duration"
                step="0.1"
                :value="currentTime"
                @mousedown="onSeekStart"
                @touchstart="onSeekStart"
                @input="onSeekInput"
                @change="onSeekEnd"
                class="progress-range"
                :disabled="!duration"
              />
              <span class="ocean-time">{{ formatTime(duration) }}</span>
            </div>
          </div>
        </div>
        <!-- 控制区 -->
        <div class="ocean-controls">
          <button class="ocean-btn" @click="prev" title="上一曲">⏮</button>
          <button class="ocean-btn main" @click="togglePlay" title="播放/暂停">
            <span v-if="isPlaying">⏸</span>
            <span v-else>▶</span>
          </button>
          <button class="ocean-btn" @click="next" title="下一曲">⏭</button>
          <button class="ocean-btn shuffle" :class="{ active: isShuffleMode }" @click="toggleShuffleMode" title="随机播放">
            🔀
          </button>
        </div>
        <!-- 添加音乐区 -->
        <div class="ocean-add">
          <a-upload :show-upload-list="false" accept="audio/*" :before-upload="handleLocalUpload">
            <button class="ocean-add-btn">
              <span>📁 本地</span>
            </button>
          </a-upload>
          <div class="ocean-url-group">
            <input v-model="musicUrl" placeholder="输入音乐URL..." class="ocean-url-input" @keyup.enter="addNetworkMusic" />
            <button class="ocean-add-btn" @click="addNetworkMusic" title="添加网络音乐">🔗</button>
          </div>
        </div>
        <!-- 播放列表区 -->
        <div class="ocean-playlist">
          <div class="ocean-playlist-title">
            <span>🎶 播放列表</span>
            <span class="ocean-count">({{ playlist.length }})</span>
          </div>
          <div class="ocean-list">
            <div v-if="playlist.length === 0" class="ocean-empty">
              <span>🌊 暂无音乐，快来添加吧！</span>
            </div>
            <div v-else>
              <div
                v-for="(item, index) in playlist"
                :key="index"
                class="ocean-track-item"
                :class="{ active: currentIndex === index }"
                @click="() => selectTrack(index)"
              >
                <div class="ocean-track-main">
                  <span class="ocean-track-num">{{ index + 1 }}</span>
                  <span class="ocean-track-title">
                    <template v-if="editingIndex === index">
                      <input
                        :id="'edit-music-name-' + index"
                        v-model="editingName"
                        class="edit-music-input"
                        @keyup.enter="saveEditName(index)"
                        @blur="saveEditName(index)"
                        @keyup.esc="cancelEditName"
                        maxlength="40"
                      />
                    </template>
                    <template v-else>
                      {{ item.name }}
                    </template>
                  </span>
                  <button class="ocean-track-btn edit" @click.stop="startEditName(index)" v-if="editingIndex !== index" title="重命名">✏️</button>
                </div>
                <div class="ocean-track-actions">
                  <button class="ocean-track-btn del" @click.stop="() => removeMusic(index)">✕</button>
                </div>
              </div>
            </div>
          </div>
        </div>
        <!-- 隐藏音频播放器 -->
        <audio
          v-if="currentMusic"
          ref="audioRef"
          :src="currentMusic.url"
          @ended="next"
          @play="isPlaying = true"
          @pause="isPlaying = false"
          @timeupdate="onTimeUpdate"
          @loadedmetadata="onLoadedMetadata"
          @loadstart="onAudioLoadStart"
          @canplay="onAudioCanPlay"
          class="ocean-audio"
          controls
        />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, watch } from 'vue';
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
const position = ref<Position>({ x: window.innerWidth - 100, y: window.innerHeight - 100 });
const isDragging = ref(false);
const dragOffset = ref<Position>({ x: 0, y: 0 });
const dragStartPosition = ref<Position>({ x: 0, y: 0 });
const hasDragged = ref(false);
const currentMusic = computed(() => playlist.value[currentIndex.value]);
const editingIndex = ref(-1);
const editingName = ref('');
const currentTime = ref(0);
const duration = ref(0);
const isSeeking = ref(false);
const isLoading = ref(false);
const isShuffleMode = ref(false);
const shouldAutoPlay = ref(false);

function savePlaylist() {
  localStorage.setItem('musicPlayerPlaylist', JSON.stringify(playlist.value));
}

onMounted(() => {
  const savedPosition = localStorage.getItem('musicPlayerPosition');
  if (savedPosition) {
    try {
      position.value = JSON.parse(savedPosition);
    } catch (e) {
      position.value = { x: window.innerWidth - 100, y: window.innerHeight - 100 };
    }
  }
  // 恢复播放列表
  const savedPlaylist = localStorage.getItem('musicPlayerPlaylist');
  if (savedPlaylist) {
    try {
      playlist.value = JSON.parse(savedPlaylist);
    } catch (e) {
      playlist.value = [];
    }
  }
});
function savePosition() {
  localStorage.setItem('musicPlayerPosition', JSON.stringify(position.value));
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
onUnmounted(() => {
  document.removeEventListener('mousemove', onDrag);
  document.removeEventListener('touchmove', onDrag);
  document.removeEventListener('mouseup', stopDrag);
  document.removeEventListener('touchend', stopDrag);
});
function togglePlayer() {
  showPlayer.value = !showPlayer.value;
}
function closePlayer() {
  showPlayer.value = false;
}
function handleLocalUpload(file: File) {
  const reader = new FileReader();
  reader.onload = (e) => {
    const base64 = e.target?.result as string;
    playlist.value.push({ name: file.name, url: base64 });
    savePlaylist();
    if (playlist.value.length === 1) {
      currentIndex.value = 0;
      play();
    }
    message.success('本地音乐添加成功');
  };
  reader.readAsDataURL(file);
  return false;
}
function addNetworkMusic() {
  if (!musicUrl.value) return;
  playlist.value.push({ name: musicUrl.value.split('/').pop() || '网络音乐', url: musicUrl.value });
  savePlaylist();
  if (playlist.value.length === 1) {
    currentIndex.value = 0;
    play();
  }
  musicUrl.value = '';
  message.success('网络音乐添加成功');
}
function play() {
  if (audioRef.value) {
    // 如果是切歌等自动播放，交给canplay事件
    if (!shouldAutoPlay.value) {
      if (audioRef.value.readyState >= 2) {
        audioRef.value.play();
      } else {
        shouldAutoPlay.value = true;
      }
    }
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
  if (isShuffleMode.value) {
    let idx = Math.floor(Math.random() * playlist.value.length);
    while (idx === currentIndex.value && playlist.value.length > 1) {
      idx = Math.floor(Math.random() * playlist.value.length);
    }
    currentIndex.value = idx;
  } else {
    currentIndex.value = (currentIndex.value + 1) % playlist.value.length;
  }
  shouldAutoPlay.value = true;
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
  savePlaylist();
  if (currentIndex.value >= playlist.value.length) {
    currentIndex.value = 0;
  }
  if (playlist.value.length === 0) {
    pause();
  } else {
    play();
  }
}
function handleFabClick() {
  // 如果正在拖拽或刚刚拖拽过，不触发点击
  if (isDragging.value || hasDragged.value) {
    hasDragged.value = false; // 重置
    return;
  }
  togglePlayer();
}
function startEditName(idx: number) {
  editingIndex.value = idx;
  editingName.value = playlist.value[idx].name;
  // 等待下一个tick后聚焦输入框
  setTimeout(() => {
    const input = document.getElementById('edit-music-name-' + idx) as HTMLInputElement;
    if (input) input.focus();
  }, 0);
}
function saveEditName(idx: number) {
  if (editingName.value.trim() !== '') {
    playlist.value[idx].name = editingName.value.trim();
    savePlaylist();
    message.success('重命名成功');
  }
  editingIndex.value = -1;
}
function cancelEditName() {
  editingIndex.value = -1;
}
const popupStyle = computed(() => {
  // 弹窗右侧展开，距离按钮8px
  const btnWidth = 68; // ocean-fab宽度
  const gap = 8;
  let left = position.value.x + btnWidth + gap;
  let top = position.value.y;
  // 防止超出右边界
  const popupWidth = 480;
  if (left + popupWidth > window.innerWidth) {
    left = window.innerWidth - popupWidth - 16;
  }
  // 防止超出下边界
  const popupHeight = 540;
  if (top + popupHeight > window.innerHeight) {
    top = window.innerHeight - popupHeight - 16;
  }
  if (top < 0) top = 16;
  return {
    position: 'fixed' as const,
    left: left + 'px',
    top: top + 'px',
    zIndex: 1100 as const
  };
});
function onTimeUpdate() {
  if (!isSeeking.value && audioRef.value) {
    currentTime.value = audioRef.value.currentTime;
  }
}
function onLoadedMetadata() {
  if (audioRef.value) {
    duration.value = audioRef.value.duration;
  }
}
function onSeekStart() {
  isSeeking.value = true;
}
function onSeekEnd(e: Event) {
  if (audioRef.value) {
    const value = Number((e.target as HTMLInputElement).value);
    audioRef.value.currentTime = value;
    currentTime.value = value;
  }
  isSeeking.value = false;
}
function onSeekInput(e: Event) {
  currentTime.value = Number((e.target as HTMLInputElement).value);
}
function formatTime(sec: number) {
  if (isNaN(sec)) return '00:00';
  const m = Math.floor(sec / 60).toString().padStart(2, '0');
  const s = Math.floor(sec % 60).toString().padStart(2, '0');
  return `${m}:${s}`;
}
function onAudioLoadStart() {
  isLoading.value = true;
}
function onAudioCanPlay() {
  isLoading.value = false;
  if (shouldAutoPlay.value && audioRef.value) {
    audioRef.value.play();
    shouldAutoPlay.value = false;
  }
}
function toggleShuffleMode() {
  isShuffleMode.value = !isShuffleMode.value;
  message.success(isShuffleMode.value ? '随机播放模式已开启' : '随机播放模式已关闭');
}
function selectTrack(index: number) {
  currentIndex.value = index;
  shouldAutoPlay.value = true;
}
watch(currentMusic, () => {
  isLoading.value = true;
  currentTime.value = 0;
  duration.value = 0;
  isPlaying.value = false;
});
</script>

<style scoped>
.ocean-floating-player {
  position: fixed;
  z-index: 1000;
  user-select: none;
  cursor: grab;
}
.ocean-floating-player:active {
  cursor: grabbing;
}
.ocean-fab {
  width: 68px;
  height: 68px;
  background: linear-gradient(135deg, #3a8dde 0%, #36d1c4 100%);
  border-radius: 50%;
  box-shadow: 0 0 0 6px rgba(54, 209, 196, 0.10), 0 8px 24px rgba(54, 209, 196, 0.18);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  position: relative;
  cursor: pointer;
  transition: all 0.25s cubic-bezier(.4,2,.6,1);
  border: 2.5px solid #36d1c4;
  overflow: visible;
}
.fab-wave {
  position: absolute;
  width: 90px;
  height: 90px;
  left: 50%;
  top: 50%;
  transform: translate(-50%, -50%);
  border-radius: 50%;
  background: radial-gradient(circle, rgba(54,209,196,0.18) 60%, transparent 100%);
  animation: fabWave 2.5s infinite linear;
  z-index: 0;
}
@keyframes fabWave {
  0% { opacity: 0.7; transform: translate(-50%, -50%) scale(1); }
  70% { opacity: 0.2; transform: translate(-50%, -50%) scale(1.3); }
  100% { opacity: 0; transform: translate(-50%, -50%) scale(1.5); }
}
.fab-inner {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  z-index: 1;
}
.fab-icon {
  font-size: 28px;
  z-index: 1;
  position: relative;
  margin-bottom: 2px;
}
.fab-text {
  font-size: 12px;
  color: #e6f7ff;
  margin-top: 0;
  display: block;
  text-align: center;
  letter-spacing: 2px;
  font-weight: 600;
  text-shadow: 0 1px 4px rgba(54,209,196,0.18), 0 0px 2px #223a5f;
  user-select: none;
  pointer-events: none;
}
.ocean-popup {
  min-width: 420px;
  max-width: 520px;
  min-height: 520px;
  box-shadow: 0 20px 40px rgba(0,0,0,0.13);
  border-radius: 22px;
  overflow: visible;
  background: transparent;
  border: none;
  padding: 0;
  animation: popupFadeIn 0.25s cubic-bezier(.4,2,.6,1);
}
@keyframes popupFadeIn {
  from { opacity: 0; transform: translateY(20px) scale(0.98); }
  to { opacity: 1; transform: translateY(0) scale(1); }
}
.ocean-popup-close {
  position: absolute;
  right: 18px;
  top: 18px;
  z-index: 2;
  background: rgba(54,209,196,0.13);
  border: none;
  color: #fff;
  font-size: 20px;
  border-radius: 50%;
  width: 36px;
  height: 36px;
  cursor: pointer;
  transition: background 0.2s;
}
.ocean-popup-close:hover {
  background: rgba(255,107,107,0.23);
  color: #ff6b6b;
}
.ocean-music-modal :deep(.ant-modal) {
  background: transparent !important;
  box-shadow: none !important;
}
.ocean-music-modal :deep(.ant-modal-content) {
  background: transparent !important;
  border: none !important;
  box-shadow: none !important;
  color: #fff;
}
.ocean-music-modal :deep(.ant-modal-body) {
  background: transparent !important;
  padding: 0 !important;
}
.ocean-modal-bg {
  position: absolute;
  left: 0; right: 0; top: 0; bottom: 0;
  z-index: 0;
  pointer-events: none;
}
.ocean-waves {
  position: absolute;
  left: 0; right: 0; top: 0; height: 90px;
  overflow: hidden;
}
.wave {
  position: absolute;
  width: 200%;
  height: 100px;
  left: -50%;
  background: rgba(54,209,196,0.18);
  border-radius: 43% 57% 60% 40% / 60% 40% 60% 40%;
  opacity: 0.7;
  animation: oceanWave 8s linear infinite;
}
.wave1 { top: 0; animation-delay: 0s; }
.wave2 { top: 20px; opacity: 0.5; animation-delay: 2s; }
.wave3 { top: 40px; opacity: 0.3; animation-delay: 4s; }
@keyframes oceanWave {
  0% { left: -50%; }
  100% { left: 0%; }
}
.ocean-bubbles {
  position: absolute;
  left: 0; right: 0; bottom: 0; height: 100%;
  pointer-events: none;
}
.bubble {
  position: absolute;
  border-radius: 50%;
  background: rgba(255,255,255,0.13);
  animation: bubbleFloat 6s ease-in-out infinite;
}
.b1 { width: 36px; height: 36px; left: 10%; bottom: 20%; animation-delay: 0s; }
.b2 { width: 22px; height: 22px; left: 70%; bottom: 10%; animation-delay: 2s; }
.b3 { width: 28px; height: 28px; left: 40%; bottom: 30%; animation-delay: 4s; }
@keyframes bubbleFloat {
  0%,100% { transform: translateY(0); }
  50% { transform: translateY(-30px); }
}
.ocean-content {
  position: relative;
  z-index: 1;
  padding: 40px 30px 30px 30px;
  background: linear-gradient(135deg, #223a5f 60%, #36d1c4 100%);
  border-radius: 22px;
  min-height: 520px;
  box-shadow: 0 20px 40px rgba(0,0,0,0.13);
  border: 2.5px solid #36d1c4;
  overflow: hidden;
}
.ocean-title {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 22px;
  font-weight: bold;
  color: #fff;
  margin-bottom: 18px;
  letter-spacing: 2px;
}
.ocean-title-icon {
  font-size: 26px;
}
.ocean-nowplaying {
  display: flex;
  align-items: center;
  gap: 28px;
  margin-bottom: 28px;
  padding: 18px 22px;
  background: rgba(54,209,196,0.10);
  border-radius: 16px;
  border: 1.5px solid rgba(54,209,196,0.22);
  box-shadow: 0 2px 8px rgba(54,209,196,0.08);
}
.ocean-disc-wrap {
  display: flex;
  align-items: center;
  justify-content: center;
}
.ocean-disc {
  width: 64px;
  height: 64px;
  border-radius: 50%;
  background: linear-gradient(45deg, #2b5876, #4e4376);
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4px 12px rgba(0,0,0,0.23);
  position: relative;
  transition: all 0.3s ease;
}
.ocean-disc.spinning {
  animation: spin 2.2s linear infinite;
}
@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}
.ocean-disc-center {
  width: 20px;
  height: 20px;
  border-radius: 50%;
  background: #fff;
  box-shadow: 0 0 8px #36d1c4, inset 0 2px 4px rgba(0,0,0,0.23);
}
.ocean-disc-wave {
  position: absolute;
  left: 50%; top: 50%;
  width: 80px; height: 80px;
  transform: translate(-50%, -50%);
  border-radius: 50%;
  background: radial-gradient(circle, rgba(54,209,196,0.18) 60%, transparent 100%);
  animation: discWave 1.8s infinite linear;
  z-index: 0;
}
@keyframes discWave {
  0% { opacity: 0.7; transform: translate(-50%, -50%) scale(1); }
  70% { opacity: 0.2; transform: translate(-50%, -50%) scale(1.2); }
  100% { opacity: 0; transform: translate(-50%, -50%) scale(1.4); }
}
.ocean-track-info {
  flex: 1;
}
.ocean-track-name {
  font-size: 18px;
  font-weight: bold;
  margin-bottom: 4px;
  color: #fff;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 200px;
}
.ocean-track-status {
  font-size: 14px;
  opacity: 0.85;
  color: #b2f0f7;
}
.ocean-controls {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 18px;
  margin-bottom: 28px;
}
.ocean-btn {
  width: 48px;
  height: 48px;
  border: none;
  border-radius: 50%;
  background: rgba(255,255,255,0.10);
  color: #fff;
  font-size: 20px;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 2px 8px rgba(54,209,196,0.08);
}
.ocean-btn:hover {
  background: rgba(54,209,196,0.18);
  color: #36d1c4;
  transform: scale(1.08);
}
.ocean-btn.main {
  width: 68px;
  height: 68px;
  background: linear-gradient(135deg, #36d1c4 0%, #3a8dde 100%);
  font-size: 28px;
  box-shadow: 0 8px 20px rgba(54,209,196,0.18);
}
.ocean-btn.main:hover {
  background: linear-gradient(135deg, #3a8dde 0%, #36d1c4 100%);
  color: #fff;
  transform: scale(1.13);
}
.ocean-btn.shuffle.active {
  background: linear-gradient(135deg, #36d1c4 0%, #3a8dde 100%);
  color: #fff;
  box-shadow: 0 0 8px #36d1c4;
  border: 2px solid #fff;
}
.ocean-add {
  display: flex;
  gap: 16px;
  align-items: center;
  margin-bottom: 22px;
}
.ocean-add-btn {
  padding: 10px 18px;
  border: none;
  border-radius: 22px;
  background: rgba(255,255,255,0.10);
  color: #fff;
  font-size: 15px;
  cursor: pointer;
  transition: all 0.3s;
  box-shadow: 0 2px 8px rgba(54,209,196,0.08);
}
.ocean-add-btn:hover {
  background: rgba(54,209,196,0.18);
  color: #36d1c4;
}
.ocean-url-group {
  display: flex;
  gap: 8px;
  flex: 1;
}
.ocean-url-input {
  flex: 1;
  padding: 10px 18px;
  border: none;
  border-radius: 22px;
  background: rgba(255,255,255,0.10);
  color: #fff;
  font-size: 15px;
  outline: none;
  transition: background 0.2s;
}
.ocean-url-input::placeholder {
  color: #b2f0f7;
}
.ocean-url-input:focus {
  background: rgba(255,255,255,0.18);
}
.ocean-playlist {
  margin-bottom: 10px;
}
.ocean-playlist-title {
  font-size: 16px;
  font-weight: bold;
  color: #b2f0f7;
  margin-bottom: 8px;
  display: flex;
  align-items: center;
  gap: 8px;
}
.ocean-count {
  font-size: 13px;
  opacity: 0.7;
}
.ocean-list {
  max-height: 180px;
  overflow-y: auto;
  border-radius: 12px;
  background: rgba(54,209,196,0.07);
  border: 1.5px solid rgba(54,209,196,0.13);
  box-shadow: 0 2px 8px rgba(54,209,196,0.08);
}
.ocean-empty {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 38px 0;
  color: #b2f0f7;
  font-size: 15px;
}
.ocean-track-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 16px;
  border-radius: 8px;
  margin-bottom: 6px;
  transition: all 0.2s;
  background: transparent;
  cursor: pointer;
}
.ocean-track-item.active {
  background: rgba(54,209,196,0.13);
  border-left: 3px solid #36d1c4;
}
.ocean-track-item:hover {
  background: rgba(54,209,196,0.10);
}
.ocean-track-main {
  display: flex;
  align-items: center;
  gap: 10px;
}
.ocean-track-num {
  width: 26px;
  height: 26px;
  border-radius: 50%;
  background: rgba(255,255,255,0.10);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: bold;
  color: #36d1c4;
}
.ocean-track-title {
  font-size: 15px;
  color: #fff;
  font-weight: 500;
  white-space: normal;
  word-break: break-all;
  max-width: 180px;
  line-height: 1.4;
  display: block;
}
.ocean-track-actions {
  display: flex;
  gap: 6px;
}
.ocean-track-btn {
  width: 28px;
  height: 28px;
  border: none;
  border-radius: 50%;
  background: rgba(255,255,255,0.10);
  color: #fff;
  cursor: pointer;
  font-size: 13px;
  transition: all 0.2s;
}
.ocean-track-btn:hover {
  background: rgba(54,209,196,0.18);
  color: #36d1c4;
}
.ocean-track-btn.del:hover {
  background: rgba(255,107,107,0.23);
  color: #ff6b6b;
}
.ocean-track-btn.edit {
  background: rgba(54,209,196,0.10);
  color: #36d1c4;
  font-size: 14px;
  margin-left: 6px;
}
.ocean-track-btn.edit:hover {
  background: rgba(54,209,196,0.18);
  color: #223a5f;
}
.ocean-audio {
  display: none;
}
.ocean-list::-webkit-scrollbar {
  width: 6px;
}
.ocean-list::-webkit-scrollbar-track {
  background: rgba(255,255,255,0.05);
  border-radius: 3px;
}
.ocean-list::-webkit-scrollbar-thumb {
  background: rgba(54,209,196,0.23);
  border-radius: 3px;
}
.ocean-list::-webkit-scrollbar-thumb:hover {
  background: rgba(54,209,196,0.38);
}
.edit-music-input {
  width: 180px;
  font-size: 15px;
  border: 1.5px solid #36d1c4;
  border-radius: 6px;
  padding: 2px 8px;
  outline: none;
  color: #223a5f;
  background: #e6f7ff;
  margin-right: 4px;
  white-space: normal;
  word-break: break-all;
  line-height: 1.4;
  max-width: 100%;
}
.ocean-progress-bar {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-top: 10px;
  margin-bottom: 2px;
  height: 24px;
}
.ocean-time {
  font-size: 12px;
  color: #b2f0f7;
  min-width: 38px;
  text-align: center;
  font-family: monospace;
}
.progress-range {
  flex: 1;
  height: 4px;
  background: linear-gradient(90deg, #36d1c4 0%, #3a8dde 100%);
  border-radius: 2px;
  outline: none;
  -webkit-appearance: none;
  appearance: none;
  margin: 0 4px;
  cursor: pointer;
  transition: background 0.2s;
  position: relative;
  display: flex;
  align-items: center;
}
.progress-range:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}
.progress-range::-webkit-slider-thumb {
  -webkit-appearance: none;
  appearance: none;
  width: 14px;
  height: 14px;
  border-radius: 50%;
  background: #36d1c4;
  border: 2px solid #fff;
  box-shadow: 0 2px 8px rgba(54,209,196,0.18);
  transition: background 0.2s;
  margin-top: -5px;
}
.progress-range:active::-webkit-slider-thumb {
  background: #3a8dde;
}
.progress-range::-moz-range-thumb {
  width: 14px;
  height: 14px;
  border-radius: 50%;
  background: #36d1c4;
  border: 2px solid #fff;
  box-shadow: 0 2px 8px rgba(54,209,196,0.18);
  transition: background 0.2s;
}
.progress-range:active::-moz-range-thumb {
  background: #3a8dde;
}
.progress-range::-ms-thumb {
  width: 14px;
  height: 14px;
  border-radius: 50%;
  background: #36d1c4;
  border: 2px solid #fff;
  box-shadow: 0 2px 8px rgba(54,209,196,0.18);
  transition: background 0.2s;
}
.progress-range:active::-ms-thumb {
  background: #3a8dde;
}
.progress-range::-webkit-slider-runnable-track {
  height: 4px;
  border-radius: 2px;
  background: linear-gradient(90deg, #36d1c4 0%, #3a8dde 100%);
}
.progress-range::-moz-range-track {
  height: 4px;
  border-radius: 2px;
  background: linear-gradient(90deg, #36d1c4 0%, #3a8dde 100%);
}
.progress-range::-ms-fill-lower {
  background: #36d1c4;
}
.progress-range::-ms-fill-upper {
  background: #3a8dde;
}
</style> 