<template>
  <div class="post-list-view">
    <a-row :gutter="[16, 16]">
      <a-col :span="24">
        <div class="header-section">
          <div class="left">
            <h2 class="page-title">社区讨论</h2>
            <div class="filters">
              <div style="margin-bottom: 8px; color: #666; font-size: 12px;">
                当前排序: {{ currentFilter === 'latest' ? '最新发布' : currentFilter === 'hot' ? '最热门' : '最多浏览' }}
              </div>
              <a-radio-group v-model="currentFilter" button-style="solid" @change="handleFilterChange">
                <a-radio-button value="latest">最新发布</a-radio-button>
                <a-radio-button value="hot">最热门</a-radio-button>
                <a-radio-button value="most-viewed">最多浏览</a-radio-button>
              </a-radio-group>
            </div>
          </div>
          <a-button 
            v-if="isLogin" 
            type="primary" 
            @click="showModal"
            size="large"
          >
            <template #icon><form-outlined /></template>
            发布新帖子
          </a-button>
        </div>
      </a-col>

      <a-col :span="24">
        <a-list
          :data-source="posts"
          :loading="loading"
          item-layout="vertical"
          size="large"
        >
          <template #renderItem="{ item }">
            <a-list-item key="item.id">
              <a-card :bordered="false" hoverable>
                <template #extra>
                  <a-space>
                    <a-tag color="blue">
                      <eye-outlined /> {{ item.viewCount || 0 }}
                    </a-tag>
                    <a-tag color="red">
                      <like-outlined /> {{ item.voteCount || 0 }}
                    </a-tag>
                  </a-space>
                </template>

                <a-list-item-meta>
                  <template #title>
                    <router-link :to="`/post/${item.id}`" class="post-title">
                      {{ item.title }}
                    </router-link>
                  </template>
                  <template #avatar>
                    <a-avatar :size="48" :src="getImageUrl(item.avatar)" icon="user" />
                  </template>
                  <template #description>
                    <a-space>
                      <span class="author">作者: {{ item.username }}</span>
                      <a-divider type="vertical" />
                      <span class="time">
                        <clock-circle-outlined />
                        {{ new Date(item.createTime).toLocaleString() }}
                      </span>
                    </a-space>
                  </template>
                </a-list-item-meta>

                <div class="post-content">{{ item.content.slice(0, 200) }}...</div>

                <template #actions>
                  <a-space>
                    <a-button type="link" @click="goToComment(item.id)">
                      <message-outlined /> 评论
                    </a-button>
                    <a-button type="link">
                      <share-alt-outlined /> 分享
                    </a-button>
                    <a-button
                      v-if="isLogin && String(item.userId) === String(currentUserId)"
                      type="link"
                      danger
                      @click="handleDeletePost(item.id)"
                    >
                      <DeleteOutlined /> 删除
                    </a-button>
                    <span style="display:none">
                      {{ console.log('删除按钮条件:', isLogin, user.value, item.userId, user.value && item.userId === user.value.id, typeof item.userId, typeof (user.value && user.value.id)) }}
                    </span>
                  </a-space>
                </template>
              </a-card>
            </a-list-item>
          </template>
        </a-list>
      </a-col>
    </a-row>

    <a-modal
      v-model:visible="modalVisible"
      title="发布新帖子"
      :confirm-loading="modalLoading"
      @ok="handleOk"
      @cancel="handleCancel"
      destroyOnClose
      width="700px"
    >
      <a-form :model="newPost" layout="vertical">
        <a-form-item 
          label="标题" 
          :rules="[{ required: true, message: '请输入标题' }]"
        >
          <a-input 
            v-model:value="newPost.title" 
            placeholder="请输入标题"
            :maxLength="100"
            show-count
            size="large"
          />
        </a-form-item>
        <a-form-item 
          label="内容"
          :rules="[{ required: true, message: '请输入内容' }]"
        >
          <a-textarea 
            v-model:value="newPost.content" 
            :rows="8"
            placeholder="请输入内容"
            :maxLength="2000"
            show-count
          />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script lang="ts">
import api from '@/api/index';
import { createPost, deletePost, getPostList } from '@/api/post';
import store from '@/store';
import SessionStorage from '@/utils/session-storage';
import {
ClockCircleOutlined,
DeleteOutlined,
EyeOutlined,
FormOutlined,
LikeOutlined,
MessageOutlined,
ShareAltOutlined
} from '@ant-design/icons-vue';
import { message, Modal } from 'ant-design-vue';
import { computed, defineComponent, onMounted, onUnmounted, ref, watch } from 'vue';
import { useRouter } from 'vue-router';

