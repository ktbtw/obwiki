<template>
  <!-- 该页面进行海洋生物电子书管理 -->
  <div>
    海洋生物电子书
    <a-layout>
      <a-layout-content
        :style="{ background: '#fff', padding: '24px', margin: 0, minHeight: '280px' }"
      >
        <p>
          <a-form
            layout="inline"
            :model="param"
          >
            <a-form-item>
              <a-input
                v-model:value="param.name"
                placeholder="名称"
              />
            </a-form-item>
            <a-form-item>
              <a-button
                type="primary"
                @click="handleQuery({page: 1, size: pagination.pageSize})"
              >
                查询
              </a-button>
            </a-form-item>

            <a-form-item>
              <a-button
                type="primary"
                @click="handleAdd()"
              >
                新增
              </a-button>
            </a-form-item>
          </a-form>

        <a-table
          :columns="columns"
          :row-key="(record:Ebook) => record.id"
          :data-source="ebooks"
          :pagination="pagination"
          :loading="loading"
          @change="handleTableChange"
        >
          <template #bodyCell="{column, record}">
            <template v-if="column.key === 'cover'">
              <a-image
                :src="'/public' + record.cover"
                alt="图片加载失败"
                style="width:80px;height:80px"
              />
            </template>


            <template v-if="column.dataIndex === 'action'">
              <a-space size="small">
                <a-button
                  type="primary"
                  @click="handleEdit(record)"
                >
                  编辑
                </a-button>
                <a-popconfirm
                  title="删除后不可以恢复，确认删除?"
                  ok-text="是"
                  cancel-text="否"
                  @confirm="handleDelete(record.id)"
                >
                  <a-button type="danger">
                    删除
                  </a-button>
                </a-popconfirm>
              </a-space>
            </template>
          </template>
        </a-table>
      </a-layout-content>
    </a-layout>

    <a-modal
      v-model:visible="modalVisible"
      title="电子书表单"
      :confirm-loading="modalLoading"
      @ok="handleModalOk"
    >
      <a-form
        :model="ebook"
        :label-col="{ span: 6 }"
        :wrapper-col="{ span: 18 }"
      >
        <a-form-item label="封面">
          <a-input v-model:value="ebook.cover" />
        </a-form-item>
        <a-form-item label="名称">
          <a-input v-model:value="ebook.name" />
        </a-form-item>
        <a-form-item label="分类一">
          <a-input v-model:value="ebook.category1Id" />
        </a-form-item>
        <a-form-item label="分类二">
          <a-input v-model:value="ebook.category2Id" />
        </a-form-item>
        <a-form-item label="描述">
          <a-textarea
            v-model:value="ebook.description"
            type="textarea"
          />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script lang="ts" setup>

import { ref, onMounted } from 'vue';
import { Tool } from "@/utils/tool";
import api from '@/api/index'

const ebooks = ref([]);//定义查询电子书返回集合
// 编辑相关功能
const ebook = ref();
//定义分页参数
const pagination = ref({
  current: 1,
  pageSize: 5,
  total: 0
});
// 定义加载状态
const loading = ref(false);
// 定义表格列
const columns = [
  {
    title: '封面',
    key:'cover',
    dataIndex: 'cover',
  },
  {
    title: '名称',
    dataIndex: 'name'
  },
  {
    title: '分类一',
    key: 'category1Id',
    dataIndex: 'category1Id'
  },
  {
    title: '分类二',
    dataIndex: 'category2Id'
  },
  {
    title: '文档数',
    dataIndex: 'docCount'
  },
  {
    title: '阅读数',
    dataIndex: 'viewCount'
  },
  {
    title: '点赞数',
    dataIndex: 'voteCount'
  },
  {
    title: '操作',
    key: 'action',
    dataIndex:'action'
  }
];
// 定义对话框相关状态
const modalVisible = ref(false);
const modalLoading = ref(false);
// 定义查询参数
// 这里使用ref来定义响应式对象
const param = ref();
param.value = {};

// 定义电子书接口
interface Ebook {
  id?: number;
  cover?: string;
  name?: string;
  category1Id?: number;
  category2Id?: number;
  description?: string;
  docCount?: number;
  viewCount?: number;
  voteCount?: number;
}

//编辑
const handleEdit = (record:any)=>{
  modalVisible.value =true;
  ebook.value = Tool.copy(record);
}

//新增
const handleAdd = ()=>{
  modalVisible.value =true;
  //清空对话框数据
  ebook.value = {};
}

//删除
const handleDelete = (id:number)=>{
  console.log("删除电子书ID："+id);
  api.delete('/ebook/delete/'+id).then((resp)=>{
    if (resp.data.success){
      //重新加载列表
      handleQuery({
        page:pagination.value.current,
        size:pagination.value.pageSize
      });
    }
  })
}

const handleModalOk = ()=>{
  modalLoading.value = true;
  api.post("/ebook/save",ebook.value).then(resp =>{
    //回去返回参数
    const data = resp.data;
    if(data.success){
      modalLoading.value = false;//关闭等待
      modalVisible.value = false;//关闭对话框
      //重新加载列表
      handleQuery({
        page:pagination.value.current,
        size:pagination.value.pageSize
      })
    }
  })
}

/*
* 数据查询
* */
const handleQuery = (params:any)=>{
  loading.value = true;
  api.get("/ebook/list",{
    params:{
      page:params.page,
      size:params.size,
      name:param.value.name
    }}).then((resp)=>{
    loading.value = false;
    const data = resp.data;
    if (data.success){
      //获取查询数据
      ebooks.value = data.content.list;
      //重置分页按钮
      pagination.value.current = params.page;
      //设置总条数
      pagination.value.total = data.content.total;
    }
  });
};
/*
* 表格点击页码时触发
* */
const handleTableChange =(pagination:any)=>{
  console.log("看看自带分页的参数都有些啥："+pagination);
  handleQuery({
    page:pagination.current,
    size:pagination.pageSize
  });
};

onMounted(() => {
  // 页面加载时查询数据
  handleQuery({
    // 默认查询第一页数据
    page: 1,
    // 每页显示5条数据
    size: pagination.value.pageSize
  });
})

</script>
