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
              <a-tag :color="post.isVoted ? 'red' : 'default'" @click="handleVote">
                <like-outlined /> {{ post.voteCount || 0 }} 点赞
              </a-tag>
              <a-divider type="vertical" />
              <a-avatar :size="32">{{ post.userId }}</a-avatar>
              <span class="author">{{ post.userId }}</span>
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
        <a-card :bordered="false" title="评论区">
          <template v-if="isLogin">
            <div class="comment-editor">
              <a-form :model="newComment" layout="vertical">
                <a-form-item>
                  <a-textarea
                    v-model="newComment.content"
                    :rows="4"
                    placeholder="写下你的评论..."
                    :maxLength="500"
                    show-count
                  />
                </a-form-item>
                <a-form-item>
                  <a-button type="primary" @click="submitComment">
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

          <a-list
            :data-source="comments"
            :loading="loading"
            item-layout="horizontal"
          >
            <template #renderItem="{ item }">
              <a-list-item>
                <a-comment
                  :author="item.userId"
                  :content="item.content"
                  :datetime="new Date(item.createTime).toLocaleString()"
                >
                  <template #avatar>
                    <a-avatar>{{ item.userId }}</a-avatar>
                  </template>
                  <template #actions>
                    <a-space>
                      <a-button type="link" size="small">
                        <message-outlined /> 回复
                      </a-button>
                      <a-button type="link" size="small" @click="handleCommentVote(item.id)">
                        <like-outlined /> {{ item.voteCount || 0 }} 点赞
                      </a-button>
                    </a-space>
                  </template>
                </a-comment>
              </a-list-item>
            </template>
            <template #empty>
              <a-empty description="暂无评论" />
            </template>
          </a-list>
        </a-card>
      </a-col>
    </a-row>
  </div>
</template>

<script lang="ts">
import { createComment, voteComment } from '@/api/comment';
import { getPostDetail, votePost } from '@/api/post';
import store from '@/store';
import { Tool } from '@/utils/tool';
import {
ClockCircleOutlined,
EyeOutlined,
LikeOutlined,
MessageOutlined
} from '@ant-design/icons-vue';
import { message } from 'ant-design-vue';
import { marked } from 'marked';
import { computed, defineComponent, onMounted, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';

export default defineComponent({
  name: 'PostDetailView',
  components: {
    EyeOutlined,
    LikeOutlined,
    MessageOutlined,
    ClockCircleOutlined
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
      postId: Number(route.params.id),
      userId: user.value?.id
    });

    const loadPost = async () => {
      loading.value = true;
      try {
        const res = await getPostDetail(route.params.id as string);
        if (Tool.isNotEmpty(res.data)) {
          post.value = res.data;
          comments.value = res.data.comments || [];
        }
      } catch (error) {
        message.error('加载文章详情失败');
      } finally {
        loading.value = false;
      }
    };

    const handleVote = async () => {
      if (!isLogin.value) {
        message.warning('请先登录后再点赞');
        return;
      }

      try {
        await votePost(post.value.id, user.value.id);
        post.value.voteCount = (post.value.voteCount || 0) + 1;
        post.value.isVoted = true;
        message.success('点赞成功');
      } catch (error) {
        message.error('点赞失败，请稍后再试');
      }
    };

    const submitComment = async () => {
      if (!newComment.value.content.trim()) {
        message.warning('评论内容不能为空');
        return;
      }

      try {
        await createComment(newComment.value);
        message.success('评论发表成功');
        newComment.value.content = '';
        await loadPost(); // 重新加载评论列表
      } catch (error) {
        message.error('评论发表失败，请稍后再试');
      }
    };

    const handleCommentVote = async (commentId: number) => {
      if (!isLogin.value) {
        message.warning('请先登录后再点赞');
        return;
      }

      try {
        await voteComment(commentId, user.value.id);
        message.success('点赞成功');
        await loadPost(); // 重新加载评论列表
      } catch (error) {
        message.error('点赞失败，请稍后再试');
      }
    };

    onMounted(loadPost);

    return {
      post,
      comments,
      loading,
      isLogin,
      newComment,
      marked,
      handleVote,
      submitComment,
      handleCommentVote
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