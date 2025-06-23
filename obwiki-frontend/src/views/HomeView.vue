<template>
  <div>
    <a-layout>
      <a-layout-sider
        width="200"
        style="background: #fff"
      >
        <a-menu
          v-model:selectedKeys="selectedKeys2"
          :open-keys="openKeys"
          mode="inline"
          style="height: 100%"
        >
          <a-sub-menu key="sub1">
            <template #title>
              <span>
                <user-outlined />
                subnav 11111
              </span>
            </template>
            <a-menu-item key="1">
              option1
            </a-menu-item>
            <a-menu-item key="2">
              option2
            </a-menu-item>
            <a-menu-item key="3">
              option3
            </a-menu-item>
            <a-menu-item key="4">
              option4
            </a-menu-item>
          </a-sub-menu>
          <a-sub-menu key="sub2">
            <template #title>
              <span>
                <laptop-outlined />
                subnav 2
              </span>
            </template>
            <a-menu-item key="5">
              option5
            </a-menu-item>
            <a-menu-item key="6">
              option6
            </a-menu-item>
            <a-menu-item key="7">
              option7
            </a-menu-item>
            <a-menu-item key="8">
              option8
            </a-menu-item>
          </a-sub-menu>
          <a-sub-menu key="sub3">
            <template #title>
              <span>
                <notification-outlined />
                subnav 3
              </span>
            </template>
            <a-menu-item key="9">
              option9
            </a-menu-item>
            <a-menu-item key="10">
              option10
            </a-menu-item>
            <a-menu-item key="11">
              option11
            </a-menu-item>
            <a-menu-item key="12">
              option12
            </a-menu-item>
          </a-sub-menu>
        </a-menu>
      </a-layout-sider>
      <a-layout-content :style="{ padding: '0 24px', minHeight: '280px' }">
        <a-list
          item-layout="vertical"
          size="large"
          :grid="{ gutter: 20, column: 3 }"
          :data-source="ebooks"
        >
          <template #renderItem="{ item }">
            <a-list-item key="item.name">
              <template #actions>
                <span
                  v-for="{ icon, text } in actions"
                  :key="icon"
                >
                  <component
                    :is="icon"
                    style="margin-right: 8px"
                  />
                  {{ text }}
                </span>
              </template>

              <a-list-item-meta :description="item.description">
                <template #title>
                  <a :href="item.href">{{ item.name }}</a>
                </template>
                <template #avatar>
                  <a-avatar :src="item.cover" />
                </template>
              </a-list-item-meta>
            </a-list-item>
          </template>
        </a-list>
      </a-layout-content>
    </a-layout>
  </div>
</template>

<script lang="ts" setup>
import { ref, onMounted } from 'vue';
import api from '../api/index.ts'
import { StarOutlined, LikeOutlined, MessageOutlined } from '@ant-design/icons-vue';
//定义响应式数据
const ebooks = ref();
//完成渲染后执行
onMounted(() => {
  handleQueryEbook();
})

const handleQueryEbook = () => {
  api.get("/ebook/list").then(resp => {
    console.log(resp.data.content);
    ebooks.value = resp.data.content;
  });
}

const actions: Record<string, any>[] = [
  { icon: StarOutlined, text: '156' },
  { icon: LikeOutlined, text: '156' },
  { icon: MessageOutlined, text: '2' },
];

</script>

<style scoped>
.ant-avatar {
  width: 50px;
  height: 50px;
  line-height: 50px;
  border-radius: 8%;
  margin: 5px 0;
}
</style>
