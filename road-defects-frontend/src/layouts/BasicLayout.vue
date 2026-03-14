<template>
  <div id="basic-layout">
    <a-layout style="min-height: 100vh">
      <!--顶部导航栏-->
      <!-- 添加 style 确保背景为白色，并设置 flex 布局以对齐内容 -->
      <a-layout-header class="header" style="background: #fff; padding: 0 50px; ">
        <GlobalHeader />
        <div class="logo" />
      </a-layout-header>

      <!--主内容区-->
      <a-layout-content>
        <Sidebar />
      </a-layout-content>
      <!--底部版权信息-->
      <a-layout-footer style="text-align: center; background: #fff; position: fixed; bottom: 0; left: 0; right: 0; z-index: 10;">
        @拿个奖有啥不队
      </a-layout-footer>
    </a-layout>
  </div>
</template>

<script lang="ts" setup>
import GlobalHeader from "@/components/GlobalHeader.vue";
import Sidebar from "@/components/Sidebar.vue";
</script>

<style scoped>
/* 核心修改：强制 Header 背景为白色，并添加轻微阴影以增加层次感（可选但推荐） */
.header {
  background: #fff !important;
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
  z-index: 10;
}

/* 修改 Logo 样式：原背景是半透明白色，在白色 Header 下不可见，改为深色背景或边框 */
#components-layout-demo-top-side .logo {
  float: left;
  width: 120px;
  height: 31px;
  margin: 16px 24px 16px 0;
  /* 改为深灰色背景，确保在白色头部可见 */
  background: rgba(0, 0, 0, 0.2);
  /* 如果这里有 logo 图片，请确保图片是深色版本或带有边框 */
}

.ant-row-rtl #components-layout-demo-top-side .logo {
  float: right;
  margin: 16px 0 16px 24px;
}

.site-layout-background {
  background: #fff;
}

/* 深度选择器：如果 GlobalHeader 组件内部文字默认是白色，强制改为深色 */
:deep(.header) {
  color: rgba(0, 0, 0, 0.85);
}
:deep(.header a) {
  color: rgba(0, 0, 0, 0.85);
}

/* 页脚字体大小 */
:deep(.ant-layout-footer) {
  font-size: 16px;
  font-weight: 500;
}
</style>
