<template>
  <div
    v-if="post"
    class="post-detail-view"
  >
    <h2>{{ post.title }}</h2>
    <div class="meta">
      <span>作者ID: {{ post.userId }}</span>
      <span>时间: {{ new Date(post.createTime).toLocaleString() }}</span>
      <span>
        <eye-outlined /> {{ post.viewCount || 0 }}
      </span>
      <span>
        <like-outlined /> {{ post.voteCount || 0 }}
      </span>
    </div>
    <div class="content">
      {{ post.content }}
    </div>

    <div class="vote-section">
      <a-button
        type="primary"
        shape="round"
        :size="'large'"
        @click="handleVote"
      >
        <template #icon>
          <like-outlined />
        </template>
        点赞 {{ post.voteCount || 0 }}
      </a-button>
    </div>

    <h3>评论</h3>
    <ul>
      <li
        v-for="comment in comments"
        :key="comment.id"
        class="comment-item"
      >
        <div class="comment-meta">
          <span>用户ID: {{ comment.userId }}</span>
          <span>时间: {{ comment.createTime }}</span>
        </div>
        <div class="comment-content">
          {{ comment.content }}
        </div>
      </li>
    </ul>

    <div
      v-if="isLogin"
      class="comment-form"
    >
      <textarea
        v-model="commentContent"
        placeholder="写下你的评论..."
        rows="3"
      />
      <button @click="submitComment">
        发表评论
      </button>
    </div>
    <div
      v-else
      class="comment-form"
    >
      <span>请先登录后再评论。</span>
    </div>
  </div>
  <div v-else>
    加载中...
  </div>
</template>

<script lang="ts">
import store from '@/store';
import { message } from 'ant-design-vue';
import { computed, defineComponent, onMounted, ref } from 'vue';
import { useRoute } from 'vue-router';
import { createComment, getCommentList } from '../api/comment';
import { getPostDetail, votePost } from '../api/post';
import { EyeOutlined, LikeOutlined } from '@ant-design/icons-vue';

export default defineComponent({
  name: 'PostDetailView',
  components: {
    EyeOutlined,
    LikeOutlined,
  },
  setup() {
    const route = useRoute();
    const post = ref<any>(null);
    const comments = ref<any[]>([]);
    const commentContent = ref('');
    const user = computed(() => store.state.user);
    const isLogin = computed(() => !!user.value?.token);

    const loadPost = async () => {
      const res = await getPostDetail(Number(route.params.id));
      post.value = res.data;
    };
    const loadComments = async () => {
      const res = await getCommentList(Number(route.params.id));
      comments.value = res.data;
    };
    const submitComment = async () => {
      if (!commentContent.value.trim()) return;
      await createComment({
        postId: Number(route.params.id),
        userId: user.value?.id,
        content: commentContent.value
      });
      commentContent.value = '';
      await loadComments();
    };

    const handleVote = async () => {
      if (!isLogin.value) {
        message.warning('请先登录后再点赞');
        return;
      }
      try {
        await votePost(post.value.id);
        post.value.voteCount++;
        message.success('点赞成功！');
      } catch (error) {
        message.error('点赞失败，请稍后再试');
      }
    };

    onMounted(() => {
      loadPost();
      loadComments();
    });

    return { post, comments, commentContent, isLogin, submitComment, handleVote };
  }
});
</script>

<style scoped>
.post-detail-view { max-width: 700px; margin: 0 auto; }
.meta { color: #888; font-size: 13px; margin-bottom: 8px; }
.meta span { margin-right: 16px; }
.meta .anticon { margin-right: 4px; }
.content { margin-bottom: 24px; }
.comment-item { border-bottom: 1px solid #eee; padding: 12px 0; }
.comment-meta { color: #888; font-size: 12px; margin-bottom: 4px; }
.comment-content { color: #444; }
.comment-form { margin-top: 16px; }
textarea { width: 100%; resize: vertical; }
button { margin-top: 8px; }
.vote-section {
  text-align: center;
  margin: 20px 0;
}
</style> 