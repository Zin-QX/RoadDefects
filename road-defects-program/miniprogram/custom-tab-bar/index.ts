Component({
  data: {
    active: 'pages/index/index',
    list: [
      {
        pagePath: 'pages/index/index',
        text: '主页',
        icon: 'home'
      },
      {
        pagePath: 'pages/records/records',
        text: '记录',
        icon: 'file-copy'
      },
      {
        pagePath: 'pages/profile/profile',
        text: '个人',
        icon: 'user'
      }
    ]
  },
  methods: {
    onChange(e: any) {
      const pagePath = e.detail.value
      this.setData({
        active: pagePath
      })
      wx.switchTab({
        url: '/' + pagePath
      })
    },
    init() {
      const page = getCurrentPages().pop()
      if (page) {
        const route = page.route
        this.setData({
          active: route
        })
      }
    }
  }
})
