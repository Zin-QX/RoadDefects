import { getToken } from './auth'

const BASE_URL = 'http://localhost:8122/api'

interface RequestOptions {
  url: string
  method?: 'GET' | 'POST' | 'PUT' | 'DELETE'
  data?: any
  header?: Record<string, string>
}

interface ApiResponse<T = any> {
  code: number
  data: T
  message: string
}

export function request<T = any>(options: RequestOptions): Promise<T> {
  const token = getToken()
  const header: Record<string, string> = {
    'Content-Type': 'application/json',
    ...options.header
  }
  
  if (token) {
    header['Authorization'] = `Bearer ${token}`
  }

  return new Promise((resolve, reject) => {
    wx.request({
      url: BASE_URL + options.url,
      method: options.method || 'GET',
      data: options.data,
      header,
      success: (res) => {
        const data = res.data as ApiResponse<T>
        if (data.code === 0 || data.code === 200) {
          resolve(data.data)
        } else {
          reject(new Error(data.message || '请求失败'))
        }
      },
      fail: (err) => {
        reject(new Error(err.errMsg || '网络请求失败'))
      }
    })
  })
}

export function getWxCode(): Promise<string> {
  return new Promise((resolve, reject) => {
    wx.login({
      success: (res) => {
        if (res.code) {
          resolve(res.code)
        } else {
          reject(new Error('获取登录凭证失败'))
        }
      },
      fail: () => {
        reject(new Error('wx.login调用失败'))
      }
    })
  })
}
