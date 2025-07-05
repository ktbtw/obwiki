import api from './index';

// AI对话相关接口
export interface AiChatReq {
  sessionId?: number;
  message: string;
  model?: string;
  temperature?: number;
  maxTokens?: number;
  isStream?: boolean;
}

export interface AiChatResp {
  sessionId: number;
  sessionTitle?: string;
  content: string;
  model: string;
  responseTime: number;
  createTime: string;
  messageHistory?: Array<{
    role: string;
    content: string;
    createTime: string;
  }>;
}

export interface AiChatSession {
  id: number;
  title: string;
  createTime: string;
  updateTime: string;
  messageCount: number;
}

export interface AiChatMessage {
  id: number;
  role: string;
  content: string;
  createTime: string;
}

// AI对话接口
export const aiChatApi = {
  // 发送消息给AI
  chat: (data: AiChatReq, userId: number) => {
    // 保证data.sessionId为字符串或undefined
    const sendData = { ...data, sessionId: data.sessionId ? data.sessionId.toString() : undefined };
    return api.post('/ai-chat/chat', sendData, {
      params: { userId }
    });
  },

  // 流式发送消息给AI
  chatStream: (data: AiChatReq, userId: number) => {
    return api.post('/ai-chat/chat/stream', data, {
      params: { userId },
      responseType: 'text'
    });
  },

  // 获取会话列表
  getSessionList: (userId: number) => {
    return api.get('/ai-chat/sessions', {
      params: { userId }
    });
  },

  // 获取会话消息历史
  getMessageHistory: (sessionId: string, userId: number) => {
    return api.get(`/ai-chat/sessions/${sessionId}/messages`, {
      params: { userId }
    });
  },

  // 删除会话
  deleteSession: (sessionId: number, userId: number) => {
    return api.delete(`/ai-chat/sessions/${sessionId}`, {
      params: { userId }
    });
  },

  // 清空会话消息
  clearSession: (sessionId: number, userId: number) => {
    return api.delete(`/ai-chat/sessions/${sessionId}/messages`, {
      params: { userId }
    });
  }
}; 