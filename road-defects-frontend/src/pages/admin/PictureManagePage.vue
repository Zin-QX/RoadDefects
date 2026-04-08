<template>
  <div id="pictureManagePage">
    <!-- 搜索表单 -->
    <a-form layout="inline" :model="searchParams" @finish="doSearch">
      <a-form-item label="图片名称">
        <a-input v-model:value="searchParams.name" placeholder="输入图片名称" allow-clear />
      </a-form-item>
      <a-form-item label="地址">
        <a-input v-model:value="searchParams.address" placeholder="输入地址" allow-clear />
      </a-form-item>
      <a-form-item label="审核状态">
        <a-select v-model:value="searchParams.reviewStatus" placeholder="选择审核状态" allow-clear>
          <a-select-option :value="0">待审核</a-select-option>
          <a-select-option :value="1">通过</a-select-option>
          <a-select-option :value="2">拒绝</a-select-option>
        </a-select>
      </a-form-item>
      <a-form-item>
        <a-button type="primary" html-type="submit">搜索</a-button>
      </a-form-item>
    </a-form>
    <div style="margin-bottom: 8px" />
    <!-- 表格 -->
    <a-table
      :columns="columns"
      :data-source="dataList"
      :pagination="pagination"
      @change="doTableChange"
      :scroll="{ x: 1800 }"
    >
      <template #bodyCell="{ column, record }">
        <template v-if="column.dataIndex === 'url'">
          <a-image :src="record.url" :width="80" />
        </template>
        <template v-else-if="column.dataIndex === 'processedUrl'">
          <a-image v-if="record.processedUrl" :src="record.processedUrl" :width="80" />
          <span v-else>-</span>
        </template>
        <template v-else-if="column.dataIndex === 'reviewStatus'">
          <a-tag v-if="record.reviewStatus === 0" color="orange">待审核</a-tag>
          <a-tag v-else-if="record.reviewStatus === 1" color="green">通过</a-tag>
          <a-tag v-else-if="record.reviewStatus === 2" color="red">拒绝</a-tag>
        </template>
        <template v-else-if="column.dataIndex === 'isDelete'">
          <a-tag v-if="record.isDelete === 0" color="green">未删除</a-tag>
          <a-tag v-else-if="record.isDelete === 1" color="red">已删除</a-tag>
        </template>
        <template v-else-if="column.dataIndex === 'picSize'">
          {{ formatFileSize(record.picSize) }}
        </template>
        <template v-else-if="column.dataIndex === 'processedResult'">
          <template v-if="record.processedResult">
            <span v-if="typeof record.processedResult === 'string'">
              {{ parseProcessedResult(record.processedResult) }}
            </span>
            <span v-else-if="Array.isArray(record.processedResult) && record.processedResult.length > 0">
              {{ record.processedResult.join('；') }}
            </span>
            <span v-else>-</span>
          </template>
          <span v-else>-</span>
        </template>
        <template v-else-if="column.dataIndex === 'createTime' || column.dataIndex === 'updateTime' || column.dataIndex === 'reviewTime'">
          <a-tooltip :title="dayjs(record[column.dataIndex]).format('YYYY-MM-DD HH:mm:ss')">
            <span class="ellipsis-text">{{ dayjs(record[column.dataIndex]).format('YYYY-MM-DD') }}</span>
          </a-tooltip>
        </template>
        <template v-else-if="column.dataIndex === 'id' || column.dataIndex === 'userId' || column.dataIndex === 'reviewerId'">
          <a-tooltip :title="record[column.dataIndex]">
            <span class="ellipsis-text">{{ record[column.dataIndex] }}</span>
          </a-tooltip>
        </template>
        <template v-else-if="column.dataIndex === 'name' || column.dataIndex === 'address' || column.dataIndex === 'reviewMessage'">
          <a-tooltip :title="record[column.dataIndex]">
            <span class="ellipsis-text">{{ record[column.dataIndex] }}</span>
          </a-tooltip>
        </template>
        <template v-else-if="column.key === 'action'">
          <a-space>
            <a-button
              v-if="record.reviewStatus === 0"
              type="primary"
              size="small"
              @click="showReviewModal(record)"
            >
              审核
            </a-button>
            <a-button danger size="small" @click="doDelete(record.id)">删除</a-button>
          </a-space>
        </template>
      </template>
    </a-table>

    <!-- 审核弹窗 -->
    <a-modal
      v-model:open="reviewModalVisible"
      title="图片审核"
      ok-text="确认"
      cancel-text="取消"
      @ok="submitReview"
    >
      <a-form :model="reviewForm" layout="vertical">
        <a-form-item label="审核状态" required>
          <a-select v-model:value="reviewForm.reviewStatus" placeholder="选择审核状态">
            <a-select-option :value="1">通过</a-select-option>
            <a-select-option :value="2">拒绝</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="审核信息">
          <a-textarea
            v-model:value="reviewForm.reviewMessage"
            placeholder="输入审核信息，如拒绝原因"
            :rows="4"
          />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>
<script lang="ts" setup>
import { computed, onMounted, reactive, ref } from 'vue'
import {
  listPictureByPageUsingPost,
  doPictureReviewUsingPost,
  deletePictureUsingPost
} from '@/api/pictureController.ts'
import { message, Modal } from 'ant-design-vue'
import dayjs from 'dayjs'

