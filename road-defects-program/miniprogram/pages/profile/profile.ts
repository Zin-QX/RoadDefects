import { getToken, setToken, getUserInfo, setUserInfo, removeToken, removeUserInfo, isLoggedIn, UserInfo } from '../../utils/auth'
import { request, getWxCode } from '../../utils/request'

Component({
  data: {
    isLoggedIn: false,
    userInfo: null as UserInfo | null,
    loginLoading: false,
    agreed: false,
    tempAvatarUrl: '',
    tempNickName: ''
  },

  methods: {
    checkLoginStatus() {
      const loggedIn = isLoggedIn()
      const userInfo = getUserInfo()
      this.setData({
        isLoggedIn: loggedIn,
        userInfo: userInfo
      })
    },

    onChooseAvatar(e: any) {
      const { avatarUrl } = e.detail
      this.setData({
        tempAvatarUrl: avatarUrl
      })
    },

    onNickNameInput(e: any) {
      this.setData({
        tempNickName: e.detail.value
      })
    },

    onNickNameBlur(e: any) {
      this.setData({
        tempNickName: e.detail.value
      })
    },

    onAgreementChange(e: any) {
      const agreed = e.detail.value.includes('agree')
      this.setData({ agreed })
    },

    async onLogin() {
      const { agreed, tempNickName, tempAvatarUrl } = this.data
      
      if (!agreed) {
        wx.showToast({
          title: '请先同意用户协议和隐私政策',
          icon: 'none'
        })
        return
      }

      if (!tempNickName) {
        wx.showToast({
          title: '请输入昵称',
          icon: 'none'
        })
        return
      }

      this.setData({ loginLoading: true })
      wx.showLoading({ title: '登录中...' })

      try {
        const code = await getWxCode()
        const result: any = await request({
          url: '/wxuser/login',
          method: 'POST',
          data: { code }
        })

        setToken(result.token)
        
        const userInfo: UserInfo = {
          id: result.id,
          openid: result.openid,
          nickName: tempNickName,
          avatarUrl: tempAvatarUrl
        }
        setUserInfo(userInfo)

        this.setData({
          isLoggedIn: true,
          userInfo: userInfo,
          loginLoading: false,
          agreed: false,
          tempAvatarUrl: '',
          tempNickName: ''
        })

        wx.hideLoading()
        wx.showToast({
          title: '登录成功',
          icon: 'success'
        })
      } catch (error: any) {
        this.setData({ loginLoading: false })
        wx.hideLoading()
        wx.showToast({
          title: error.message || '登录失败',
          icon: 'error'
        })
      }
    },

    onLogout() {
      wx.showModal({
        title: '提示',
        content: '确定要退出登录吗？',
        success: (res) => {
          if (res.confirm) {
            removeToken()
            removeUserInfo()
            this.setData({
              isLoggedIn: false,
              userInfo: null
            })
            wx.showToast({
              title: '已退出登录',
              icon: 'success'
            })
          }
        }
      })
    }
  },

  pageLifetimes: {
    show() {
      this.checkLoginStatus()
      const tabBar = this.getTabBar()
      if (tabBar) {
        tabBar.setData({
          active: 'pages/profile/profile'
        })
      }
    }
  }
})
