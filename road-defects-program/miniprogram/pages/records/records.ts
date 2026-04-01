import { getMyPictureList, PictureVO, MyPictureQueryRequest } from '../../utils/api'
import { isLoggedIn } from '../../utils/auth'

Component({
  data: {
    pictureList: [] as PictureVO[],
    current: 1,
    pageSize: 10,
    total: 0,
    showDetail: false,
    currentPicture: null as PictureVO | null,
    isLoggedIn: false,
    currentFilter: 'all'
  },

  methods: {
    async loadPictureList() {
      if (!isLoggedIn()) {
        wx.showToast({
          title: '请先登录',
          icon: 'none'
        })
        return
      }

      try {
        const params: MyPictureQueryRequest = {
          current: this.data.current,
          pageSize: this.data.pageSize
        }

        if (this.data.currentFilter === 'pending') {
          params.reviewStatus = 0
        } else if (this.data.currentFilter === 'passed') {
          params.reviewStatus = 1
        }

        const result = await getMyPictureList(params)
        
        this.setData({
          pictureList: result.records || [],
          total: result.total
        })
      } catch (error) {
        console.error('获取记录列表失败:', error)
        wx.showToast({
          title: '获取记录失败',
          icon: 'none'
        })
      }
    },

    checkLoginStatus() {
      this.setData({
        isLoggedIn: isLoggedIn()
      })
    },

    onFilterChange(e: WechatMiniprogram.TouchEvent) {
      const { filter } = e.currentTarget.dataset
      if (filter !== this.data.currentFilter) {
        this.setData({
          currentFilter: filter,
          current: 1
        })
        this.loadPictureList()
      }
    },

    onCellTap(e: WechatMiniprogram.TouchEvent) {
      const { index } = e.currentTarget.dataset
      const picture = this.data.pictureList[index]
      if (picture) {
        this.setData({
          showDetail: true,
          currentPicture: picture
        })
      }
    },

    closeDetail() {
      this.setData({
        showDetail: false,
        currentPicture: null
      })
    },

    previewImage() {
      if (this.data.currentPicture?.url) {
        wx.previewImage({
          urls: [this.data.currentPicture.url],
          current: this.data.currentPicture.url
        })
      }
    },

    previewProcessedImage() {
      if (this.data.currentPicture?.processedUrl) {
        wx.previewImage({
          urls: [this.data.currentPicture.processedUrl],
          current: this.data.currentPicture.processedUrl
        })
      }
    }
  },

  pageLifetimes: {
    show() {
      const tabBar = this.getTabBar()
      if (tabBar) {
        tabBar.setData({
          active: 'pages/records/records'
        })
      }
      this.checkLoginStatus()
      this.loadPictureList()
    }
  }
})
