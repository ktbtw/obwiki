<template>
  <div class="post-detail-view">
    <a-row :gutter="[16, 16]">
      <a-col :span="24">
        <a-page-header
          :title="post.title"
          @back="$router.back()"
        >
          <template #extra>
            <a-space>
              <a-tag color="blue">
                <eye-outlined /> {{ post.viewCount || 0 }} 浏览
              </a-tag>
              <a-tag
                :color="post.isVoted ? 'blue' : 'default'"
                @click="handleVote"
                style="cursor: pointer;"
              >
                <like-outlined :style="{ color: post.isVoted ? 'blue' : '#aaa' }" />
                {{ post.voteCount || 0 }} {{ post.isVoted ? '已点赞' : '点赞' }}
              </a-tag>
              <a-divider type="vertical" />
              <a-avatar :size="32" :src="getImageUrl(post.avatar)" icon="user" />
              <span class="author">{{ post.username }}</span>
              <a-divider type="vertical" />
              <span class="time">
                <clock-circle-outlined />
                {{ new Date(post.createTime).toLocaleString() }}
              </span>
            </a-space>
          </template>
          <template #tags>
            <a-tag color="cyan">社区讨论</a-tag>
          </template>
        </a-page-header>
      </a-col>

      <a-col :span="24">
        <a-card :bordered="false">
          <div class="post-content" v-html="marked.parse(post.content || '')"></div>
        </a-card>
      </a-col>

      <a-col :span="24">
        <a-card :bordered="false" title="评论区" id="comment-section">
          <template v-if="isLogin">
            <div class="comment-editor">
              <a-form :model="newComment" layout="vertical">
                <a-form-item>
                  <a-textarea
                    v-model:value="newComment.content"
                    :rows="4"
                    placeholder="写下你的评论..."
                    :max-length="500"
                  />
                </a-form-item>
                <a-form-item>
                  <a-button type="primary" @click="submitComment(null)">
                    发表评论
                  </a-button>
                </a-form-item>
              </a-form>
            </div>
          </template>
          <template v-else>
            <a-alert
              type="info"
              message="请先登录后再发表评论"
              show-icon
              class="login-alert"
            />
          </template>

          <a-divider />

          <div v-if="comments.length > 0">
            <comment-item
              v-for="comment in comments"
              :key="comment.id"
              :comment="comment"
              @submit-reply="submitComment"
              @like-comment="handleCommentLike"
            />
          </div>
          <a-empty v-else description="暂无评论"/>
        </a-card>
      </a-col>
    </a-row>
  </div>
</template>

