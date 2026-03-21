import { createRouter, createWebHistory } from 'vue-router'
import UserLoginPage from '@/pages/user/UserLoginPage.vue'
import UserRegisterPage from '@/pages/user/UserRegisterPage.vue'
import UserManagePage from '@/pages/admin/UserManagePage.vue'
import PictureManagePage from '@/pages/admin/PictureManagePage.vue'
import HomePage from '@/pages/HomePage.vue'
import StatisticsPage from '@/pages/StatisticsPage.vue'


const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
  {
    path: '/',
    name: '主页',
    component: HomePage,
  },
  {
    path: '/statistics',
    name: '道路缺陷统计',
    component: StatisticsPage,
  },
  {
    path: '/user/login',
    name: '用户登录',
    component: UserLoginPage,
  },
  {
    path: '/user/register',
    name: '用户注册',
    component: UserRegisterPage,
  },
  {
    path: '/admin/userManage',
    name: '用户管理',
    component: UserManagePage,
  },
  {
    path: '/admin/pictureManage',
    name: '图片管理',
    component: PictureManagePage,
  },

  {
      path: '/about',
      name: 'about',
      component: () => import('../views/AboutView.vue'),
    },
  ],
})

export default router
