<template>
  <div class="chat-container">
    <div class="friend-list">
      <a-list
        :data-source="friends"
        bordered
        :rowKey="item => item.id"
      >
        <template #renderItem="{ item }">
          <a-list-item :class="{ active: item.id === currentFriend?.id }" @click="selectFriend(item)">
            <span>{{ item.name }}</span>
            <a-button type="text" danger size="small" style="float:right;" @click.stop="handleDeleteFriend(item)">
              <DeleteOutlined />
            </a-button>
          </a-list-item>
        </template>
      </a-list>
      <div class="add-friend-btn">
        <a-button type="primary" block @click="showAddFriendModal = true">添加好友</a-button>
      </div>
      <a-modal v-model:open="showAddFriendModal" title="添加好友" @ok="handleAddFriend" @cancel="showAddFriendModal = false">
        <a-input v-model:value="newFriendName" placeholder="请输入好友昵称" />
      </a-modal>
    </div>
    <div class="chat-box">
      <div class="chat-history">
        <div v-for="msg in messages" :key="msg.id" :class="msg.fromMe ? 'me' : 'other'" class="chat-msg-row">
          <template v-if="msg.fromMe">
            <div class="chat-msg-right">
              <span class="chat-msg-content">{{ msg.content }}</span>
              <a-avatar :src="getAvatar(msg.fromUserId)" :alt="msg.fromUserId" />
            </div>
          </template>
          <template v-else>
            <div class="chat-msg-left">
              <a-avatar :src="getAvatar(msg.fromUserId)" :alt="msg.fromUserId" />
              <span class="chat-msg-content">{{ msg.content }}</span>
            </div>
          </template>
        </div>
      </div>
      <div class="chat-input">
        <a-input-search
          v-model:value="input"
          enter-button="发送"
          @search="sendMessage"
        />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import api from '@/api/index';
import store from '@/store';
import { DeleteOutlined } from '@ant-design/icons-vue';
import { message, Modal } from 'ant-design-vue';
import { onMounted, onUnmounted, reactive, ref } from 'vue';

const friends = ref([
  // 初始可为空，实际应从后端拉取
]);
const currentFriend = ref(null);
const messages = ref<any[]>([]);
const messageMap = ref<Record<string, any[]>>({}); // 好友id->消息数组
const input = ref('');

const showAddFriendModal = ref(false);
const newFriendName = ref('');

const user = store.state.user;
let websocket: any = null;

const avatarMap = reactive<Record<string, string>>({}); // userId->avatar

const loadHistory = async (friendId: number) => {
  const resp = await api.get('/chat/history', { params: { userId: user.id, friendId } });
  let list = (resp.data && resp.data.content) || [];
  // 补充fromMe字段，类型统一
  list = list.map(msg => ({
    ...msg,
    fromMe: String(msg.fromUserId) === String(user.id)
  }));
  messageMap.value[friendId] = list;
  messages.value = messageMap.value[friendId];
};

const initWebSocket = () => {
  if ('WebSocket' in window) {
    websocket = new WebSocket(process.env.VUE_APP_WS_SERVER + '/ws/' + user.id);
    websocket.onopen = () => {
      console.log('WebSocket连接成功');
    };
    websocket.onmessage = (event: any) => {
      const data = JSON.parse(event.data);
      // 聊天消息
      if (data.type === 'chat') {
        const friendId = data.fromUserId === user.id ? data.toUserId : data.fromUserId;
        if (!messageMap.value[friendId]) messageMap.value[friendId] = [];
        messageMap.value[friendId].push({
          id: Date.now(),
          content: data.content,
          fromMe: data.fromUserId === user.id,
          fromUserId: data.fromUserId,
          toUserId: data.toUserId,
          time: data.time
        });
        if (currentFriend.value && friendId === currentFriend.value.id) {
          messages.value = messageMap.value[friendId];
        }
      }
      // 处理好友请求
      if (data.type === 'friendRequest' && data.toUserId === user.id) {
        Modal.confirm({
          title: '好友请求',
          content: `${data.fromUserName} 请求加你为好友，是否同意？`,
          okText: '同意',
          cancelText: '拒绝',
          onOk: () => {
            websocket.send(JSON.stringify({
              type: 'friendResponse',
              fromUserId: user.id,
              fromUserName: user.name,
              toUserId: data.fromUserId,
              toUserName: data.fromUserName,
              accepted: true
            }));
            // 本地加好友
            friends.value.push({ id: data.fromUserId, name: data.fromUserName });
          }
        });
      }
      // 处理同意结果
      if (data.type === 'friendResponse' && data.accepted) {
        if (!friends.value.find(f => f.id === data.fromUserId)) {
          friends.value.push({ id: data.fromUserId, name: data.fromUserName });
        }
        message.success('你和对方已互为好友！');
      }
    };
    websocket.onerror = () => { console.log('WebSocket连接错误'); };
    websocket.onclose = () => { console.log('WebSocket连接关闭'); };
  }
};

