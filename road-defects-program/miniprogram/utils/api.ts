import { request } from './request'

export interface PictureQueryRequest {
  id?: number
  userId?: number
  address?: string
  startTime?: string
  endTime?: string
  reviewStatus?: number
  reviewerId?: number
}

export interface MyPictureQueryRequest {
  current: number
  pageSize: number
  reviewStatus?: number
}

export interface PictureVO {
  id: string
  url: string
  name: string
  longitude: number
  latitude: number
  address: string
  processedUrl: string
  processedResult: string[]
  picSize: string
  userId: string
  createTime: string
  updateTime: string
  reviewStatus?: number
  reviewMessage?: string
  reviewerId?: number
  reviewTime?: string
}

export interface ApiResponse<T> {
  code: number
  data: T
  message: string
}

export interface PageResponse<T> {
  records: T[]
  total: number
  size: number
  current: number
  pages: number
}

export const getPictureList = (params: PictureQueryRequest = {}): Promise<PageResponse<PictureVO>> => {
  return request<PageResponse<PictureVO>>({
    url: '/picture/list/page/vo',
    method: 'POST',
    data: params
  })
}

export const getMyPictureList = (params: MyPictureQueryRequest): Promise<PageResponse<PictureVO>> => {
  return request<PageResponse<PictureVO>>({
    url: '/picture/my/list/page/vo',
    method: 'POST',
    data: params
  })
}

export interface PictureUploadRequest {
  id?: number
  longitude: number
  latitude: number
  address: string
}

export const uploadPicture = (
  filePath: string,
  uploadData: PictureUploadRequest
): Promise<PictureVO> => {
  return new Promise((resolve, reject) => {
    const app = getApp<IAppOption>()
    const token = wx.getStorageSync('token') as string
    
    wx.uploadFile({
      url: app.globalData.baseUrl + '/api/picture/upload',
      filePath: filePath,
      name: 'file',
      formData: {
        id: uploadData.id || '',
        longitude: uploadData.longitude,
        latitude: uploadData.latitude,
        address: uploadData.address
      },
      header: {
        'Authorization': token ? `Bearer ${token}` : ''
      },
      success: (res) => {
        if (res.statusCode === 200) {
          try {
            const response = JSON.parse(res.data) as ApiResponse<PictureVO>
            if (response.code === 0 || response.code === 200) {
              resolve(response.data)
            } else {
              reject(new Error(response.message || '上传失败'))
            }
          } catch (e) {
            reject(new Error('解析响应失败'))
          }
        } else {
          reject(new Error(`上传失败: ${res.statusCode}`))
        }
      },
      fail: (err) => {
        reject(new Error(err.errMsg || '上传请求失败'))
      }
    })
  })
}
