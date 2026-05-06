<script setup lang="ts">
import { onMounted, onBeforeUnmount } from 'vue'
import 'leaflet/dist/leaflet.css'
import L from 'leaflet'
import { listPictureVoByPageUsingPost } from '@/api/pictureController'

let map: L.Map | null = null

// 加载图片数据并在地图上标记
const loadPictures = async () => {
  try {
    const res = await listPictureVoByPageUsingPost({
      current: 1,
      pageSize: 100, // 获取前 100 条数据
    })

    if (res.data.code === 0 && res.data.data?.records) {
      const pictures = res.data.data.records

      pictures.forEach((picture) => {
        if (picture.latitude && picture.longitude) {
          // 坐标转换：GCJ-02 转 WGS-84（天地图使用 GCJ-02，Leaflet 使用 WGS-84）
          const wgs84Coord = gcj02towgs84(picture.latitude, picture.longitude)

          // 创建标记点，自定义图标锚点使其对准精确位置
          const marker = L.marker([wgs84Coord.lat, wgs84Coord.lng], {
            icon: L.icon({
              iconUrl: 'https://unpkg.com/leaflet@1.9.4/dist/images/marker-icon.png',
              iconRetinaUrl: 'https://unpkg.com/leaflet@1.9.4/dist/images/marker-icon-2x.png',
              shadowUrl: 'https://unpkg.com/leaflet@1.9.4/dist/images/marker-shadow.png',
              iconSize: [25, 41],
              iconAnchor: [12, 41], // 图标锚点，尖端对准精确位置
              popupAnchor: [1, -34], // 弹窗打开位置
              shadowAnchor: [4, 62],
              shadowSize: [41, 41]
            })
          }).addTo(map!)

          // 创建复杂内容的弹窗
          let popupContent = `
            <div style="min-width: 200px;">
              <h3 style="margin: 0 0 8px 0; font-size: 16px; color: #1a1a1a;">${picture.name || '道路缺陷'}</h3>
          `

          // 添加图片
          if (picture.processedUrl) {
            popupContent += `<img src="${picture.processedUrl}" alt="处理后图片" style="width: 100%; height: 120px; object-fit: cover; border-radius: 4px; margin-bottom: 8px;" />`
          }

          // 添加详细信息
          popupContent += `
            <div style="font-size: 13px; line-height: 1.6;">
              <p style="margin: 4px 0;"><strong>地址:</strong> ${picture.address || '未知'}</p>
              <p style="margin: 4px 0;"><strong>经度:</strong> ${picture.longitude}</p>
              <p style="margin: 4px 0;"><strong>纬度:</strong> ${picture.latitude}</p>
              <p style="margin: 4px 0;"><strong>大小:</strong> ${formatFileSize(picture.picSize)}</p>
              <p style="margin: 4px 0;"><strong>上传时间:</strong> ${formatDate(picture.createTime)}</p>
              <p style="margin: 4px 0;"><strong>处理结果:</strong> ${parseProcessedResult(picture.processedResult)}</p>
          `

          popupContent += `</div></div>`

          // 绑定弹窗
          marker.bindPopup(popupContent)
        }
      })

      // 如果有数据，将地图中心设置到第一个点
      // if (pictures.length > 0 && pictures[0].latitude && pictures[0].longitude) {
      //   map?.setView([pictures[0].latitude, pictures[0].longitude], 12)
      // }
    }
  } catch (error) {
    console.error('加载图片数据失败:', error)
  }
}

// 格式化文件大小
const formatFileSize = (bytes?: number): string => {
  if (!bytes) return '0 B'
  if (bytes < 1024) return bytes + ' B'
  if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(2) + ' KB'
  return (bytes / (1024 * 1024)).toFixed(2) + ' MB'
}

// 格式化日期
const formatDate = (dateStr?: string): string => {
  if (!dateStr) return '未知'
  try {
    const date = new Date(dateStr)
    return date.toLocaleString('zh-CN', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit',
    })
  } catch {
    return dateStr
  }
}

