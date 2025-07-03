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
            {{ item.name }}
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
        <div v-for="msg in messages" :key="msg.id" :class="msg.fromMe ? 'me' : 'other'">
          <span>{{ msg.content }}</span>
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
import { message, Modal } from 'ant-design-vue';
import { onMounted, onUnmounted, ref } from 'vue';

const friends = ref([
  // 初始可为空，实际应从后端拉取
]);
const currentFriend = ref(null);
const messages = ref<any[]>([]);
const input = ref('');

const showAddFriendModal = ref(false);
const newFriendName = ref('');

const user = store.state.user;
let websocket: any = null;

const initWebSocket = () => {
  if ('WebSocket' in window) {
    websocket = new WebSocket(process.env.VUE_APP_WS_SERVER + '/ws/' + user.id);
    websocket.onopen = () => {
      console.log('WebSocket连接成功');
    };
    websocket.onmessage = (event: any) => {
      const data = JSON.parse(event.data);
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
  initWebSocket();
});
onUnmounted(() => {
  if (websocket) websocket.close();
});

function selectFriend(friend: any) {
  currentFriend.value = friend;
  // TODO: 拉取与该好友的历史消息
  messages.value = [];
}

function sendMessage() {
  if (!input.value) return;
  messages.value.push({ id: Date.now(), content: input.value, fromMe: true });
  // TODO: 通过WebSocket发送消息到后端
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
</script>

<style scoped>
.chat-container { display: flex; height: 80vh; border: 1px solid #eee; }
.friend-list { width: 200px; border-right: 1px solid #eee; overflow-y: auto; position: relative; }
.add-friend-btn { padding: 12px; }
.chat-box { flex: 1; display: flex; flex-direction: column; }
.chat-history { flex: 1; padding: 16px; overflow-y: auto; }
.chat-input { padding: 12px; border-top: 1px solid #eee; }
.me { text-align: right; color: #1890ff; margin: 8px 0; }
.other { text-align: left; color: #333; margin: 8px 0; }
.active { background: #e6f7ff; }
</style> 