export default defineComponent({
  name: 'PostListView',
  components: {
    EyeOutlined,
    LikeOutlined,
    FormOutlined,
    MessageOutlined,
    ShareAltOutlined,
    ClockCircleOutlined,
    DeleteOutlined
  },
  setup() {
    const posts = ref<any[]>([]);
    const loading = ref(false);
    const user = computed(() => store.state.user);
    const isLogin = computed(() => !!user.value?.token);
    const currentFilter = ref('latest');
    const router = useRouter();

    // Modal related state
    const modalVisible = ref(false);
    const modalLoading = ref(false);
    const newPost = ref({
      title: '',
      content: '',
      userId: user.value?.id,
    });

    // 封装图片URL生成方法
    const baseURL = api.defaults.baseURL?.replace(/\/$/, '') || '';
    function getImageUrl(path: string) {
      if (!path) return '';
      if (/^https?:\/\//.test(path)) return path;
      return baseURL + path;
    }

    const loadPosts = async () => {
      loading.value = true;
      try {
        const res = await getPostList();
        posts.value = res.data;
        
        // 根据当前筛选条件排序
        sortPosts();
      } catch (error) {
        message.error('加载文章列表失败');
      } finally {
        loading.value = false;
      }
    };

    // 前端排序函数
    const sortPosts = () => {
      console.log('🔄 开始排序，当前筛选条件:', currentFilter.value);
      const postsCopy = [...posts.value];
      switch (currentFilter.value) {
        case 'hot':
          postsCopy.sort((a, b) => (b.voteCount || 0) - (a.voteCount || 0));
          console.log('🔥 按热度排序完成');
          break;
        case 'most-viewed':
          postsCopy.sort((a, b) => (b.viewCount || 0) - (a.viewCount || 0));
          console.log('👁️ 按浏览量排序完成');
          break;
        default:
          postsCopy.sort((a, b) => new Date(b.createTime).getTime() - new Date(a.createTime).getTime());
          console.log('📅 按时间排序完成');
      }
      posts.value = postsCopy;
      console.log('✅ 排序完成，文章数量:', posts.value.length);
    };

    // 监听筛选条件变化 - 只进行前端排序，不重新请求数据
    watch(currentFilter, (newValue, oldValue) => {
      console.log('🎯 筛选条件变化:', oldValue, '->', newValue);
      sortPosts();
    });

    // 处理筛选条件变化
    const handleFilterChange = (e: any) => {
      console.log('🎯 点击筛选按钮:', e.target.value);
      currentFilter.value = e.target.value;
    };

    const showModal = () => {
      newPost.value = {
        title: '',
        content: '',
        userId: user.value?.id,
      };
      console.log('打开发帖弹窗，newPost:', newPost.value);
      modalVisible.value = true;
    };

    const handleOk = async () => {
      console.log('点击发布，newPost:', newPost.value);
      if (!newPost.value.title.trim() || !newPost.value.content.trim()) {
        message.warning('标题和内容不能为空！');
        return;
      }

      modalLoading.value = true;
      try {
        await createPost(newPost.value);
        message.success('发布成功！');
        modalVisible.value = false;
        await loadPosts();
      } catch (error) {
        message.error('发布失败，请稍后再试。');
      } finally {
        modalLoading.value = false;
      }
    };

    const handleCancel = () => {
      modalVisible.value = false;
      newPost.value = {
        title: '',
        content: '',
        userId: user.value?.id,
      };
    };

    const goToComment = (postId: number) => {
      router.push({ path: `/post/${postId}`, query: { scrollTo: 'comment' } });
    };

    const handleDeletePost = async (postId: number) => {
      Modal.confirm({
        title: '确认删除',
        content: '确定要删除这篇文章吗？删除后不可恢复！',
        okText: '删除',
        okType: 'danger',
        cancelText: '取消',
        onOk: async () => {
          try {
            await deletePost(postId);
            message.success('删除成功');
            await loadPosts();
          } catch (error) {
            message.error('删除失败，请稍后再试');
          }
        },
      });
    };

    let websocket: any = null;
    let wsToken: string = '';
    let reconnectTimer: any = null;
    let reconnectAttempts = 0;
    const MAX_RECONNECT_ATTEMPTS = 10;

    // WebSocket实时刷新，带自动重连
    const initWebSocket = () => {
      if ('WebSocket' in window) {
        wsToken = Math.random().toString(36).slice(2, 12);
        websocket = new WebSocket(process.env.VUE_APP_WS_SERVER + '/ws/' + wsToken);
        websocket.onopen = () => {
          console.log('WebSocket连接成功');
          reconnectAttempts = 0;
        };
        websocket.onmessage = async () => {
          await loadPosts();
        };
        websocket.onerror = () => {
          console.log('WebSocket连接错误');
          tryReconnect();
        };
        websocket.onclose = () => {
          console.log('WebSocket连接关闭');
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

    const USER = 'USER';
    const sessionUser = SessionStorage.get(USER) || {};
    const currentUserId = sessionUser.id;

    onMounted(() => {
      loadPosts();
      initWebSocket();
    });
    onUnmounted(() => {
      if (websocket) websocket.close();
      clearTimeout(reconnectTimer);
    });

    return {
      posts,
      loading,
      isLogin,
      modalVisible,
      modalLoading,
      newPost,
      currentFilter,
      loadPosts,
      showModal,
      handleOk,
      handleCancel,
      goToComment,
      getImageUrl,
      handleDeletePost,
      handleFilterChange,
      user,
      currentUserId
    };
  }
});
</script>

<style scoped>
.post-list-view {
  max-width: 1000px;
  margin: 0 auto;
  padding: 24px;
}

.header-section {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.left {
  display: flex;
  align-items: center;
  gap: 24px;
}

.page-title {
  margin: 0;
  font-size: 24px;
  font-weight: 600;
}

.post-title {
  font-size: 18px;
  font-weight: 600;
  color: #1a1a1a;
  text-decoration: none;
  transition: color 0.3s;
}

.post-title:hover {
  color: #1890ff;
}

.author {
  color: #666;
}

.time {
  color: #999;
  display: flex;
  align-items: center;
  gap: 4px;
}

.post-content {
  color: #666;
  margin: 16px 0;
  line-height: 1.6;
}

:deep(.ant-card-body) {
  padding: 24px;
}

:deep(.ant-list-item) {
  padding: 0;
  margin-bottom: 16px;
}

:deep(.ant-card) {
  transition: all 0.3s;
}

:deep(.ant-card:hover) {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

:deep(.ant-list-item-meta-title) {
  margin-bottom: 8px !important;
}

:deep(.ant-avatar) {
  background-color: #1890ff;
}
</style>

<style>
/* 强制Ant Design Vue的Modal弹窗垂直居中 */
.ant-modal-root .ant-modal-wrap {
  display: flex !important;
  align-items: center !important;
  justify-content: center !important;
  min-height: 100vh;
}
</style> 