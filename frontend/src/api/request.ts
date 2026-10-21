import axios, { type AxiosInstance, type AxiosResponse } from 'axios'
import { ElMessage, ElMessageBox } from 'element-plus'
import router from '@/router'

export interface ApiResult<T = any> {
  code: number
  msg: string
  data: T
  success: boolean
  timestamp: number
}

const service: AxiosInstance = axios.create({
  baseURL: '/api/v1',
  timeout: 15000
})

service.interceptors.request.use((config) => {
  const token = localStorage.getItem('accessToken')
  if (token) {
    config.headers['Authorization'] = 'Bearer ' + token
  }
  return config
})

service.interceptors.response.use(
  (resp: AxiosResponse<ApiResult>) => {
    const body = resp.data
    if (resp.status === 200 && body && typeof body.code === 'number') {
      if (body.code === 200) {
        return resp
      }
      if (body.code === 401) {
        localStorage.removeItem('accessToken')
        ElMessage.error('登录已失效，请重新登录')
        router.replace('/login')
        return Promise.reject(new Error(body.msg || '未认证'))
      }
      ElMessage.error(body.msg || '请求失败')
      return Promise.reject(new Error(body.msg || 'BizError'))
    }
    return resp
  },
  (error) => {
    const status = error.response?.status
    if (status === 401 || status === 403) {
      localStorage.removeItem('accessToken')
      ElMessage.error(status === 403 ? '无访问权限' : '登录已失效，请重新登录')
      router.replace('/login')
    } else {
      ElMessage.error(error.response?.data?.msg || error.message || '网络错误')
    }
    return Promise.reject(error)
  }
)

export async function get<T = any>(url: string, params?: any): Promise<T> {
  const resp = await service.get<ApiResult<T>>(url, { params })
  return resp.data.data
}
export async function post<T = any>(url: string, data?: any): Promise<T> {
  const resp = await service.post<ApiResult<T>>(url, data)
  return resp.data.data
}
export async function put<T = any>(url: string, data?: any): Promise<T> {
  const resp = await service.put<ApiResult<T>>(url, data)
  return resp.data.data
}
export async function del<T = any>(url: string): Promise<T> {
  const resp = await service.delete<ApiResult<T>>(url)
  return resp.data.data
}

export default service
