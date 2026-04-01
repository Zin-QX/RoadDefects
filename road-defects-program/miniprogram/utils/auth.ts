const TOKEN_KEY = 'token'
const USER_INFO_KEY = 'userInfo'

export interface UserInfo {
  id: string
  openid: string
  nickName?: string
  avatarUrl?: string
}

export function getToken(): string | null {
  return wx.getStorageSync(TOKEN_KEY) || null
}

export function setToken(token: string): void {
  wx.setStorageSync(TOKEN_KEY, token)
}

export function removeToken(): void {
  wx.removeStorageSync(TOKEN_KEY)
}

export function getUserInfo(): UserInfo | null {
  return wx.getStorageSync(USER_INFO_KEY) || null
}

export function setUserInfo(userInfo: UserInfo): void {
  wx.setStorageSync(USER_INFO_KEY, userInfo)
}

export function removeUserInfo(): void {
  wx.removeStorageSync(USER_INFO_KEY)
}

export function isLoggedIn(): boolean {
  return !!getToken()
}

export function logout(): void {
  removeToken()
  removeUserInfo()
}
