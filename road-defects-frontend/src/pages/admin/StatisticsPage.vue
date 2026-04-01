<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import * as echarts from 'echarts'
import zhCN from 'ant-design-vue/es/date-picker/locale/zh_CN'
import dayjs from 'dayjs'
import 'dayjs/locale/zh-cn'
import { ArrowUpOutlined, ArrowDownOutlined, UploadOutlined, PictureOutlined, UserOutlined, ClockCircleOutlined } from '@ant-design/icons-vue'
import { getStatisticsUsingGet, getUploadTrendUsingPost, getApprovedTrendUsingPost } from '@/api/pictureController'

dayjs.locale('zh-cn')

// 统计数据
const statistics = ref<API.StatisticsVO>({
  todayUploads: 0,
  todayUploadsChange: 0,
  totalUploads: 0,
  totalUploadsChange: 0,
  activeUsers: 0,
  activeUsersChange: 0,
  pendingReview: 0,
  pendingReviewChange: 0,
})

// 加载统计数据
const loadStatistics = async () => {
  try {
    const res = await getStatisticsUsingGet()
    if (res.data.code === 0 && res.data.data) {
      statistics.value = res.data.data
    }
  } catch (error) {
    console.error('加载统计数据失败:', error)
  }
}

// 日期范围和日期选择器
const lineDateRange = ref<string>('month')
const lineDates = ref<[any, any] | null>(null)
const barDateRange = ref<string>('month')
const barDates = ref<[any, any] | null>(null)

// 计算日期范围
const getDateRange = (rangeType: string) => {
  const now = dayjs()
  let startDate: dayjs.Dayjs
  let endDate: dayjs.Dayjs = now

  switch (rangeType) {
    case 'month':
      // 本月：从本月 1 号到今天
      startDate = now.startOf('month')
      break
    case '30days':
      // 近 30 天
      startDate = now.subtract(29, 'day')
      break
    case '15days':
      // 近 15 天
      startDate = now.subtract(14, 'day')
      break
    case '7days':
      // 近 7 天
      startDate = now.subtract(6, 'day')
      break
    default:
      startDate = now.startOf('month')
  }

  return [startDate, endDate]
}

// 生成日期序列
const generateDateSequence = (start: dayjs.Dayjs, end: dayjs.Dayjs): string[] => {
  const dates: string[] = []
  let current = start.clone()

  while (current.isSame(end, 'day') || current.isBefore(end, 'day')) {
    dates.push(current.format('YYYY-M-D'))
    current = current.add(1, 'day')
  }

  return dates
}

// 生成模拟数据
const generateMockData = (dateSequence: string[]): number[] => {
  return dateSequence.map(() => Math.floor(Math.random() * 200) + 50)
}

// 图表实例
let chartInstance: echarts.ECharts | null = null
let barChartInstance: echarts.ECharts | null = null

// 更新折线图
const updateLineChart = async () => {
  if (!chartInstance) return

  const [startDate, endDate] = getDateRange(lineDateRange.value)

  // 更新日期选择器显示
  lineDates.value = [startDate, endDate]

  try {
    const res = await getUploadTrendUsingPost({
      dateRange: lineDateRange.value,
    })

    if (res.data.code === 0 && res.data.data) {
      const { dates, uploadCounts } = res.data.data
      chartInstance.setOption({
        xAxis: {
          data: dates || [],
        },
        series: [
          {
            data: uploadCounts || [],
          },
        ],
      })
    }
  } catch (error) {
    console.error('加载上传趋势失败:', error)
  }
}

// 更新柱状图
const updateBarChart = async () => {
  if (!barChartInstance) return

  const [startDate, endDate] = getDateRange(barDateRange.value)

  // 更新日期选择器显示
  barDates.value = [startDate, endDate]

  try {
    const res = await getApprovedTrendUsingPost({
      dateRange: barDateRange.value,
    })

    if (res.data.code === 0 && res.data.data) {
      const { dates, approvedCounts } = res.data.data
      barChartInstance.setOption({
        xAxis: {
          data: dates || [],
        },
        series: [
          {
            data: approvedCounts || [],
            label: {
              show: true,
              position: 'top',
              formatter: '{c}',
              fontSize: 12,
              color: '#666',
            },
          },
        ],
      })
    }
  } catch (error) {
    console.error('加载审核通过趋势失败:', error)
  }
}

