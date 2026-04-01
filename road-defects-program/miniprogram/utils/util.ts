export const formatTime = (date: Date) => {
  const year = date.getFullYear()
  const month = date.getMonth() + 1
  const day = date.getDate()
  const hour = date.getHours()
  const minute = date.getMinutes()
  const second = date.getSeconds()

  return (
    [year, month, day].map(formatNumber).join('/') +
    ' ' +
    [hour, minute, second].map(formatNumber).join(':')
  )
}

const formatNumber = (n: number) => {
  const s = n.toString()
  return s[1] ? s : '0' + s
}

export const formatDateTime = (dateStr: string | undefined | null): string => {
  if (!dateStr) return '-'
  
  try {
    const date = new Date(dateStr)
    
    if (isNaN(date.getTime())) return dateStr
    
    const year = date.getFullYear()
    const month = formatNumber(date.getMonth() + 1)
    const day = formatNumber(date.getDate())
    const hour = formatNumber(date.getHours())
    const minute = formatNumber(date.getMinutes())
    const second = formatNumber(date.getSeconds())
    
    return `${year}-${month}-${day} ${hour}:${minute}:${second}`
  } catch {
    return dateStr
  }
}
