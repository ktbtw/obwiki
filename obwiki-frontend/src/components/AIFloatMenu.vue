<template>
  <div class="ai-chat-wrapper">
    <!-- 会话列表 -->
    <div v-if="showSessionList" class="session-list-container">
      <div class="session-header">
        <span class="session-title">📚 会话列表</span>
        <button class="new-session-btn" @click="createNewSession">
          <span>✨</span>
          <span>新建</span>
        </button>
      </div>
      <div class="session-list">
        <div 
          v-for="session in sessions" 
          :key="session.id" 
          class="session-item"
          :class="{ active: currentSessionId === session.id.toString() }"
          @click="selectSession(session)"
        >
          <div class="session-info">
            <div class="session-name">{{ session.title || '新对话' }}</div>
            <div class="session-meta">
              <span class="session-time">{{ formatTime(session.updateTime) }}</span>
              <span class="session-count">{{ session.messageCount }}条消息</span>
            </div>
          </div>
          <div class="session-indicator"></div>
        </div>
      </div>
    </div>

    <!-- 消息列表 -->
    <div class="messages-container">
      <div v-if="messages.length === 0" class="empty-state">
        <div class="empty-icon">🤖</div>
        <div class="empty-text">开始和AI对话吧！</div>
        <div class="empty-subtext">我可以帮你解答各种问题</div>
      </div>
      <div v-else class="messages-list">
        <div v-for="(msg, idx) in messages" :key="idx" class="message-item" :class="msg.role">
          <div class="message-content">
            <div class="message-header">
              <span class="message-role">{{ msg.role === 'assistant' ? '🤖 AI助手' : '👤 我' }}</span>
              <span class="message-time">{{ formatTime(msg.createTime) }}</span>
            </div>
            <div class="message-body" v-if="msg.role === 'assistant'" v-html="msg.content"></div>
            <div class="message-body" v-else>{{ msg.content }}</div>
          </div>
        </div>
      </div>
      <div v-if="loading" class="loading-state">
        <div class="loading-dots">
          <span></span>
          <span></span>
          <span></span>
        </div>
        <span class="loading-text">AI正在思考...</span>
      </div>
    </div>

    <!-- 输入框 -->
    <div class="input-container">
      <div class="input-wrapper">
        <input
          v-model="input"
          @input="handleInput"
          @keydown.enter="send"
          placeholder="和AI聊点什么吧…"
          :disabled="loading"
          class="chat-input"
        />
        <div v-if="loading" class="input-loading">
          <div class="loading-spinner"></div>
        </div>
      </div>
      
      <button 
        @click="send"
        :disabled="loading || !input.trim()"
        class="send-btn"
        :class="{ active: input.trim() && !loading }"
      >
        <span class="send-icon">🚀</span>
        <span class="send-text">发送</span>
      </button>
      
      <button 
        @click="toggleSessionList"
        class="history-btn"
        :class="{ active: showSessionList }"
      >
        <span class="history-icon">{{ showSessionList ? '📋' : '📚' }}</span>
        <span class="history-text">{{ showSessionList ? '隐藏' : '历史' }}</span>
      </button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed, watch } from 'vue';
import { message } from 'ant-design-vue';
import { aiChatApi, type AiChatSession, type AiChatMessage } from '../api/ai-chat';
import store from '../store';

const input = ref('');
const messages = ref<AiChatMessage[]>([]);
const sessions = ref<AiChatSession[]>([]);
const currentSessionId = ref<string | null>(null);
const loading = ref(false);
const showSessionList = ref(false);

// 获取当前用户ID
const userId = computed(() => {
  return store.state.user?.id || 1; // 默认用户ID为1，实际应该从登录状态获取
});

// 格式化时间
function formatTime(timeStr: string) {
  const date = new Date(timeStr);
  const now = new Date();
  const diff = now.getTime() - date.getTime();
  
  if (diff < 60000) return '刚刚';
  if (diff < 3600000) return Math.floor(diff / 60000) + '分钟前';
  if (diff < 86400000) return Math.floor(diff / 3600000) + '小时前';
  return date.toLocaleDateString();
}

// 切换会话列表显示
function toggleSessionList() {
  showSessionList.value = !showSessionList.value;
  if (showSessionList.value) {
    loadSessionList();
  }
}

