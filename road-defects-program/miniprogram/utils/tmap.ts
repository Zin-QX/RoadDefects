const app = getApp<IAppOption>()

export interface TMapResult {
  status: string
  [key: string]: any
}

export const requestTMapApi = (apiPath: string, params: Record<string, any> = {}): Promise<TMapResult> => {
  return new Promise((resolve, reject) => {
    wx.request({
      url: app.globalData.tmapWebUrl + apiPath,
      data: {
        ...params,
        tk: app.globalData.tmapKey
      },
      success(res) {
        if (res.statusCode === 200) {
          resolve(res.data as TMapResult)
        } else {
          reject(res)
        }
      },
      fail(err) {
        reject(err)
      }
    })
  })
}

export const transformCoord = (lng: number, lat: number, fromCoord: string = 'WGS84', toCoord: string = 'GCJ02'): [number, number] => {
  const PI = Math.PI
  const a = 6378245.0
  const ee = 0.00669342162296594323

  const transformLat = (x: number, y: number): number => {
    let ret = -100.0 + 2.0 * x + 3.0 * y + 0.2 * y * y + 0.1 * x * y + 0.2 * Math.sqrt(Math.abs(x))
    ret += (20.0 * Math.sin(6.0 * x * PI) + 20.0 * Math.sin(2.0 * x * PI)) * 2.0 / 3.0
    ret += (20.0 * Math.sin(y * PI) + 40.0 * Math.sin(y / 3.0 * PI)) * 2.0 / 3.0
    ret += (160.0 * Math.sin(y / 12.0 * PI) + 320 * Math.sin(y * PI / 30.0)) * 2.0 / 3.0
    return ret
  }

  const transformLng = (x: number, y: number): number => {
    let ret = 300.0 + x + 2.0 * y + 0.1 * x * x + 0.1 * x * y + 0.1 * Math.sqrt(Math.abs(x))
    ret += (20.0 * Math.sin(6.0 * x * PI) + 20.0 * Math.sin(2.0 * x * PI)) * 2.0 / 3.0
    ret += (20.0 * Math.sin(x * PI) + 40.0 * Math.sin(x / 3.0 * PI)) * 2.0 / 3.0
    ret += (150.0 * Math.sin(x / 12.0 * PI) + 300.0 * Math.sin(x / 30.0 * PI)) * 2.0 / 3.0
    return ret
  }

  const outOfChina = (lng: number, lat: number): boolean => {
    return !(lng > 73.66 && lng < 135.05 && lat > 3.86 && lat < 53.55)
  }

  if (fromCoord === toCoord) {
    return [lng, lat]
  }

  if (outOfChina(lng, lat)) {
    return [lng, lat]
  }

  let dLat = transformLat(lng - 105.0, lat - 35.0)
  let dLng = transformLng(lng - 105.0, lat - 35.0)
  const radLat = lat / 180.0 * PI
  let magic = Math.sin(radLat)
  magic = 1 - ee * magic * magic
  const sqrtMagic = Math.sqrt(magic)
  dLat = (dLat * 180.0) / ((a * (1 - ee)) / (magic * sqrtMagic) * PI)
  dLng = (dLng * 180.0) / (a / sqrtMagic * Math.cos(radLat) * PI)
  const mgLat = lat + dLat
  const mgLng = lng + dLng

  if (fromCoord === 'WGS84' && toCoord === 'GCJ02') {
    return [mgLng, mgLat]
  } else if (fromCoord === 'GCJ02' && toCoord === 'WGS84') {
    return [lng * 2 - mgLng, lat * 2 - mgLat]
  }

  return [lng, lat]
}