onMounted(() => {
  // 1. 拉取数据库好友列表
  api.get('/friend/list', { params: { userId: user.id } }).then(resp => {
    if (resp.data && resp.data.content) {
      friends.value = resp.data.content;
    }
  });
  // 2. 初始化WebSocket
  initWebSocket();
});
onUnmounted(() => {
  if (websocket) websocket.close();
});

function selectFriend(friend: any) {
  currentFriend.value = friend;
  loadHistory(friend.id);
}

function sendMessage() {
  if (!input.value || !currentFriend.value) return;
  const msg = {
    type: 'chat',
    fromUserId: user.id,
    fromUserName: user.name,
    toUserId: currentFriend.value.id,
    toUserName: currentFriend.value.name,
    content: input.value,
    time: new Date().toISOString().slice(0, 19).replace('T', ' ')
  };
  websocket.send(JSON.stringify(msg));
  if (!messageMap.value[currentFriend.value.id]) messageMap.value[currentFriend.value.id] = [];
  messageMap.value[currentFriend.value.id].push({
    id: Date.now(),
    content: input.value,
    fromMe: true,
    fromUserId: user.id,
    toUserId: currentFriend.value.id,
    time: msg.time
  });
  messages.value = messageMap.value[currentFriend.value.id];
  input.value = '';
}

async function handleAddFriend() {
  if (!newFriendName.value.trim()) return;
  // 通过昵称查找用户id
  try {
    const resp = await api.get('/user/findByName', { params: { name: newFriendName.value.trim() } });
    const toUser = resp.data.content;
    if (!toUser || !toUser.id) {
      message.error('未找到该用户');
      return;
    }
    websocket.send(JSON.stringify({
      type: 'friendRequest',
      fromUserId: user.id,
      fromUserName: user.name,
      toUserId: toUser.id,
      toUserName: toUser.name
    }));
    showAddFriendModal.value = false;
    newFriendName.value = '';
    message.info('好友请求已发送');
  } catch (e) {
    message.error('查找用户失败');
  }
}

function fixAvatarUrl(url) {
  if (!url) return '';
  if (url.startsWith('http')) return url;
  return 'http://localhost:8880/obwiki' + url;
}

function getAvatar(userId) {
  if (userId === user.id) {
    if (!avatarMap[userId]) avatarMap[userId] = user.avatar; // 主动缓存
    return fixAvatarUrl(user.avatar);
  }
  if (avatarMap[userId]) return fixAvatarUrl(avatarMap[userId]);
  api.get('/user/getById', { params: { id: userId } }).then(resp => {
    if (resp.data && resp.data.content) {
      avatarMap[userId] = resp.data.content.avatar;
      messages.value = [...messages.value]; // 强制刷新
    }
  });
  return '';
}

function handleDeleteFriend(friend) {
  Modal.confirm({
    title: '确认删除',
    content: `确定要删除好友「${friend.name}」吗？删除后不可恢复！`,
    okText: '删除',
    okType: 'danger',
    cancelText: '取消',
    onOk: () => {
      api.delete('/friend/delete', { params: { userId: user.id, friendId: friend.id } }).then(resp => {
        if (resp.data && resp.data.success) {
          friends.value = friends.value.filter(f => f.id !== friend.id);
          if (currentFriend.value && currentFriend.value.id === friend.id) {
            currentFriend.value = null;
            messages.value = [];
          }
          message.success('已删除好友');
        } else {
          message.error(resp.data && resp.data.message || '删除失败');
        }
      });
    }
  });
}
</script>

<style scoped>
.chat-container { display: flex; height: 80vh; border: 1px solid #eee; }
.friend-list { width: 200px; border-right: 1px solid #eee; overflow-y: auto; position: relative; }
.add-friend-btn { padding: 12px; }
.chat-box { flex: 1; display: flex; flex-direction: column; }
.chat-history { flex: 1; padding: 16px; overflow-y: auto; }
.chat-input { padding: 12px; border-top: 1px solid #eee; }
.chat-msg-row { display: flex; margin: 8px 0; }
.chat-msg-left { display: flex; align-items: flex-end; gap: 8px; }
.chat-msg-right { display: flex; align-items: flex-end; gap: 8px; margin-left: auto; }
.chat-msg-content {
  max-width: 320px;
  border-radius: 16px;
  padding: 10px 18px;
  word-break: break-all;
  font-size: 15px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.04);
  margin-bottom: 2px;
  min-height: 36px;
  display: flex;
  align-items: center;
}
.me .chat-msg-content, .chat-msg-right .chat-msg-content {
  background: #e6f7ff;
  color: #1677ff;
  border: 1px solid #91d5ff;
}
.other .chat-msg-content, .chat-msg-left .chat-msg-content {
  background: #f5f5f5;
  color: #333;
  border: 1px solid #e4e4e4;
}
.active { background: #e6f7ff; }
</style> 