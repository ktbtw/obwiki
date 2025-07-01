<template>
  <a-comment>
    <template #actions>
      <a-button type="link" size="small" @click="toggleReplyFor(comment.id)">
        <message-outlined /> 回复
      </a-button>
      <a-button type="link" size="small" @click="handleLike(comment)">
        <like-outlined :style="{ color: comment.isVoted ? 'blue' : '#aaa' }" />
        {{ comment.voteCount || 0 }} {{ comment.isVoted ? '已点赞' : '点赞' }}
      </a-button>
    </template>
    <template #author><a>{{ comment.username || comment.userId }}</a></template>
    <template #avatar>
      <a-avatar :src="`https://i.pravatar.cc/150?u=${comment.userId}`" :alt="comment.username"/>
    </template>
    <template #content>
      <p>{{ comment.content }}</p>
    </template>

    <!-- 主评论的回复输入框 -->
    <div v-if="activeReplyId === comment.id" class="reply-input-wrapper">
      <a-textarea v-model:value="replyContent" :rows="2" placeholder="输入你的回复..."/>
      <a-button type="primary" size="small" @click="handleReply(comment.id)" :loading="replying">
        提交回复
      </a-button>
    </div>

    <!-- 子评论平铺展示 -->
    <div v-if="flattenedReplies.length > 0" class="replies-container">
      <div v-for="reply in flattenedReplies" :key="reply.id" class="reply-item">
         <a-comment>
           <template #actions>
             <a-button type="link" size="small" @click="toggleReplyFor(reply.id)">
               <message-outlined /> 回复
             </a-button>
             <a-button type="link" size="small" @click="handleLike(reply)">
               <like-outlined :style="{ color: reply.isVoted ? 'blue' : '#aaa' }" />
               {{ reply.voteCount || 0 }} {{ reply.isVoted ? '已点赞' : '点赞' }}
             </a-button>
           </template>
           <template #author>
             <span>
               <b>{{ reply.username }}</b>
               <span v-if="reply.parentId && reply.parentId !== comment.id && reply.replyToUsername"> -> <b>{{ reply.replyToUsername }}</b></span>
             </span>
           </template>
           <template #avatar>
             <a-avatar :src="`https://i.pravatar.cc/150?u=${reply.userId}`" :alt="reply.username"/>
           </template>
           <template #content>
              <p>{{ reply.content }}</p>
           </template>
           <!-- 子评论的回复输入框 -->
           <div v-if="activeReplyId === reply.id" class="reply-input-wrapper">
             <a-textarea v-model:value="replyContent" :rows="2" placeholder="输入你的回复..."/>
             <a-button type="primary" size="small" @click="handleReply(reply.id)" :loading="replying">
               提交回复
             </a-button>
           </div>
         </a-comment>
      </div>
    </div>
  </a-comment>
</template>

<script lang="ts">
import { LikeOutlined, MessageOutlined } from '@ant-design/icons-vue';
import { computed, defineComponent, ref } from 'vue';

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
    const activeReplyId = ref<number | null>(null);
    const replyContent = ref('');
    const replying = ref(false);

    const toggleReplyFor = (id: number) => {
      if (activeReplyId.value === id) {
        activeReplyId.value = null;
      } else {
        activeReplyId.value = id;
        replyContent.value = '';
      }
    };

    const handleReply = (parentId: number) => {
      if (!replyContent.value.trim()) {
        return;
      }
      replying.value = true;
      const replyData = {
        parentId: parentId,
        content: replyContent.value,
      }
      emit('submit-reply', replyData);
      
      activeReplyId.value = null;
      replyContent.value = '';
      replying.value = false;
    };

    const handleLike = (comment: any) => {
      const cancel = !!comment.isVoted;
      emit('like-comment', comment.id, cancel);
    };

    const flatten = (comments: any[]): any[] => {
      let result: any[] = [];
      for (const comment of comments) {
        // 日志：打印每个子评论
        console.log('flatten处理:', {id: comment.id, parentId: comment.parentId, replyToUsername: comment.replyToUsername});
        result.push(comment);
        if (comment.children && comment.children.length > 0) {
          result = result.concat(flatten(comment.children));
        }
      }
      return result;
    };

    const flattenedReplies = computed(() => {
      if (props.comment.children && props.comment.children.length > 0) {
        const flat = flatten(props.comment.children);
        // 日志：打印所有子评论
        console.log('flattenedReplies:', flat.map(r => ({id: r.id, parentId: r.parentId, replyToUsername: r.replyToUsername})));
        return flat;
      }
      return [];
    });

    return {
      activeReplyId,
      replyContent,
      replying,
      toggleReplyFor,
      handleReply,
      flattenedReplies,
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
.replies-container {
  margin-top: 16px;
  padding-left: 24px;
  border-left: 2px solid #f0f0f0;
}
.reply-item {
  margin-top: 8px;
}
</style> 