// 加载会话列表
async function loadSessionList() {
  try {
    const response = await aiChatApi.getSessionList(userId.value);
    if (response.data.success) {
      sessions.value = response.data.content || [];
    }
  } catch (error) {
    console.error('加载会话列表失败:', error);
    message.error('加载会话列表失败');
  }
}

// 选择会话
async function selectSession(session: AiChatSession) {
  console.log('🎯 选择会话:', session);
        currentSessionId.value = session.id?.toString() || null;
  await loadMessageHistory(currentSessionId.value);
  showSessionList.value = false;
}

// 加载消息历史
async function loadMessageHistory(sessionId: string) {
  console.log('📚 开始加载消息历史，sessionId:', sessionId, 'userId:', userId.value);
  try {
    const response = await aiChatApi.getMessageHistory(sessionId, userId.value);
    console.log('📡 消息历史API响应:', response);
    
    if (response.data.success) {
      const messageList = response.data.content || [];
      console.log('✅ 获取到消息列表:', messageList);
      console.log('📊 消息数量:', messageList.length);
      
      // 确保消息格式正确
      messages.value = messageList.map((msg: any) => ({
        id: msg.id,
        role: msg.role,
        content: msg.content,
        createTime: msg.createTime
      }));
      
      console.log('🎉 消息已加载到界面，当前messages:', messages.value);
    } else {
      console.error('❌ API返回失败:', response.data.message);
      message.error(response.data.message || '加载消息历史失败');
    }
  } catch (error) {
    console.error('💥 加载消息历史失败:', error);
    message.error('加载消息历史失败');
  }
}

// 创建新会话
function createNewSession() {
  currentSessionId.value = null;
  messages.value = [];
  showSessionList.value = false;
}

// 处理输入框输入
function handleInput(e: Event) {
  const target = e.target as HTMLInputElement;
  input.value = target.value;
  console.log('📝 输入框值更新:', input.value);
}

// 发送消息
async function send() {
  console.log('🚀 send函数被调用，input值:', input.value);
  console.log('📊 当前状态 - loading:', loading.value, 'input长度:', input.value.length);
  
  if (!input.value.trim()) {
    console.log('❌ 输入为空，返回');
    message.warning('请输入消息内容');
    return;
  }
  
  if (loading.value) {
    console.log('⏳ 正在加载中，忽略重复请求');
    return;
  }
  
  const userMessage = input.value.trim();
  input.value = '';
  
  console.log('✅ 开始处理消息:', userMessage);
  
  // 添加用户消息到界面
  const userMsg: AiChatMessage = {
    id: Date.now(),
    role: 'user',
    content: userMessage,
    createTime: new Date().toISOString()
  };
  messages.value.push(userMsg);
  
  loading.value = true;
  
  try {
    console.log('🌐 准备调用AI API，参数:', {
      sessionId: currentSessionId.value || undefined,
      message: userMessage,
      userId: userId.value
    });
    
    const response = await aiChatApi.chat({
      sessionId: currentSessionId.value || undefined,
      message: userMessage,
      model: 'doubao-seed-1-6-250615',
      temperature: 0.7,
      maxTokens: 2048,
      isStream: false
    }, userId.value);
    
    console.log('📡 AI API响应:', response);
    
    if (response.data.success) {
      const aiResp = response.data.content;
      
      // 更新会话ID
      if (!currentSessionId.value && aiResp.sessionId) {
        currentSessionId.value = aiResp.sessionId.toString();
      }
      
      // 添加AI回复
      const aiMsg: AiChatMessage = {
        id: Date.now() + 1,
        role: 'assistant',
        content: aiResp.content,
        createTime: aiResp.createTime
      };
      messages.value.push(aiMsg);
      
      // 刷新会话列表
      await loadSessionList();
      
      console.log('✅ 消息发送成功');
      message.success('发送成功');
    } else {
      console.error('❌ AI API返回失败:', response.data.message);
      message.error(response.data.message || '发送失败');
    }
  } catch (error) {
    console.error('💥 发送消息失败:', error);
    message.error('发送失败，请重试');
  } finally {
    loading.value = false;
    console.log('🏁 发送流程结束');
  }
}

// 监听输入框值变化
watch(input, (newValue) => {
  console.log('👀 输入框值变化:', newValue);
});

