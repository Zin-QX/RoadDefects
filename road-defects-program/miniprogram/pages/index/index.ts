import { getPictureList, PictureVO, uploadPicture, PictureUploadRequest } from '../../utils/api'
import { isLoggedIn } from '../../utils/auth'

interface MarkerItem {
  id: number
  latitude: number
  longitude: number
  width: number
  height: number
  pictureData: PictureVO
}

Component({
  data: {
    latitude: 23.268662,
    longitude: 112.674618,
    scale: 12,
    markers: [] as MarkerItem[],
    mapContext: null as WechatMiniprogram.MapContext | null,
    pictureList: [] as PictureVO[],
    showDetail: false,
    currentPicture: null as PictureVO | null,
    showUploadPopup: false,
    uploadForm: {
      longitude: '',
      latitude: '',
      address: ''
    },
    tempFilePath: ''
  },

  methods: {
    onMapReady() {
      this.setData({
        mapContext: wx.createMapContext('tmap', this)
      })
    },

    onRegionChange(e: WechatMiniprogram.TouchEvent) {
      if (e.type === 'end' && e.causedBy === 'drag') {
        this.updateMapCenter()
      }
    },

    updateMapCenter() {
      const mapContext = this.data.mapContext
      if (mapContext) {
        mapContext.getCenterLocation({
          success: (res) => {
            this.setData({
              latitude: res.latitude,
              longitude: res.longitude
            })
          }
        })
      }
    },

    async onMarkerTap(e: WechatMiniprogram.TouchEvent) {
      const markerId = e.detail.markerId
      const marker = this.data.markers.find(m => m.id === markerId)
      if (marker) {
        this.setData({
          showDetail: true,
          currentPicture: marker.pictureData
        })
      }
    },

    closeDetail() {
      this.setData({
        showDetail: false,
        currentPicture: null
      })
    },

    async loadPictureList() {
      try {
        const result = await getPictureList({})
        const pictureList = result.records || []
        
        const markers: MarkerItem[] = pictureList.map((item, index) => ({
          id: index,
          latitude: item.latitude,
          longitude: item.longitude,
          width: 32,
          height: 32,
          pictureData: item
        }))
        
        this.setData({ 
          pictureList,
          markers
        })
      } catch (error) {
        console.error('获取图片列表失败:', error)
      }
    },

    onUpload() {
      if (!isLoggedIn()) {
        wx.showToast({
          title: '请先登录',
          icon: 'none'
        })
        return
      }
      
      this.setData({
        showUploadPopup: true,
        uploadForm: {
          longitude: '',
          latitude: '',
          address: ''
        },
        tempFilePath: ''
      })
    },

    closeUploadPopup() {
      this.setData({
        showUploadPopup: false,
        uploadForm: {
          longitude: '',
          latitude: '',
          address: ''
        },
        tempFilePath: ''
      })
    },

    onInputChange(e: WechatMiniprogram.Input) {
      const { field } = e.currentTarget.dataset
      const { value } = e.detail
      this.setData({
        [`uploadForm.${field}`]: value
      })
    },

    chooseImage() {
      wx.chooseMedia({
        count: 1,
        mediaType: ['image'],
        sourceType: ['album', 'camera'],
        success: (res) => {
          const tempFilePath = res.tempFiles[0].tempFilePath
          this.setData({
            tempFilePath
          })
        },
        fail: (err) => {
          console.error('选择图片失败:', err)
          wx.showToast({
            title: '选择图片失败',
            icon: 'none'
          })
        }
      })
    },

    async submitUpload() {
      const { uploadForm, tempFilePath } = this.data
      
      if (!tempFilePath) {
        wx.showToast({
          title: '请选择图片',
          icon: 'none'
        })
        return
      }
      
      if (!uploadForm.longitude || !uploadForm.latitude) {
        wx.showToast({
          title: '请输入经纬度',
          icon: 'none'
        })
        return
      }
      
      if (!uploadForm.address) {
        wx.showToast({
          title: '请输入地址',
          icon: 'none'
        })
        return
      }

      wx.showLoading({ title: '上传中...' })

      try {
        const uploadData: PictureUploadRequest = {
          longitude: parseFloat(uploadForm.longitude),
          latitude: parseFloat(uploadForm.latitude),
          address: uploadForm.address
        }

        await uploadPicture(tempFilePath, uploadData)
        
        wx.hideLoading()
        wx.showToast({
          title: '上传成功',
          icon: 'success'
        })
        
        this.closeUploadPopup()
        this.loadPictureList()
      } catch (error: any) {
        wx.hideLoading()
        wx.showToast({
          title: error.message || '上传失败',
          icon: 'none'
        })
      }
    },

    previewImage() {
      if (this.data.currentPicture) {
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
          active: 'pages/index/index'
        })
      }
      this.loadPictureList()
    }
  }
})