const columns = [
  {
    title: 'id',
    dataIndex: 'id',
    width: 80,
    fixed: 'left',
    align: 'center',
  },
  {
    title: '图片',
    dataIndex: 'url',
    width: 100,
    align: 'center',
  },
  {
    title: '图片名称',
    dataIndex: 'name',
    width: 120,
    ellipsis: true,
    align: 'center',
  },
  {
    title: '经度',
    dataIndex: 'longitude',
    width: 90,
    align: 'center',
  },
  {
    title: '纬度',
    dataIndex: 'latitude',
    width: 90,
    align: 'center',
  },
  {
    title: '地址',
    dataIndex: 'address',
    width: 180,
    ellipsis: true,
    align: 'center',
  },
  {
    title: '处理后图片',
    dataIndex: 'processedUrl',
    width: 120,
    align: 'center',
  },
  {
    title: '处理结果',
    dataIndex: 'processedResult',
    width: 250,
    ellipsis: true,
    align: 'center',
  },
  {
    title: '用户ID',
    dataIndex: 'userId',
    width: 100,
    align: 'center',
  },
  {
    title: '审核状态',
    dataIndex: 'reviewStatus',
    width: 110,
    align: 'center',
  },
  {
    title: '审核信息',
    dataIndex: 'reviewMessage',
    width: 150,
    ellipsis: true,
    align: 'center',
  },
  {
    title: '审核人 ID',
    dataIndex: 'reviewerId',
    width: 120,
    align: 'center',
  },
  {
    title: '审核时间',
    dataIndex: 'reviewTime',
    width: 160,
    ellipsis: true,
    align: 'center',
  },
  {
    title: '创建时间',
    dataIndex: 'createTime',
    width: 120,
    ellipsis: true,
    align: 'center',
  },
  {
    title: '更新时间',
    dataIndex: 'updateTime',
    width: 120,
    ellipsis: true,
    align: 'center',
  },
  {
    title: '图片体积',
    dataIndex: 'picSize',
    width: 110,
    align: 'center',
  },
  {
    title: '是否删除',
    dataIndex: 'isDelete',
    width: 110,
    align: 'center',
  },
  {
    title: '操作',
    key: 'action',
    width: 140,
    fixed: 'right',
    align: 'center',
  },
]

const dataList = ref<API.Picture[]>([])
const total = ref(0)

const searchParams = reactive<API.PictureQueryRequest>({
  current: 1,
  pageSize: 10,
  sortField: 'createTime',
  sortOrder: 'ascend',
})

const fetchData = async () => {
  const res = await listPictureByPageUsingPost({
    ...searchParams,
  })
  if (res.data.code === 0 && res.data.data) {
    dataList.value = res.data.data.records ?? []
    total.value = res.data.data.total ?? 0
  } else {
    message.error('获取数据失败，' + res.data.message)
  }
}

onMounted(() => {
  fetchData()
})

const pagination = computed(() => {
  return {
    current: searchParams.current,
    pageSize: searchParams.pageSize,
    total: total.value,
    showSizeChanger: true,
    showTotal: (total) => `共 ${total} 条`,
    showQuickJumper: true,
    locale: {
      items_per_page: '条/页',
      jump_to: '跳至',
      page: '页',
    },
  }
})

const doTableChange = (page: any) => {
  searchParams.current = page.current
  searchParams.pageSize = page.pageSize
  fetchData()
}

const doSearch = () => {
  searchParams.current = 1
  fetchData()
}

const doDelete = async (id: number) => {
  if (!id) {
    return
  }

  Modal.confirm({
    title: '确认删除',
    content: '确定要删除这张图片吗？此操作不可恢复。',
    okText: '确认',
    cancelText: '取消',
    okType: 'danger',
    onOk: async () => {
      try {
        const res = await deletePictureUsingPost({ id })

        if (res.data.code === 0) {
          message.success('删除成功')
          fetchData()
        } else {
          message.error('删除失败：' + (res.data.message || '未知错误'))
        }
      } catch (error) {
        message.error('删除失败，请稍后重试')
        console.error('删除图片失败：', error)
      }
    },
  })
}

// 审核相关
const reviewModalVisible = ref(false)
const currentReviewId = ref<number | undefined>(undefined)
const reviewForm = reactive({
  reviewStatus: 1,
  reviewMessage: '符合',
})

const showReviewModal = (record: API.PictureVO) => {
  currentReviewId.value = record.id
  reviewForm.reviewStatus = 1
  reviewForm.reviewMessage = '符合'
  reviewModalVisible.value = true
}

const submitReview = async () => {
  if (!currentReviewId.value) {
    return
  }
  const res = await doPictureReviewUsingPost({
    id: currentReviewId.value,
    reviewStatus: reviewForm.reviewStatus,
    reviewMessage: reviewForm.reviewMessage,
  })
  if (res.data.code === 0) {
    message.success('审核成功')
    reviewModalVisible.value = false
    fetchData()
  } else {
    message.error('审核失败')
  }
}

const formatFileSize = (bytes?: number) => {
  if (!bytes) return '0 B'
  if (bytes < 1024) return bytes + ' B'
  if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(2) + ' KB'
  return (bytes / (1024 * 1024)).toFixed(2) + ' MB'
}

const parseProcessedResult = (result: string) => {
  try {
    const parsed = JSON.parse(result)
    if (Array.isArray(parsed) && parsed.length > 0) {
      return parsed.join('；')
    }
    return result
  } catch (e) {
    return result
  }
}
</script>

<style scoped>
#pictureManagePage {
  padding: 0;
}

:deep(.ant-table) {
  font-size: 16px;
}

:deep(.ant-table-thead th) {
  font-size: 16px;
  font-weight: 500;
  text-align: center;
}

:deep(.ant-table-tbody td) {
  font-size: 16px;
}

:deep(.ant-table-pagination) {
  font-size: 16px;
}

.ellipsis-text {
  display: inline-block;
  max-width: 100%;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  vertical-align: middle;
}
</style>
