<template>
  <div id="basic-layout">
    <a-layout style="min-height: 100vh">
      <!--顶部导航栏-->
      <a-layout-header class="header" style="background: #fff; padding: 0 50px; ">
        <GlobalHeader />
        <div class="logo" />
      </a-layout-header>

      <!--主内容区-->
      <a-layout-content style="padding: 64px 0 36px 0;">
        <Sidebar />
      </a-layout-content>
      <!--底部版权信息-->
      <a-layout-footer class="footer" >
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
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
  z-index: 10;
  background: #fff;
  padding: 0 50px;
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  width: 100%;
}

/*  Logo 样式：原背景是半透明白色，在白色 Header 下不可见，改为深色背景或边框 */
#components-layout-demo-top-side .logo {
  float: right;
  width: 120px;
  height: 31px;
  margin: 16px 0 16px 24px;
  /* 改为深灰色背景，确保在白色头部可见 */
  background: rgba(0, 0, 0, 0.2);
  /* 如果这里有 logo 图片，请确保图片是深色版本或带有边框 */
}

.footer{
  text-align: center;
  background: #fff;
  padding: 24px 50px;
  font-size: 12px;
  height: 36px;
  line-height: 8px;
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  width: 100%;
}

</style>
