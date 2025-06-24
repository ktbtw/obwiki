<template>
  <!-- 该页面进行海洋生物电子书管理 -->
  <div>
    海洋生物电子书
    <a-layout>
    <a-layout-content
      :style="{ background: '#fff', padding: '24px', margin: 0, minHeight: '280px' }">

      <p>
        <a-form layout="inline" :model="param">

          <a-form-item>
            <a-input v-model:value="param.name" placeholder="名称">
            </a-input>
          </a-form-item>
          <a-form-item>
            <a-button type="primary" @click="handleQuery({page: 1, size: pagination.pageSize})">
              查询
            </a-button>
          </a-form-item>

          <a-form-item>
            <a-button type="primary" @click="add()">
              新增
            </a-button>
          </a-form-item>
        </a-form>
      </p>

      <a-table
        :columns="columns"
        :row-key="(record: any) => record.id"
        :data-source="cateorys"
        :pagination="pagination"
        :loading="loading"
        @change="handleTableChange"
      >
        <template #bodyCell="{column, record}">
          <template v-if="column.key === 'cover'">
            <a-image :src="record.cover" alt="图片加载失败" style="width:80px;height:80px"/>
          </template>


          <template v-if="column.dataIndex === 'action'">
            <a-space size="small">
              <a-button type="primary"  @click="edit(record)">
                编辑
              </a-button>
              <a-popconfirm
                title="删除后不可以恢复，确认删除?"
                ok-text="是"
                cancel-text="否"
                @confirm="console.log(record)"
              >
                <a-button type="danger" >
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
      title="电子书表单"
      v-model:visible="modalVisible"
      :confirm-loading="modalLoading"
      @ok="handleModalOk"
    >
      <a-form :model="cateory" :label-col="{ span: 6 }" :wrapper-col="{ span: 18 }">
        <a-form-item label="封面">
          <a-input v-model:value="cateory.cover" />
        </a-form-item>
        <a-form-item label="名称">
          <a-input v-model:value="cateory.name" />
        </a-form-item>
        <a-form-item label="分类一">
          <a-input v-model:value="cateory.category1Id" />
        </a-form-item>
        <a-form-item label="分类二">
          <a-input v-model:value="cateory.category2Id" />
        </a-form-item>
        <a-form-item label="描述">
          <a-textarea v-model:value="cateory.description" type="textarea" />
        </a-form-item>
      </a-form>
    </a-modal>

  </div>
</template>

<script lang="ts" setup>

import { ref, onMounted } from 'vue';
import api from '@/api/index'
import { Tool } from "@/utils/tool";

const cateorys = ref([]);//定义查询电子书返回集合
// 编辑相关功能
const cateory = ref();
const modalVisible = ref(false);
const modalLoading = ref(false);
//编辑
const edit = (record:any)=>{
  modalVisible.value =true;
  cateory.value = Tool.copy(record);
}

//新增
const add = ()=>{
  modalVisible.value =true;
  //清空对话框数据
  cateory.value = {};
}

//删除
const handleDelete = (id:number)=>{
  api.delete('/cateory/delete/'+id).then((resp)=>{
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
  api.post("/cateory/save",cateory.value).then(resp =>{
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

//定义分页参数
const pagination = ref({
  current: 1,
  pageSize: 5,
  total: 0
});
const loading = ref(false);
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
    title: 'Action',
    key: 'action',
    dataIndex:'action'

  }
];

onMounted(() => {
  handleQuery({
    page: 1,
    size: pagination.value.pageSize
  });
})

const param = ref();
param.value = {};

/*
* 数据查询
* */
const handleQuery = (params:any)=>{
  loading.value = true;
  api.get("/cateory/list",{
    params:{
      page:params.page,
      size:params.size,
      name:param.value.name
    }}).then((resp)=>{
    loading.value = false;
    const data = resp.data;
    if (data.success){
      //获取查询数据
      cateorys.value = data.content.list;
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



</script>