// 初始化折线图
const initChart = () => {
  const chartDom = document.getElementById('statistics-chart')
  if (chartDom) {
    chartInstance = echarts.init(chartDom)

    const option = {
      tooltip: {
        trigger: 'axis',
      },
      xAxis: {
        type: 'category',
        data: [],
        axisLabel: {
          rotate: 45,
          interval: 'auto',
        },
      },
      yAxis: {
        type: 'value',
      },
      series: [
        {
          data: [],
          type: 'line',
          smooth: true,
        },
      ],
      grid: {
        left: '3%',
        right: '4%',
        bottom: '3%',
        top: '10px',
        containLabel: true,
      },
    }

    chartInstance.setOption(option)
  }
}

// 初始化柱状图
const initBarChart = () => {
  const barChartDom = document.getElementById('bar-chart')
  if (barChartDom) {
    barChartInstance = echarts.init(barChartDom)

    const barOption = {
      tooltip: {
        trigger: 'axis',
      },
      xAxis: {
        type: 'category',
        data: [],
        axisLabel: {
          rotate: 45,
          interval: 'auto',
        },
      },
      yAxis: {
        type: 'value',
      },
      series: [
        {
          data: [],
          type: 'bar',
          barWidth: '40%',
          label: {
            show: true,
            position: 'top',
            formatter: '{c}',
            fontSize: 12,
            color: '#666',
          },
        },
      ],
      grid: {
        left: '3%',
        right: '4%',
        bottom: '3%',
        top: '20px',
        containLabel: true,
      },
    }

    barChartInstance.setOption(barOption)
  }
}

// 监听日期范围变化
watch(lineDateRange, () => {
  updateLineChart()
})

watch(barDateRange, () => {
  updateBarChart()
})

// 监听日期选择器变化
watch(lineDates, async (newDates) => {
  if (newDates && newDates.length === 2) {
    const [startDate, endDate] = newDates

    try {
      const res = await getUploadTrendUsingPost({
        startDate: startDate.format('YYYY-MM-DD'),
        endDate: endDate.format('YYYY-MM-DD'),
      })

      if (res.data.code === 0 && res.data.data) {
        const { dates, uploadCounts } = res.data.data
        chartInstance?.setOption({
          xAxis: {
            data: dates || [],
          },
          series: [
            {
              data: uploadCounts || [],
            },
          ],
        })
      }
    } catch (error) {
      console.error('加载上传趋势失败:', error)
    }
  }
})

watch(barDates, async (newDates) => {
  if (newDates && newDates.length === 2) {
    const [startDate, endDate] = newDates

    try {
      const res = await getApprovedTrendUsingPost({
        startDate: startDate.format('YYYY-MM-DD'),
        endDate: endDate.format('YYYY-MM-DD'),
      })

      if (res.data.code === 0 && res.data.data) {
        const { dates, approvedCounts } = res.data.data
        barChartInstance?.setOption({
          xAxis: {
            data: dates || [],
          },
          series: [
            {
              data: approvedCounts || [],
              label: {
                show: true,
                position: 'top',
                formatter: '{c}',
                fontSize: 12,
                color: '#666',
              },
            },
          ],
        })
      }
    } catch (error) {
      console.error('加载审核通过趋势失败:', error)
    }
  }
})

onMounted(() => {
  initChart()
  initBarChart()

  // 加载统计数据
  loadStatistics()

  // 初始化图表数据
  updateLineChart()
  updateBarChart()

  // 初始调整大小，确保图表占满容器
  setTimeout(() => {
    chartInstance?.resize()
    barChartInstance?.resize()
  }, 0)

  // 响应式调整
  window.addEventListener('resize', () => {
    chartInstance?.resize()
    barChartInstance?.resize()
  })
})
</script>

