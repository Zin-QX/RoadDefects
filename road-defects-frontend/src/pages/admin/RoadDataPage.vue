<script setup lang="ts">
import { ref, onMounted } from 'vue'
import * as echarts from 'echarts'
import { pcaTextArr } from 'element-china-area-data'
import { getDefectStatisticsUsingPost } from '@/api/pictureController'

let roseChartInstance: echarts.ECharts | null = null
let barChartInstance: echarts.ECharts | null = null
const selectedAddress = ref<string[]>([])
const defectData = ref<API.DefectStatisticsVO | null>(null)

const initRoseChart = () => {
  const chartDom = document.getElementById('rose-chart')
  if (chartDom) {
    roseChartInstance = echarts.init(chartDom)

    const option = {
      title: {
        text: '缺陷类型分布',
        left: 'center',
        top: 10,
        textStyle: {
          fontSize: 16,
          fontWeight: 'bold',
        },
      },
      tooltip: {
        trigger: 'item',
        formatter: '{b}: {d}%',
      },
      legend: {
        top: 'bottom',
      },
      toolbox: {
        show: true,
        feature: {
          mark: { show: true },
          dataView: { show: true, readOnly: false },
          restore: { show: true },
          saveAsImage: { show: true },
        },
      },
      series: [
        {
          name: '缺陷分布',
          type: 'pie',
          radius: [50, 250],
          center: ['50%', '50%'],
          roseType: 'area',
          itemStyle: {
            borderRadius: 8,
          },
          label: {
            show: true,
            formatter: '{b}\n{d}%',
          },
          data: [],
        },
      ],
    }

    roseChartInstance.setOption(option)
  }
}

const initBarChart = () => {
  const chartDom = document.getElementById('bar-chart')
  if (chartDom) {
    barChartInstance = echarts.init(chartDom)

    const option = {
      title: {
        text: '缺陷类型数量统计',
        left: 'center',
        top: 10,
        textStyle: {
          fontSize: 16,
          fontWeight: 'bold',
        },
      },
      tooltip: {
        trigger: 'axis',
        axisPointer: {
          type: 'shadow',
        },
      },
      grid: {
        left: '3%',
        right: '4%',
        bottom: '15%',
        top: '15%',
        containLabel: true,
      },
      xAxis: {
        type: 'category',
        data: [],
        axisLabel: {
          interval: 0,
          rotate: 30,
        },
      },
      yAxis: {
        type: 'value',
        name: '数量',
      },
      series: [
        {
          name: '数量',
          type: 'bar',
          data: [],
          itemStyle: {
            color: '#5470c6',
          },
          label: {
            show: true,
            position: 'top',
          },
        },
      ],
    }

    barChartInstance.setOption(option)
  }
}

const handleConfirm = async () => {
  const [province, city, district] = selectedAddress.value || []

  const params: API.DefectStatisticsRequest = {
    province: province || undefined,
    city: city || undefined,
    district: district || undefined,
  }

  console.log('选择的地址：', params)

  try {
    const res = await getDefectStatisticsUsingPost(params)

    if (res.data.code === 0 && res.data.data) {
      defectData.value = res.data.data
      updateCharts(res.data.data)
    }
  } catch (error) {
    console.error('获取缺陷统计数据失败：', error)
  }
}

const updateCharts = (data: API.DefectStatisticsVO) => {
  if (!data.defects || data.defects.length === 0) {
    return
  }

  const defects = data.defects

  const defectTypes = defects.map((item) => item.defectType || '')
  const counts = defects.map((item) => item.count || 0)

  const roseData = defects.map((item) => ({
    name: item.defectType || '',
    value: item.percentage || 0,
  }))

  if (barChartInstance) {
    barChartInstance.setOption({
      xAxis: {
        data: defectTypes,
      },
      series: [
        {
          data: counts,
        },
      ],
    })
  }

  if (roseChartInstance) {
    roseChartInstance.setOption({
      series: [
        {
          data: roseData,
        },
      ],
    })
  }
}

onMounted(async () => {
  initRoseChart()
  initBarChart()

  await handleConfirm()

  setTimeout(() => {
    roseChartInstance?.resize()
    barChartInstance?.resize()
  }, 0)

  window.addEventListener('resize', () => {
    roseChartInstance?.resize()
    barChartInstance?.resize()
  })
})
</script>

<template>
  <div class="road-data-page">
    <div class="page-header">
      <h2>已收录道路数据展示</h2>
    </div>
    <div class="charts-container">
      <div class="operation-bar">
        <div class="address-selector">
          <a-cascader
            v-model:value="selectedAddress"
            :options="pcaTextArr"
            placeholder="请选择省 / 市/区"
            style="width: 300px;"
            change-on-select
          />
        </div>
        <a-button type="primary" @click="handleConfirm" style="margin-left: 12px;">
          确认
        </a-button>
      </div>
      <div class="charts-row">
        <div class="chart-wrapper">
          <div id="bar-chart" class="chart"></div>
        </div>
        <div class="chart-wrapper">
          <div id="rose-chart" class="chart"></div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.road-data-page {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.page-header {
  margin-bottom: 24px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.page-header h2 {
  margin: 0;
  font-size: 24px;
  font-weight: 600;
  color: #262626;
}

.address-selector {
  flex-shrink: 0;
}

.charts-container {
  display: flex;
  flex-direction: column;
  gap: 16px;
  flex: 1;
  min-height: 0;
}

.operation-bar {
  display: flex;
  justify-content: flex-start;
  align-items: center;
  padding: 0 8px;
}

.address-selector {
  flex-shrink: 0;
}

.chart-wrapper {
  flex: 1;
  background: #fff;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.03);
  display: flex;
  flex-direction: column;
  min-height: 0;
}

.charts-row {
  display: flex;
  gap: 24px;
  flex: 1;
  min-height: 0;
}

.chart {
  width: 100%;
  flex: 1;
  min-height: 400px;
}
</style>
