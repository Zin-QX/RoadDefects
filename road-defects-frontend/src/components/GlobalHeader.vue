<template>
  <a-row :wrap="false">
    <a-col flex="200px">
      <RouterLink to="/">
        <div class="title-bar">
          <img class="logo" src="../assets/logo.png" alt="logo" />
          <div class="title">粤智呼</div>
        </div>
      </RouterLink>
    </a-col>
    <a-col flex="auto">
      <a-menu v-model:selectedKeys="current" mode="horizontal" :items="items" />
    </a-col>
    <a-col flex="200px">
      <div class="user-login-status">
        <BellOutlined style="margin-right: 8px; font-size: 24px; vertical-align: middle;" />
        <div v-if="loginUserStore.loginUser.id">
          <a-dropdown>
            <a class="ant-dropdown-link user-info">
              <img
                v-if="loginUserStore.loginUser.userAvatar"
                :src="loginUserStore.loginUser.userAvatar"
                class="user-avatar"
                alt="avatar"
              />
              <a-avatar v-else class="user-avatar">{{ loginUserStore.loginUser.userName?.charAt(0) ?? '无' }}</a-avatar>
              <span class="user-name">{{ loginUserStore.loginUser.userName ?? '无名' }}</span>
            </a>
            <template #overlay>
              <a-menu>
                <a-menu-item style="font-size: 18px; font-weight: 400;" @click="handleLogout">
                  <LogoutOutlined style="margin-right: 8px;" />
                  退出登录
                </a-menu-item>
              </a-menu>
            </template>
          </a-dropdown>
        </div>
        <div v-else>
          <a-button type="primary" href="/user/login">登录</a-button>
        </div>
      </div>

    </a-col>
  </a-row>
</template>

<script lang="ts" setup>
import { h, ref } from 'vue';
import { MenuProps } from 'ant-design-vue';
import { BellOutlined, LogoutOutlined } from '@ant-design/icons-vue';
import { useLoginUserStore } from '@/stores/useLoginUserStore';
import { useRouter } from 'vue-router';
import { message } from 'ant-design-vue';
import { userLogoutUsingPost } from '@/api/userController.ts';

const router = useRouter();
const current = ref<string[]>(['mail']);
const items = ref<MenuProps['items']>([
  {
    key: 'mail',
    title: 'Navigation One',
  }
]);

const loginUserStore = useLoginUserStore();

const handleLogout = async () => {
  const res = await userLogoutUsingPost();
  if (res.data.code === 0) {
    loginUserStore.setLoginUser({ userName: '未登录' });
    message.success('退出登录成功');
    router.push('/user/login');
  } else {
    message.error('退出登录失败，' + res.data.message);
  }
};
</script>

<style scoped>
.title-bar {
  display: flex;
  align-items: center;
}

.title {
  color: #1a1a1a;
  font-size: 24px;
  font-weight: 600;
  margin-left: 16px;
}

.logo {
  height: 48px;
}

.user-login-status {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 8px;
  height: 100%;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
}

.user-avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  object-fit: cover;
}

.user-name {
  font-size: 14px;
  font-weight: 500;
}
</style>
