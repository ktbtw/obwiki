<template>
  <a-comment>
    <template #actions>
      <a-button type="link" size="small" @click="showReplyInput = !showReplyInput">
        <message-outlined /> 回复
      </a-button>
      <a-button type="link" size="small" @click="handleLike">
        <like-outlined /> {{ comment.voteCount || 0 }} 点赞
      </a-button>
    </template>
    <template #author><a>{{ comment.userId }}</a></template>
    <template #avatar>
      <a-avatar :src="`https://i.pravatar.cc/150?u=${comment.userId}`" :alt="comment.userId"/>
    </template>
    <template #content>
      <p>{{ comment.content }}</p>
    </template>

    <!-- 回复输入框 -->
    <div v-if="showReplyInput" class="reply-input-wrapper">
      <a-textarea v-model:value="replyContent" :rows="2" placeholder="输入你的回复..."/>
      <a-button type="primary" size="small" @click="handleReply" :loading="replying">
        提交回复
      </a-button>
    </div>

    <!-- 递归渲染子评论 -->
    <comment-item
        v-for="child in comment.children"
        :key="child.id"
        :comment="child"
        @submit-reply="$emit('submit-reply', $event)"
    />
  </a-comment>
</template>

<script lang="ts">
import { LikeOutlined, MessageOutlined } from '@ant-design/icons-vue';
import { defineComponent, ref } from 'vue';

export default defineComponent({
  name: 'CommentItem',
  components: {
    LikeOutlined,
    MessageOutlined,
  },
  props: {
    comment: {
      type: Object,
      required: true,
    },
  },
  emits: ['submit-reply', 'like-comment'],
  setup(props, { emit }) {
    const showReplyInput = ref(false);
    const replyContent = ref('');
    const replying = ref(false);

    const handleReply = () => {
      if (!replyContent.value.trim()) {
        return;
      }
      replying.value = true;
      const replyData = {
        parentId: props.comment.id,
        content: replyContent.value,
      }
      emit('submit-reply', replyData);
      showReplyInput.value = false;
      replyContent.value = '';
      replying.value = false;
    };

    const handleLike = () => {
      emit('like-comment', props.comment.id);
    };

    return {
      showReplyInput,
      replyContent,
      replying,
      handleReply,
      handleLike,
    };
  },
});
</script>

<style scoped>
.reply-input-wrapper {
  display: flex;
  align-items: center;
  margin-top: 8px;
  gap: 8px;
}
</style> 