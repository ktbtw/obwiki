<template>
  <div style="display: flex; flex-direction: column; height: 100%;">
    <!-- 会话列表 -->
    <div v-if="showSessionList" style="margin-bottom: 12px;">
      <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 8px;">
        <span style="font-weight: bold;">会话列表</span>
        <a-button size="small" @click="createNewSession">新建会话</a-button>
      </div>
      <div style="max-height: 200px; overflow-y: auto;">
        <div 
          v-for="session in sessions" 
          :key="session.id" 
          :style="{
            padding: '8px 12px',
            margin: '4px 0',
            borderRadius: '6px',
            cursor: 'pointer',
            background: currentSessionId === session.id ? '#e6f7ff' : '#f5f5f5',
            border: currentSessionId === session.id ? '1px solid #1890ff' : '1px solid #d9d9d9'
          }"
          @click="selectSession(session)"
        >
          <div style="font-weight: 500; margin-bottom: 4px;">{{ session.title || '新对话' }}</div>
          <div style="font-size: 12px; color: #666;">
            {{ formatTime(session.updateTime) }} · {{ session.messageCount }}条消息
          </div>
        </div>
      </div>
    </div>

    <!-- 消息列表 -->
    <div style="flex: 1; overflow-y: auto; margin-bottom: 12px;">
      <div v-if="messages.length === 0" style="text-align: center; color: #999; padding: 20px;">
        开始和AI对话吧！
      </div>
      <div v-for="(msg, idx) in messages" :key="idx" :style="{textAlign: msg.role === 'assistant' ? 'left' : 'right', margin: '8px 0'}">
        <div :style="{
          background: msg.role === 'assistant' ? '#f0f0f0' : '#d6eaff', 
          padding: '8px 12px', 
          borderRadius: '8px', 
          display: 'inline-block',
          maxWidth: '80%',
          wordBreak: 'break-word'
        }">
          <div v-if="msg.role === 'assistant'" style="font-size: 12px; color: #666; margin-bottom: 4px;">AI助手</div>
          <div v-else style="font-size: 12px; color: #666; margin-bottom: 4px;">我</div>
          <div>{{ msg.content }}</div>
          <div style="font-size: 11px; color: #999; margin-top: 4px;">{{ formatTime(msg.createTime) }}</div>
        </div>
      </div>
      <div v-if="loading" style="text-align: center; padding: 10px;">
        <a-spin size="small" />
        <span style="margin-left: 8px; color: #666;">AI正在思考...</span>
      </div>
    </div>

    <!-- 输入框 -->
    <div style="display: flex; gap: 8px;">
      <a-input-search
        v-model:value="input"
        enter-button="发送"
        @search="send"
        placeholder="和AI聊点什么吧…"
        :disabled="loading"
        style="flex: 1;"
        @keyup.enter="send"
      />
      <a-button 
        size="small" 
        @click="toggleSessionList"
        :style="{ minWidth: '60px' }"
      >
        {{ showSessionList ? '隐藏' : '会话' }}
      </a-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue';
import { message } from 'ant-design-vue';
import { aiChatApi, type AiChatSession, type AiChatMessage } from '../api/ai-chat';
import store from '../store';

const input = ref('');
const messages = ref<AiChatMessage[]>([]);
const sessions = ref<AiChatSession[]>([]);
const currentSessionId = ref<number | null>(null);
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
  currentSessionId.value = session.id;
  await loadMessageHistory(session.id);
  showSessionList.value = false;
}

// 加载消息历史
async function loadMessageHistory(sessionId: number) {
  try {
    const response = await aiChatApi.getMessageHistory(sessionId, userId.value);
    if (response.data.success) {
      messages.value = response.data.content || [];
    }
  } catch (error) {
    console.error('加载消息历史失败:', error);
    message.error('加载消息历史失败');
  }
}

// 创建新会话
function createNewSession() {
  currentSessionId.value = null;
  messages.value = [];
  showSessionList.value = false;
}

// 发送消息
async function send() {
  console.log('send函数被调用，input值:', input.value);
  if (!input.value.trim() || loading.value) {
    console.log('输入为空或正在加载中，返回');
    return;
  }
  
  const userMessage = input.value;
  input.value = '';
  
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
    console.log('准备调用AI API，参数:', {
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
    
    if (response.data.success) {
      const aiResp = response.data.content;
      
      // 更新会话ID
      if (!currentSessionId.value) {
        currentSessionId.value = aiResp.sessionId;
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
      
      message.success('发送成功');
    } else {
      message.error(response.data.message || '发送失败');
    }
  } catch (error) {
    console.error('发送消息失败:', error);
    message.error('发送失败，请重试');
  } finally {
    loading.value = false;
  }
}

// 组件挂载时加载会话列表
onMounted(() => {
  loadSessionList();
});
</script> 