<template>
  <div class="statistics-page">
    <!-- 统计卡片组件 -->
    <div class="statistics-cards">
      <a-row :gutter="[16, 16]">
        <a-col :xs="24" :sm="12" :md="12" :lg="6">
          <div class="stat-card">
            <div class="stat-card-content">
              <div class="stat-card-title">今日上传数</div>
              <div class="stat-card-value-row">
                <div class="stat-card-icon">
                  <upload-outlined />
                </div>
                <div class="stat-card-value">{{ statistics.todayUploads }}</div>
                <div class="stat-card-change" :class="statistics.todayUploadsChange >= 0 ? 'up' : 'down'">
                  <template v-if="statistics.todayUploadsChange >= 0">
                    <arrow-up-outlined />
                  </template>
                  <template v-else>
                    <arrow-down-outlined />
                  </template>
                  {{ Math.abs(statistics.todayUploadsChange) }}%
                </div>
              </div>
            </div>
          </div>
        </a-col>

        <a-col :xs="24" :sm="12" :md="12" :lg="6">
          <div class="stat-card">
            <div class="stat-card-content">
              <div class="stat-card-title">总上传数</div>
              <div class="stat-card-value-row">
                <div class="stat-card-icon">
                  <picture-outlined />
                </div>
                <div class="stat-card-value">{{ statistics.totalUploads.toLocaleString() }}</div>
                <div class="stat-card-change" :class="statistics.totalUploadsChange >= 0 ? 'up' : 'down'">
                  <template v-if="statistics.totalUploadsChange >= 0">
                    <arrow-up-outlined />
                  </template>
                  <template v-else>
                    <arrow-down-outlined />
                  </template>
                  {{ Math.abs(statistics.totalUploadsChange) }}%
                </div>
              </div>
            </div>
          </div>
        </a-col>

        <a-col :xs="24" :sm="12" :md="12" :lg="6">
          <div class="stat-card">
            <div class="stat-card-content">
              <div class="stat-card-title">活跃用户</div>
              <div class="stat-card-value-row">
                <div class="stat-card-icon">
                  <user-outlined />
                </div>
                <div class="stat-card-value">{{ statistics.activeUsers }}</div>
                <div class="stat-card-change" :class="statistics.activeUsersChange >= 0 ? 'up' : 'down'">
                  <template v-if="statistics.activeUsersChange >= 0">
                    <arrow-up-outlined />
                  </template>
                  <template v-else>
                    <arrow-down-outlined />
                  </template>
                  {{ Math.abs(statistics.activeUsersChange) }}%
                </div>
              </div>
            </div>
          </div>
        </a-col>

        <a-col :xs="24" :sm="12" :md="12" :lg="6">
          <div class="stat-card">
            <div class="stat-card-content">
              <div class="stat-card-title">待审核</div>
              <div class="stat-card-value-row">
                <div class="stat-card-icon">
                  <clock-circle-outlined />
                </div>
                <div class="stat-card-value">{{ statistics.pendingReview }}</div>
                <div class="stat-card-change" :class="statistics.pendingReviewChange >= 0 ? 'up' : 'down'">
                  <template v-if="statistics.pendingReviewChange >= 0">
                    <arrow-up-outlined />
                  </template>
                  <template v-else>
                    <arrow-down-outlined />
                  </template>
                  {{ Math.abs(statistics.pendingReviewChange) }}%
                </div>
              </div>
            </div>
          </div>
        </a-col>
      </a-row>
    </div>

    <!-- 图表容器 -->
    <div class="charts-wrapper">
      <a-row :gutter="[16, 16]">
        <!-- 折线图组件 -->
        <a-col :xs="24" :lg="12">
          <div class="chart-container">
            <!-- 顶部操作栏 -->
            <div class="header">
              <div class="title">上传趋势</div>
              <div class="controls">
                <a-radio-group v-model:value="lineDateRange" size="small">
                  <a-radio-button value="month">本月</a-radio-button>
                  <a-radio-button value="30days">近 30 天</a-radio-button>
                  <a-radio-button value="15days">近 15 天</a-radio-button>
                  <a-radio-button value="7days">近 7 天</a-radio-button>
                </a-radio-group> <a-range-picker v-model:value="lineDates" size="small" :locale="zhCN" style="width: 220px; margin-left: 16px;" />

              </div>
            </div>
            <div id="statistics-chart" class="statistics-chart"></div>
          </div>
        </a-col>

        <!-- 柱状图组件 -->
        <a-col :xs="24" :lg="12">
          <div class="chart-container">
            <!-- 顶部操作栏 -->
            <div class="header">
              <div class="title">审核通过数量</div>
              <div class="controls">
                <a-radio-group v-model:value="barDateRange" size="small">
                  <a-radio-button value="month">本月</a-radio-button>
                  <a-radio-button value="30days">近 30 天</a-radio-button>
                  <a-radio-button value="15days">近 15 天</a-radio-button>
                  <a-radio-button value="7days">近 7 天</a-radio-button>
                </a-radio-group>
                <a-range-picker v-model:value="barDates" size="small" :locale="zhCN" style="width: 220px; margin-left: 16px;" />
              </div>
            </div>
            <div id="bar-chart" class="statistics-chart"></div>
          </div>
        </a-col>
      </a-row>
    </div>
  </div>
