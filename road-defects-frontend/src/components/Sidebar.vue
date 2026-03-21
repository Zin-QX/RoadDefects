<template>
  <a-layout class="dashboard-layout">

    <a-layout-sider
      width="200"
      class="sidebar-card"
    >
      <a-menu
        v-model:selectedKeys="selectedKeys2"
        mode="inline"
        class="custom-menu"
      >
        <a-menu-item key="home" @click="navigateTo('/')">
          <template #icon>
            <home-outlined />
          </template>
          主页
        </a-menu-item>
        <a-menu-item key="statistics" @click="navigateTo('/statistics')">
          <template #icon>
            <bar-chart-outlined />
          </template>
          道路缺陷统计
        </a-menu-item>
        <a-menu-item v-if="isAdmin" key="pictureManage" @click="navigateTo('/admin/pictureManage')">
          <template #icon>
            <picture-outlined />
          </template>
          图片管理
        </a-menu-item>
        <a-menu-item v-if="isAdmin" key="userManage" @click="navigateTo('/admin/userManage')">
          <template #icon>
            <user-outlined />
          </template>
          用户管理
        </a-menu-item>
        <a-menu-item key="about" @click="navigateTo('/about')">
          <template #icon>
            <notification-outlined />
          </template>
          关于我们
        </a-menu-item>
      </a-menu>
    </a-layout-sider>

    <a-layout class="content-layout">
      <a-layout-content class="main-content">
        <div class="content-card">
          <RouterView />
        </div>
      </a-layout-content>
    </a-layout>

  </a-layout>
</template>

<script lang="ts" setup>
import { ref, computed } from 'vue';
import { useRouter } from 'vue-router';
import { useLoginUserStore } from '@/stores/useLoginUserStore';
import { HomeOutlined, BarChartOutlined, NotificationOutlined, UserOutlined, PictureOutlined } from '@ant-design/icons-vue';

const router = useRouter();
const loginUserStore = useLoginUserStore();

const selectedKeys2 = ref<string[]>(['home']);

const isAdmin = computed(() => {
  return loginUserStore.loginUser?.userRole === 'admin';
});

const navigateTo = (path: string) => {
  router.push(path);
};
</script>

<style scoped>
/* 整体布局背景，使用浅色以突出内部的白色卡片 */
.dashboard-layout {
  padding: 6px;
  background: #f5f7fa;
  min-height: calc(100vh - 48px - 64px); /* 减去顶部导航栏和页脚的高度 */
  gap: 6px;
  padding-bottom: 6px; /* 额外的底部 padding 确保内容不被页脚遮挡 */
}

/* 侧边栏卡片化 */
.sidebar-card {
  background: #fff !important;
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.03);
  overflow: hidden; /* 确保内部元素不溢出圆角 */
}


/* 优化菜单样式，去除默认的右侧边框 */
.custom-menu {
  border-right: none !important;
  padding: 0 8px;
}

/* 调整菜单项的圆角和间距，使其看起来像按钮 */
:deep(.ant-menu-item) {
  border-radius: 8px;
  margin-bottom: 4px !important;
  margin-top: 4px !important;
  font-size: 16px;
  height: 50px;
  line-height: 50px;
  padding-left: 20px !important;
}

/* 增大图标尺寸 */
:deep(.ant-menu-item .anticon) {
  font-size: 20px;
  margin-right: 12px;
}

/* 增大侧边栏标题字体 */
.sidebar-title {
  font-size: 15px;
}

/* 右侧布局背景透明 */
.content-layout {
  background: transparent;
}

/* 右侧内容区卡片化 */
.main-content {
  display: flex;
  flex-direction: column;
}

.content-card {
  flex: 1;
  background: #fff;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.03);
  /* 确保内容过多时能在卡片内部滚动或正常撑开 */
  min-height: 100%;
}
</style>
