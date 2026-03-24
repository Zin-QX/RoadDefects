<template>
  <div id="userRegisterPage">
    <div class="background-deco"></div>

    <div class="register-card">
      <div class="header">
        <div class="logo-container">
          <img class="logo" src="@/assets/logo.png" alt="logo" />
          <h2 class="title">慧眼清途</h2>
        </div>
        <div class="desc">智能道路缺陷检测系统</div>
      </div>

      <a-form :model="formState" name="basic" layout="vertical" autocomplete="off" @finish="handleSubmit">
        <a-form-item
          label="账号"
          name="userAccount"
          :rules="[{ required: true, message: '请输入账号' }]"
        >
          <a-input v-model:value="formState.userAccount" placeholder="请输入账号" size="large" />
        </a-form-item>

        <a-form-item
          label="密码"
          name="userPassword"
          :rules="[
            { required: true, message: '请输入密码' },
            { min: 8, message: '密码长度不能小于 8 位' },
          ]"
        >
          <a-input-password v-model:value="formState.userPassword" placeholder="请输入密码" size="large" />
        </a-form-item>

        <a-form-item
          label="确认密码"
          name="checkPassword"
          :rules="[
            { required: true, message: '请输入确认密码' },
            { min: 8, message: '确认密码长度不能小于 8 位' },
          ]"
        >
          <a-input-password v-model:value="formState.checkPassword" placeholder="请输入确认密码" size="large" />
        </a-form-item>

        <div class="tips">
          <span>已有账号？</span>
          <RouterLink to="/user/login">立即登录</RouterLink>
        </div>

        <a-form-item>
          <a-button type="primary" html-type="submit" class="register-button" size="large" block>
            注册
          </a-button>
        </a-form-item>
      </a-form>

      <div class="footer">
        ©拿个奖有啥不队
      </div>
    </div>
  </div>
</template>
<script lang="ts" setup>
import { reactive } from 'vue'
import { userRegisterUsingPost } from '@/api/userController.ts'
import { useLoginUserStore } from '@/stores/useLoginUserStore.ts'
import { message } from 'ant-design-vue'
import router from '@/router' // 用于接受表单输入的值

// 用于接受表单输入的值
const formState = reactive<API.UserRegisterRequest>({
  userAccount: '',
  userPassword: '',
  checkPassword: '',
})

const loginUserStore = useLoginUserStore()

/**
 * 提交表单
 * @param values
 */
const handleSubmit = async (values: any) => {
  // 校验两次输入的密码是否一致
  if (values.userPassword !== values.checkPassword) {
    message.error('两次输入的密码不一致')
    return
  }
  const res = await userRegisterUsingPost(values)
  // 注册成功，跳转到登录页面
  if (res.data.code === 0 && res.data.data) {
    message.success('注册成功')
    router.push({
      path: '/user/login',
      replace: true,
    })
  } else {
    message.error('注册失败，' + res.data.message)
  }
}
</script>

<style scoped>
#userRegisterPage {
  height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background: #f0f2f5;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  position: relative;
  overflow: hidden;
}

.background-deco {
  position: absolute;
  width: 600px;
  height: 600px;
  background: radial-gradient(circle, rgba(24, 144, 255, 0.1) 0%, rgba(255, 255, 255, 0) 70%);
  top: -200px;
  right: -200px;
  z-index: 0;
}

.register-card {
  position: relative;
  z-index: 1;
  width: 100%;
  max-width: 500px;
  padding: 48px;
  background: rgba(255, 255, 255, 0.8);
  backdrop-filter: blur(10px);
  border-radius: 12px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.3);
}

.header {
  text-align: center;
  margin-bottom: 32px;
}

.logo-container {
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 16px;
}

.logo {
  height: 48px;
  margin-right: 12px;
}

.title {
  font-size: 28px;
  color: #1a1a1a;
  font-weight: 600;
  margin: 0;
}

.desc {
  color: #8c8c8c;
  font-size: 14px;
  margin-top: 8px;
}

.tips {
  margin-bottom: 24px;
  display: flex;
  justify-content: space-between;
  font-size: 14px;
}

.tips span {
  color: #8c8c8c;
}

.register-button {
  height: 44px;
  font-size: 16px;
  border-radius: 6px;
  font-weight: 500;
  box-shadow: 0 4px 12px rgba(24, 144, 255, 0.3);
}

.footer {
  margin-top: 32px;
  text-align: center;
  color: #bfbfbf;
  font-size: 12px;
}

:deep(.ant-form-item-label > label) {
  font-weight: 500;
  color: #434343;
}
</style>