<script lang="ts">
import { createComment, voteComment } from '@/api/comment';
import api from '@/api/index';
import { deletePost, getPostDetail, votePost } from '@/api/post';
import store from '@/store';
import SessionStorage from '@/utils/session-storage';
import { Tool } from '@/utils/tool';
import {
ClockCircleOutlined,
DeleteOutlined,
EyeOutlined,
LikeOutlined,
MessageOutlined
} from '@ant-design/icons-vue';
import { message } from 'ant-design-vue';
import { marked } from 'marked';
import { computed, defineComponent, nextTick, onMounted, onUnmounted, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import CommentItem from './CommentItem.vue';

export default defineComponent({
  name: 'PostDetailView',
  components: {
    EyeOutlined,
    LikeOutlined,
    MessageOutlined,
    ClockCircleOutlined,
    CommentItem,
    DeleteOutlined
  },
  setup() {
    const route = useRoute();
    const router = useRouter();
    const loading = ref(false);
    const post = ref<any>({});
    const comments = ref<any[]>([]);
    const user = computed(() => store.state.user);
    const isLogin = computed(() => Tool.isNotEmpty(user.value?.token));

    const newComment = ref({
      content: '',
    });

    let websocket: any = null;
    let wsToken: string = '';
    let reconnectTimer: any = null;
    let reconnectAttempts = 0;
    const MAX_RECONNECT_ATTEMPTS = 10;

    const USER = 'USER';
    const sessionUser = SessionStorage.get(USER) || {};
    const currentUserId = sessionUser.id;

    // WebSocket实时刷新，带自动重连
    const initWebSocket = () => {
      if ('WebSocket' in window) {
        wsToken = Tool.uuid(10);
        websocket = new WebSocket(process.env.VUE_APP_WS_SERVER + '/ws/' + wsToken);
        websocket.onopen = () => {
          console.log('WebSocket连接成功', websocket);
          reconnectAttempts = 0;
        };
        websocket.onmessage = (event: any) => {
          console.log('WebSocket收到消息:', event.data);
          loadPost();
        };
        websocket.onerror = (e: any) => {
          console.log('WebSocket连接错误', e);
          tryReconnect();
        };
        websocket.onclose = (e: any) => {
          console.log('WebSocket连接关闭', e);
          tryReconnect();
        };
      }
    };

    function tryReconnect() {
      if (reconnectAttempts < MAX_RECONNECT_ATTEMPTS) {
        reconnectAttempts++;
        clearTimeout(reconnectTimer);
        reconnectTimer = setTimeout(() => {
          console.log(`WebSocket尝试重连，第${reconnectAttempts}次`);
          initWebSocket();
        }, 2000 * reconnectAttempts);
      } else {
        console.log('WebSocket重连次数已达上限，停止重连');
      }
    }

    const loadPost = async () => {
      loading.value = true;
      console.log('调用loadPost，准备请求文章详情和评论');
      try {
        const res = await getPostDetail(route.params.id as string, user.value?.id);
        if (Tool.isNotEmpty(res.data)) {
          post.value = res.data;
          comments.value = res.data.comments || [];
          console.log('loadPost获取到数据:', res.data);
        }
      } catch (error) {
        message.error('加载文章详情失败');
        console.log('loadPost请求失败', error);
      } finally {
        loading.value = false;
      }
    };

    const handleVote = async () => {
      if (!isLogin.value) {
        message.warning('请先登录后再点赞');
        return;
      }
      const cancel = !!post.value.isVoted;
      try {
        await votePost(post.value.id, user.value.id, cancel);
        if (cancel) {
          post.value.voteCount = Math.max((post.value.voteCount || 1) - 1, 0);
          post.value.isVoted = false;
          message.info('已取消点赞');
        } else {
          post.value.voteCount = (post.value.voteCount || 0) + 1;
          post.value.isVoted = true;
          message.success({ content: '点赞成功', style: { color: '#1677ff' } });
        }
      } catch (error) {
        message.error('操作失败，请稍后再试');
      }
    };

    const submitComment = async (replyData: { parentId: number; content: string } | null) => {
      let params;
      if (replyData) {
        // 这是回复
        params = {
          content: replyData.content,
          parentId: replyData.parentId,
          postId: Number(route.params.id),
          userId: user.value?.id,
        };
      } else {
        // 这是主评论
        if (!newComment.value.content.trim()) {
          message.warning('评论内容不能为空');
          return;
        }
        params = {
          content: newComment.value.content,
          parentId: undefined,
          postId: Number(route.params.id),
          userId: user.value?.id,
        };
      }
      console.log('发表评论时的 userId:', user.value?.id, '全局 user:', store.state.user, 'params:', params);
      try {
        await createComment(params);
        message.success('评论发表成功');
        newComment.value.content = ''; // 只清空主评论框
        await loadPost(); // 重新加载整个评论树
      } catch (error) {
        message.error('评论发表失败，请稍后再试');
      }
    };

    const findCommentInTree = (tree: any[], commentId: number): any | null => {
      for (const node of tree) {
        if (node.id === commentId) {
          return node;
        }
        if (node.children) {
          const found = findCommentInTree(node.children, commentId);
          if (found) {
            return found;
          }
        }
      }
      return null;
    };

    const handleCommentLike = async (commentId: number, cancel: boolean) => {
      if (!isLogin.value) {
        message.warning('请先登录后再点赞');
        return;
      }
      const targetComment = findCommentInTree(comments.value, commentId);
      try {
        await voteComment(commentId, user.value.id, cancel);
        if (cancel) {
          targetComment.voteCount = Math.max((targetComment.voteCount || 1) - 1, 0);
          targetComment.isVoted = false;
          message.info('已取消点赞');
        } else {
          targetComment.voteCount = (targetComment.voteCount || 0) + 1;
          targetComment.isVoted = true;
          message.success({ content: '点赞成功', style: { color: '#1677ff' } });
        }
      } catch (error) {
        message.error('操作失败，请稍后再试');
      }
    };

    const handleDeletePost = async () => {
      console.log('点击删除按钮，post:', post.value, 'user:', user.value);
      if (!window.confirm('确定要删除这篇文章吗？删除后不可恢复！')) return;
      try {
        await deletePost(post.value.id);
        message.success('删除成功');
        router.push('/');
      } catch (error) {
        message.error('删除失败，请稍后再试');
      }
    };

    // 封装图片URL生成方法
    const baseURL = api.defaults.baseURL?.replace(/\/$/, '') || '';
    function getImageUrl(path: string) {
      if (!path) return '';
      if (/^https?:\/\//.test(path)) return path;
      return baseURL + path;
    }

    onMounted(() => {
      loadPost();
      initWebSocket();
      // 自动滚动到评论区
      if (route.query.scrollTo === 'comment') {
        nextTick(() => {
          const el = document.getElementById('comment-section');
          if (el) el.scrollIntoView({ behavior: 'smooth' });
        });
      }
    });

    // 组件卸载时关闭WebSocket
    onUnmounted(() => {
      if (websocket) websocket.close();
      clearTimeout(reconnectTimer);
    });

    return {
      post,
      comments,
      loading,
      isLogin,
      newComment,
      marked,
      handleVote,
      submitComment,
      handleCommentLike,
      getImageUrl,
      handleDeletePost,
      user,
      currentUserId
    };
  }
});
</script>

<style scoped>
.post-detail-view {
  max-width: 1000px;
  margin: 0 auto;
  padding: 24px;
}

.post-content {
  font-size: 16px;
  line-height: 1.8;
  color: #1a1a1a;
}

.post-content :deep(p) {
  margin-bottom: 16px;
}

.post-content :deep(img) {
  max-width: 100%;
  height: auto;
  margin: 16px 0;
}

.author {
  font-weight: 500;
  color: #1a1a1a;
}

.time {
  color: #999;
  display: flex;
  align-items: center;
  gap: 4px;
}

.comment-editor {
  margin-bottom: 24px;
}

.login-alert {
  margin-bottom: 24px;
}

:deep(.ant-page-header) {
  padding: 24px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.06);
}

:deep(.ant-card) {
  border-radius: 8px;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.06);
}

:deep(.ant-list-item) {
  padding: 16px 0;
}

:deep(.ant-comment) {
  width: 100%;
}
</style> 