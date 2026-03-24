// @ts-ignore
/* eslint-disable */
import request from '@/request'

/** login POST /api/wxuser/login */
export async function loginUsingPost(
  body: API.AuthorizationLoginDTO,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponseAuthorizationVO_>('/api/wxuser/login', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}
