<template>
  <div class="post-list-view">
    <div class="header-bar">
      <a-button type="primary" v-if="isLogin" @click="showModal">
        发帖
      </a-button>
    </div>

    <ul>
      <li v-for="post in posts" :key="post.id" class="post-item">
        <router-link :to="`/post/` + post.id">
          <h3>{{ post.title }}</h3>
        </router-link>
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
        <div class="content-preview">{{ post.content.slice(0, 100) }}...</div>
      </li>
    </ul>

    <a-modal
      v-model:visible="modalVisible"
      title="发布新文章"
      :confirm-loading="modalLoading"
      @ok="handleOk"
    >
      <a-form :model="newPost" :label-col="{ span: 4 }" :wrapper-col="{ span: 20 }">
        <a-form-item label="标题">
          <a-input v-model:value="newPost.title" />
        </a-form-item>
        <a-form-item label="内容">
          <a-textarea v-model:value="newPost.content" :rows="6" />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script lang="ts">
import { createPost, getPostList } from '@/api/post';
import store from '@/store';
import { EyeOutlined, LikeOutlined } from '@ant-design/icons-vue';
import { message } from 'ant-design-vue';
import { computed, defineComponent, onMounted, ref } from 'vue';

export default defineComponent({
  name: 'PostListView',
  components: {
    EyeOutlined,
    LikeOutlined,
  },
  setup() {
    const posts = ref<any[]>([]);
    const user = computed(() => store.state.user);
    const isLogin = computed(() => !!user.value?.token);

    // Modal related state
    const modalVisible = ref(false);
    const modalLoading = ref(false);
    const newPost = ref({
      title: '',
      content: '',
      userId: user.value?.id,
    });

    const loadPosts = async () => {
      try {
        const res = await getPostList();
        posts.value = res.data;
      } catch (error) {
        message.error('加载文章列表失败');
      }
    };

    const showModal = () => {
      // Reset form before showing
      newPost.value = {
        title: '',
        content: '',
        userId: user.value?.id,
      };
      modalVisible.value = true;
    };

    const handleOk = async () => {
      if (!newPost.value.title.trim() || !newPost.value.content.trim()) {
        message.warn('标题和内容不能为空！');
        return;
      }

      modalLoading.value = true;
      try {
        await createPost(newPost.value);
        message.success('发布成功！');
        modalVisible.value = false;
        await loadPosts(); // Refresh list
      } catch (error) {
        message.error('发布失败，请稍后再试。');
      } finally {
        modalLoading.value = false;
      }
    };

    onMounted(loadPosts);

    return {
      posts,
      isLogin,
      modalVisible,
      modalLoading,
      newPost,
      loadPosts,
      showModal,
      handleOk,
    };
  }
});
</script>

<style scoped>
.post-list-view {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px;
}
.header-bar {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  margin-bottom: 20px;
}
.post-item {
  border-bottom: 1px solid #f0f0f0;
  padding: 16px 0;
}
.post-item h3 {
  margin-bottom: 8px;
  font-size: 18px;
}
.meta {
  color: #888;
  font-size: 13px;
  margin-bottom: 8px;
}
.meta span {
  margin-right: 16px;
}
.meta .anticon {
  margin-right: 4px;
}
.content-preview {
  color: #444;
  line-height: 1.6;
}
</style> 