// 监听消息数组变化
watch(messages, (newMessages) => {
  console.log('💬 消息数组变化:', newMessages);
  console.log('📊 消息数量:', newMessages.length);
}, { deep: true });

// 组件挂载时加载会话列表
onMounted(() => {
  loadSessionList();
  console.log('🎯 组件挂载完成，初始input值:', input.value);
});
</script>

<style scoped>
.ai-chat-wrapper {
  display: flex;
  flex-direction: column;
  height: 100%;
  color: #fff;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
}

/* 会话列表样式 */
.session-list-container {
  margin-bottom: 16px;
  background: rgba(255,255,255,0.08);
  border-radius: 16px;
  border: 1px solid rgba(91,134,229,0.2);
  overflow: hidden;
}

.session-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  background: linear-gradient(135deg, rgba(91,134,229,0.15) 0%, rgba(54,209,196,0.15) 100%);
  border-bottom: 1px solid rgba(91,134,229,0.2);
}

.session-title {
  font-weight: 600;
  font-size: 16px;
  color: #fff;
}

.new-session-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  background: linear-gradient(135deg, #36d1c4 0%, #5b86e5 100%);
  border: none;
  border-radius: 20px;
  color: #fff;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 4px 12px rgba(54,209,196,0.3);
}

.new-session-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(54,209,196,0.4);
}

.session-list {
  max-height: 200px;
  overflow-y: auto;
  padding: 8px;
}

.session-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  margin: 4px 0;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s ease;
  background: rgba(255,255,255,0.05);
  border: 1px solid transparent;
}

.session-item:hover {
  background: rgba(91,134,229,0.15);
  border-color: rgba(91,134,229,0.3);
  transform: translateX(4px);
}

.session-item.active {
  background: linear-gradient(135deg, rgba(91,134,229,0.2) 0%, rgba(54,209,196,0.2) 100%);
  border-color: rgba(91,134,229,0.5);
  box-shadow: 0 4px 12px rgba(91,134,229,0.2);
}

.session-info {
  flex: 1;
}

.session-name {
  font-weight: 500;
  font-size: 15px;
  color: #fff;
  margin-bottom: 4px;
}

.session-meta {
  display: flex;
  gap: 12px;
  font-size: 12px;
  color: rgba(255,255,255,0.7);
}

.session-indicator {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #36d1c4;
  opacity: 0;
  transition: opacity 0.3s ease;
}

.session-item.active .session-indicator {
  opacity: 1;
}

/* 消息列表样式 */
.messages-container {
  flex: 1;
  overflow-y: auto;
  margin-bottom: 16px;
  padding: 8px;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px 20px;
  text-align: center;
}

.empty-icon {
  font-size: 48px;
  margin-bottom: 16px;
  opacity: 0.8;
}

.empty-text {
  font-size: 18px;
  font-weight: 600;
  color: #fff;
  margin-bottom: 8px;
}

.empty-subtext {
  font-size: 14px;
  color: rgba(255,255,255,0.7);
}

.messages-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.message-item {
  display: flex;
  margin-bottom: 16px;
}

.message-item.user {
  justify-content: flex-end;
}

.message-item.assistant {
  justify-content: flex-start;
}

.message-content {
  max-width: 80%;
  padding: 12px 16px;
  border-radius: 18px;
  position: relative;
  box-shadow: 0 4px 12px rgba(0,0,0,0.1);
}

.message-item.user .message-content {
  background: linear-gradient(135deg, #5b86e5 0%, #36d1c4 100%);
  border-bottom-right-radius: 6px;
}

.message-item.assistant .message-content {
  background: rgba(255,255,255,0.1);
  border: 1px solid rgba(255,255,255,0.2);
  border-bottom-left-radius: 6px;
}

.message-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
  font-size: 12px;
}

.message-role {
  font-weight: 600;
  color: rgba(255,255,255,0.9);
}

.message-time {
  color: rgba(255,255,255,0.6);
}

.message-body {
  font-size: 14px;
  line-height: 1.5;
  color: #fff;
  word-break: break-word;
}

.message-body :deep(p) {
  margin: 0 0 8px 0;
}

.message-body :deep(p:last-child) {
  margin-bottom: 0;
}

.message-body :deep(code) {
  background: rgba(0,0,0,0.2);
  padding: 2px 6px;
  border-radius: 4px;
  font-family: 'Monaco', 'Menlo', monospace;
  font-size: 13px;
}