</template>

<style scoped>
.statistics-page {
  display: flex;
  flex-direction: column;
  height: 100%;
}

/* 统计卡片容器 - 定位在左上角 */
.statistics-cards {
  flex-shrink: 0;
  margin-bottom: 24px;
}

/* 图表容器 wrapper */
.charts-wrapper {
  width: 100%;
  flex: 1;
  min-height: 0;
  display: flex;
  flex-direction: column;
}

.charts-wrapper .ant-row {
  flex: 1;
  min-height: 0;
}

.charts-wrapper .ant-col {
  display: flex;
  flex-direction: column;
  min-height: 0;
}

/* 图表容器 */
.chart-container {
  background: #ffffff;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  border: 1px solid #f0f0f0;
  height: 100%;
  display: flex;
  flex-direction: column;
}

/* 顶部操作栏 */
.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-shrink: 0;
  margin-bottom: 20px;
  padding-bottom: 16px;
  border-bottom: 1px solid #f0f0f0;
}

.title {
  font-size: 16px;
  font-weight: 600;
  color: #262626;
  flex-shrink: 0;
}

.controls {
  display: flex;
  align-items: center;
  gap: 12px;
  flex: 1;
  justify-content: flex-end;
}

.statistics-chart {
  width: 100%;
  flex: 1;
  min-height: 0;
}

/* 可复用统计卡片组件 */
.stat-card {
  padding: 20px;
  background: #ffffff;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  transition: all 0.3s ease;
  border: 1px solid #f0f0f0;
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.12);
}

/* 卡片内容 */
.stat-card-content {
  display: flex;
  flex-direction: column;
  gap: 8px;
  align-items: center;
}

.stat-card-value-row {
  display: flex;
  align-items: center;
  gap: 12px;
}

.stat-card-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 40px;
  border-radius: 8px;
  font-size: 28px;
  flex-shrink: 0;
}

.stat-card-title {
  font-size: 16px;
  color: #8c8c8c;
  font-weight: 500;
  text-align: center;
}

.stat-card-value {
  font-size: 32px;
  font-weight: 600;
  color: #262626;
  line-height: 1;
}

.stat-card-change {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 18px;
  font-weight: 600;
}

.stat-card-change.up {
  color: #52c41a;
}

.stat-card-change.down {
  color: #ff4d4f;
}

/* 响应式适配 */
@media (max-width: 768px) {
  .stat-card {
    padding: 16px;
  }

  .stat-card-value {
    font-size: 24px;
  }

  .stat-card-change {
    font-size: 13px;
  }
}

@media (max-width: 576px) {
  .statistics-page {
    padding: 16px;
  }

  .statistics-cards {
    margin-bottom: 16px;
  }
}
</style>