// 解析处理结果
const parseProcessedResult = (result: string | string[] | undefined): string => {
  if (!result) return '未处理'
  
  if (typeof result === 'string') {
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
  
  if (Array.isArray(result) && result.length > 0) {
    return result.join('；')
  }
  
  return '未处理'
}

// GCJ-02 转 WGS-84 坐标转换函数
const gcj02towgs84 = (lat: number, lng: number) => {
  const a = 6378245.0
  const ee = 0.00669342162296594323

  const transformLat = (x: number, y: number) => {
    let ret = -100.0 + 2.0 * x + 3.0 * y + 0.2 * y * y + 0.1 * x * y + 0.2 * Math.sqrt(Math.abs(x))
    ret += (20.0 * Math.sin(6.0 * x * Math.PI) + 20.0 * Math.sin(2.0 * x * Math.PI)) * 2.0 / 3.0
    ret += (20.0 * Math.sin(y * Math.PI) + 40.0 * Math.sin(y / 3.0 * Math.PI)) * 2.0 / 3.0
    ret += (160.0 * Math.sin(y / 12.0 * Math.PI) + 320 * Math.sin(y * Math.PI / 30.0)) * 2.0 / 3.0
    return ret
  }

  const transformLon = (x: number, y: number) => {
    let ret = 300.0 + x + 2.0 * y + 0.1 * x * x + 0.1 * x * y + 0.1 * Math.sqrt(Math.abs(x))
    ret += (20.0 * Math.sin(6.0 * x * Math.PI) + 20.0 * Math.sin(2.0 * x * Math.PI)) * 2.0 / 3.0
    ret += (20.0 * Math.sin(x * Math.PI) + 40.0 * Math.sin(x / 3.0 * Math.PI)) * 2.0 / 3.0
    ret += (150.0 * Math.sin(x / 12.0 * Math.PI) + 300.0 * Math.sin(x / 30.0 * Math.PI)) * 2.0 / 3.0
    return ret
  }

  let dLat = transformLat(lng - 105.0, lat - 35.0)
  let dLon = transformLon(lng - 105.0, lat - 35.0)
  const radLat = lat / 180.0 * Math.PI
  let magic = Math.sin(radLat)
  magic = 1 - ee * magic * magic
  const sqrtMagic = Math.sqrt(magic)
  dLat = (dLat * 180.0) / ((a * (1 - ee)) / (magic * sqrtMagic) * Math.PI)
  dLon = (dLon * 180.0) / (a / sqrtMagic * Math.cos(radLat) * Math.PI)

  const mgLat = lat + dLat
  const mgLon = lng + dLon

  return {
    lat: lat * 2 - mgLat,
    lng: lng * 2 - mgLon
  }
}

onMounted(() => {
  map = L.map('map', {
    center: [23.27, 112.67], // 广州中心点 [纬度，经度]
    zoom: 12, // 当前展示的层级（数字越大越放大）
    maxZoom: 18, // 最大层级
    minZoom: 1, // 最小层级
    attributionControl: false, // 不展示版权信息
  })

  // 添加天地图矢量底图图层
  L.tileLayer(
    'http://t0.tianditu.gov.cn/vec_w/wmts?SERVICE=WMTS&REQUEST=GetTile&VERSION=1.0.0&LAYER=vec&STYLE=default&TILEMATRIXSET=w&FORMAT=tiles&TILEMATRIX={z}&TILEROW={y}&TILECOL={x}&tk=2b22d2bb6b011c247bb85e13f5ccfe0a'
  ).addTo(map)

  // 添加天地图矢量注记图层
  L.tileLayer(
    'http://t0.tianditu.gov.cn/cva_w/wmts?SERVICE=WMTS&REQUEST=GetTile&VERSION=1.0.0&LAYER=cva&STYLE=default&TILEMATRIXSET=w&FORMAT=tiles&TILEMATRIX={z}&TILEROW={y}&TILECOL={x}&tk=2b22d2bb6b011c247bb85e13f5ccfe0a'
  ).addTo(map)

  // 确保地图在容器大小变化时正确渲染
  setTimeout(() => {
    map?.invalidateSize()
  }, 100)

  // 加载图片数据
  loadPictures()
})

onBeforeUnmount(() => {
  if (map) {
    map.remove()
    map = null
  }
})
</script>

<template>
  <div class="home-page">
    <div id="map" class="map-container"></div>
  </div>
</template>

<style scoped>
.home-page {
  height: 100%;
  width: 100%;
}

.map-container {
  height: 100%;
  width: 100%;
  border-radius: 12px;
}
</style>
