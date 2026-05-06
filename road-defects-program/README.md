# 慧眼清途 - 道路缺陷检测小程序

## 项目简介

慧眼清途是一个基于微信小程序的道路缺陷检测应用，用户可以上传道路图片进行缺陷检测，查看检测记录和审核状态。

## 技术栈

- 微信小程序原生开发（TypeScript）
- TDesign 微信小程序组件库
- 天地图 API（地图服务）

## 环境要求

- 微信开发者工具（最新稳定版）
- Node.js 16.x 或更高版本
- npm 或 yarn

## 从 Git 拉取后的启动步骤

### 1. 克隆项目

```bash
git clone <项目地址>
cd road-defects-program
```

### 2. 安装依赖

```bash
# 进入 miniprogram 目录
cd miniprogram

# 安装 npm 依赖
npm install

# 返回项目根目录
cd ..
```

### 3. 构建 npm

打开微信开发者工具，执行以下操作：

1. 点击菜单栏 **工具** → **构建 npm**
2. 等待构建完成，会在 `miniprogram` 目录下生成 `miniprogram_npm` 文件夹

### 4. 配置后端接口

在 `miniprogram/app.ts` 中修改后端 API 地址：

```typescript
globalData: {
  baseUrl: 'http://localhost:8122/api',  // 修改为你的后端地址
  tmapKey: '你的天地图key'
}
```

### 5. 导入项目到微信开发者工具

1. 打开微信开发者工具
2. 点击 **导入项目**
3. 选择项目根目录
4. 填写 AppID（可在 project.config.json 中查看或修改）
5. 点击 **导入**

### 6. 开始开发

- 点击 **编译** 按钮预览小程序
- 点击 **预览** 生成二维码，在手机上体验

## 项目结构

```
road-defects-program/
├── miniprogram/                 # 小程序源码目录
│   ├── pages/                   # 页面目录
│   │   ├── index/               # 首页（地图展示）
│   │   ├── records/             # 记录页
│   │   ├── profile/             # 个人中心
│   │   └── logs/                # 日志页
│   ├── custom-tab-bar/          # 自定义 TabBar
│   ├── utils/                   # 工具函数
│   │   ├── api.ts               # API 接口封装
│   │   ├── auth.ts              # 认证相关
│   │   ├── request.ts           # 请求封装
│   │   └── tmap.ts              # 天地图 API
│   ├── image/                   # 图片资源
│   ├── app.ts                   # 小程序入口
│   ├── app.json                 # 小程序配置
│   ├── app.wxss                 # 全局样式
│   └── package.json             # npm 配置
├── typings/                     # TypeScript 类型定义
├── project.config.json          # 项目配置
├── tsconfig.json                # TypeScript 配置
└── package.json                 # 根目录 npm 配置
```

## 功能说明

### 首页
- 展示天地图地图
- 显示道路缺陷标记点
- 点击标记查看详情
- 上传道路图片进行检测

### 记录页
- 查看上传记录列表
- 筛选：全部/待审核/已通过
- 查看记录详情

### 个人中心
- 微信授权登录
- 查看用户信息
- 退出登录

## 后端接口

项目需要配合后端服务使用，主要接口：

| 接口 | 方法 | 说明 |
|------|------|------|
| /picture/list/page/vo | POST | 获取图片列表（地图标记） |
| /picture/my/list/page/vo | POST | 获取我的图片列表 |
| /picture/upload | POST | 上传图片 |
| /user/login | POST | 用户登录 |

## 注意事项

1. **AppID**：需要在微信开发者工具中配置正确的 AppID
2. **域名配置**：正式环境需要在微信公众平台配置合法域名
3. **天地图 Key**：需要在 `app.ts` 中配置有效的天地图 API Key
4. **后端服务**：确保后端服务已启动并可访问

## 常见问题

### Q: 构建 npm 失败？
A: 确保在 `miniprogram` 目录下执行 `npm install`，然后在微信开发者工具中点击"构建 npm"。

### Q: 地图无法显示？
A: 检查天地图 Key 是否正确配置，以及网络是否正常。

### Q: 接口请求失败？
A: 检查后端服务是否启动，`baseUrl` 配置是否正确。开发环境可在微信开发者工具中勾选"不校验合法域名"。

## 开发团队

慧眼清途开发团队