.message-body :deep(pre) {
  background: rgba(0,0,0,0.3);
  padding: 12px;
  border-radius: 8px;
  overflow-x: auto;
  margin: 8px 0;
}

.message-body :deep(pre code) {
  background: none;
  padding: 0;
}

/* 加载状态样式 */
.loading-state {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  padding: 20px;
}

.loading-dots {
  display: flex;
  gap: 4px;
}

.loading-dots span {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #36d1c4;
  animation: loadingDot 1.4s infinite ease-in-out;
}

.loading-dots span:nth-child(1) { animation-delay: -0.32s; }
.loading-dots span:nth-child(2) { animation-delay: -0.16s; }

@keyframes loadingDot {
  0%, 80%, 100% { transform: scale(0); opacity: 0.5; }
  40% { transform: scale(1); opacity: 1; }
}

.loading-text {
  font-size: 14px;
  color: rgba(255,255,255,0.8);
}

/* 输入区域样式 */
.input-container {
  display: flex;
  gap: 12px;
  align-items: center;
  padding: 8px;
}

.input-wrapper {
  flex: 1;
  position: relative;
}

.chat-input {
  width: 100%;
  padding: 12px 16px;
  border: 2px solid rgba(255,255,255,0.2);
  border-radius: 24px;
  background: rgba(255,255,255,0.1);
  color: #fff;
  font-size: 14px;
  outline: none;
  transition: all 0.3s ease;
  backdrop-filter: blur(10px);
}

.chat-input::placeholder {
  color: rgba(255,255,255,0.6);
}

.chat-input:focus {
  border-color: rgba(91,134,229,0.6);
  background: rgba(255,255,255,0.15);
  box-shadow: 0 4px 16px rgba(91,134,229,0.2);
}

.chat-input:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.input-loading {
  position: absolute;
  right: 16px;
  top: 50%;
  transform: translateY(-50%);
}

.loading-spinner {
  width: 16px;
  height: 16px;
  border: 2px solid rgba(91,134,229,0.3);
  border-top: 2px solid #36d1c4;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  0% { transform: translateY(-50%) rotate(0deg); }
  100% { transform: translateY(-50%) rotate(360deg); }
}

/* 按钮样式 */
.send-btn, .history-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 12px 16px;
  border: none;
  border-radius: 24px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
  color: #fff;
  background: rgba(255,255,255,0.1);
  border: 1px solid rgba(255,255,255,0.2);
}

.send-btn {
  background: linear-gradient(135deg, rgba(91,134,229,0.3) 0%, rgba(54,209,196,0.3) 100%);
  border-color: rgba(91,134,229,0.4);
}

.send-btn.active {
  background: linear-gradient(135deg, #5b86e5 0%, #36d1c4 100%);
  border-color: rgba(91,134,229,0.6);
  box-shadow: 0 4px 16px rgba(91,134,229,0.3);
  transform: translateY(-2px);
}

.send-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(91,134,229,0.4);
}

.send-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
  transform: none;
}

.history-btn {
  background: linear-gradient(135deg, rgba(245,87,108,0.3) 0%, rgba(240,147,251,0.3) 100%);
  border-color: rgba(245,87,108,0.4);
}

.history-btn.active {
  background: linear-gradient(135deg, #f5576c 0%, #f093fb 100%);
  border-color: rgba(245,87,108,0.6);
  box-shadow: 0 4px 16px rgba(245,87,108,0.3);
  transform: translateY(-2px);
}

.history-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(245,87,108,0.4);
}

/* 滚动条样式 */
.session-list::-webkit-scrollbar,
.messages-container::-webkit-scrollbar {
  width: 6px;
}

.session-list::-webkit-scrollbar-track,
.messages-container::-webkit-scrollbar-track {
  background: rgba(255,255,255,0.05);
  border-radius: 3px;
}

.session-list::-webkit-scrollbar-thumb,
.messages-container::-webkit-scrollbar-thumb {
  background: rgba(91,134,229,0.3);
  border-radius: 3px;
}

.session-list::-webkit-scrollbar-thumb:hover,
.messages-container::-webkit-scrollbar-thumb:hover {
  background: rgba(91,134,229,0.5);
}
</style> 