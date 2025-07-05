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
    <div style="display: flex; gap: 12px; align-items: center;">
      <div style="flex: 1; position: relative;">
        <a-input
          v-model="input"
          @input="handleInput"
          @keydown.enter="send"
          placeholder="和AI聊点什么吧…"
          :disabled="loading"
          :style="{
            borderRadius: '20px',
            border: '2px solid #e8e8e8',
            padding: '8px 16px',
            fontSize: '14px',
            transition: 'all 0.3s ease',
            background: '#fafafa',
            boxShadow: '0 2px 8px rgba(0,0,0,0.05)'
          }"
          @focus="handleInputFocus"
          @blur="handleInputBlur"
        />
        <div 
          v-if="loading" 
          style="
            position: absolute; 
            right: 12px; 
            top: 50%; 
            transform: translateY(-50%);
            color: #1890ff;
          "
        >
          <a-spin size="small" />
        </div>
      </div>
      
      <a-button 
        type="primary"
        @click="send"
        :disabled="loading || !input.trim()"
        :style="{
          borderRadius: '20px',
          height: '40px',
          padding: '0 20px',
          border: 'none',
          fontWeight: '500',
          fontSize: '14px',
          transition: 'all 0.3s ease',
          background: input.trim() && !loading 
            ? 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)' 
            : '#d9d9d9',
          boxShadow: input.trim() && !loading 
            ? '0 4px 15px rgba(102, 126, 234, 0.4), 0 2px 4px rgba(0,0,0,0.1)' 
            : 'none',
          transform: input.trim() && !loading ? 'translateY(0)' : 'translateY(0)'
        }"
        @mouseenter="handleSendButtonHover"
        @mouseleave="handleSendButtonLeave"
      >
        <template #icon>
          <span style="margin-right: 6px;">🚀</span>
        </template>
        发送
      </a-button>
      <a-button 
        size="small" 
        @click="toggleSessionList"
        @mouseenter="handleButtonHover"
        @mouseleave="handleButtonLeave"
        :style="{
          minWidth: '80px',
          height: '32px',
          borderRadius: '16px',
          border: 'none',
          color: 'white',
          fontWeight: '500',
          fontSize: '12px',
          transition: 'all 0.3s ease',
          background: showSessionList 
            ? 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)' 
            : 'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)',
          boxShadow: showSessionList 
            ? '0 4px 15px rgba(102, 126, 234, 0.4), 0 2px 4px rgba(0,0,0,0.1)' 
            : '0 4px 15px rgba(245, 87, 108, 0.4), 0 2px 4px rgba(0,0,0,0.1)'
        }"
      >
        <template #icon>
          <span v-if="showSessionList" style="margin-right: 4px;">📋</span>
          <span v-else style="margin-right: 4px;">📚</span>
        </template>
        {{ showSessionList ? '隐藏' : '历史记录' }}
      </a-button>
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
  currentSessionId.value = session.id.toString();
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

// 按钮悬停效果
function handleButtonHover(e: MouseEvent) {
  const target = e.target as HTMLElement;
  if (showSessionList.value) {
    target.style.background = 'linear-gradient(135deg, #5a6fd8 0%, #6a4190 100%)';
    target.style.transform = 'translateY(-2px)';
    target.style.boxShadow = '0 8px 25px rgba(102, 126, 234, 0.6), 0 6px 12px rgba(0,0,0,0.2)';
  } else {
    target.style.background = 'linear-gradient(135deg, #e085f0 0%, #e54b5f 100%)';
    target.style.transform = 'translateY(-2px)';
    target.style.boxShadow = '0 8px 25px rgba(245, 87, 108, 0.6), 0 6px 12px rgba(0,0,0,0.2)';
  }
}

// 按钮离开效果
function handleButtonLeave(e: MouseEvent) {
  const target = e.target as HTMLElement;
  if (showSessionList.value) {
    target.style.background = 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)';
    target.style.transform = 'translateY(0)';
    target.style.boxShadow = '0 4px 15px rgba(102, 126, 234, 0.4), 0 2px 4px rgba(0,0,0,0.1)';
  } else {
    target.style.background = 'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)';
    target.style.transform = 'translateY(0)';
    target.style.boxShadow = '0 4px 15px rgba(245, 87, 108, 0.4), 0 2px 4px rgba(0,0,0,0.1)';
  }
}

// 处理输入框输入
function handleInput(e: Event) {
  const target = e.target as HTMLInputElement;
  input.value = target.value;
  console.log('📝 输入框值更新:', input.value);
}

// 输入框获得焦点
function handleInputFocus(e: Event) {
  const target = e.target as HTMLInputElement;
  target.style.border = '2px solid #667eea';
  target.style.boxShadow = '0 4px 15px rgba(102, 126, 234, 0.2), 0 2px 8px rgba(0,0,0,0.05)';
  target.style.background = '#ffffff';
}

// 输入框失去焦点
function handleInputBlur(e: Event) {
  const target = e.target as HTMLInputElement;
  target.style.border = '2px solid #e8e8e8';
  target.style.boxShadow = '0 2px 8px rgba(0,0,0,0.05)';
  target.style.background = '#fafafa';
}

// 发送按钮悬停效果
function handleSendButtonHover(e: MouseEvent) {
  const target = e.target as HTMLElement;
  if (input.value.trim() && !loading.value) {
    target.style.background = 'linear-gradient(135deg, #5a6fd8 0%, #6a4190 100%)';
    target.style.transform = 'translateY(-2px)';
    target.style.boxShadow = '0 8px 25px rgba(102, 126, 234, 0.6), 0 6px 12px rgba(0,0,0,0.2)';
  }
}

// 发送按钮离开效果
function handleSendButtonLeave(e: MouseEvent) {
  const target = e.target as HTMLElement;
  if (input.value.trim() && !loading.value) {
    target.style.background = 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)';
    target.style.transform = 'translateY(0)';
    target.style.boxShadow = '0 4px 15px rgba(102, 126, 234, 0.4), 0 2px 4px rgba(0,0,0,0.1)';
  }
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