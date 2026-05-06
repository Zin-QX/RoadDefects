// app.ts
App<IAppOption>({
  globalData: {
    tmapWebUrl: 'https://api.tianditu.gov.cn/',
    tmapKey: '2b22d2bb6b011c247bb85e13f5ccfe0a',
    baseUrl: 'http://localhost:8122'
  },
  onLaunch() {
    // 展示本地存储能力
    const logs = wx.getStorageSync('logs') || []
    logs.unshift(Date.now())
    wx.setStorageSync('logs', logs)

    // 登录
    wx.login({
      success: res => {
        console.log(res.code)
        // 发送 res.code 到后台换取 openId, sessionKey, unionId
      },
    })
  },
})