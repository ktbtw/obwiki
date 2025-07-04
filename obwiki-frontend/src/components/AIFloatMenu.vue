<template>
  <div style="display: flex; flex-direction: column; height: 100%;">
    <div style="flex: 1; overflow-y: auto; margin-bottom: 12px;">
      <div v-for="(msg, idx) in messages" :key="idx" :style="{textAlign: msg.fromAI ? 'left' : 'right', margin: '8px 0'}">
        <span :style="{background: msg.fromAI ? '#f0f0f0' : '#d6eaff', padding: '6px 12px', borderRadius: '8px', display: 'inline-block'}">
          {{ msg.text }}
        </span>
      </div>
    </div>
    <a-input-search
      v-model="input"
      enter-button="发送"
      @search="send"
      placeholder="和AI聊点什么吧…"
    />
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue';
const input = ref('');
const messages = ref<{text: string, fromAI: boolean}[]>([
  { text: '你好，我是AI助手，有什么可以帮你？', fromAI: true }
]);

function send() {
  if (!input.value.trim()) return;
  messages.value.push({ text: input.value, fromAI: false });
  // 这里可以接入后端AI接口，暂时用mock
  setTimeout(() => {
    messages.value.push({ text: 'AI回复：' + input.value, fromAI: true });
  }, 500);
  input.value = '';
}
</script> 