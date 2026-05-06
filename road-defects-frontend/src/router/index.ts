import { createRouter, createWebHistory } from 'vue-router'
import UserLoginPage from '@/pages/user/UserLoginPage.vue'
import UserRegisterPage from '@/pages/user/UserRegisterPage.vue'
import UserManagePage from '@/pages/admin/UserManagePage.vue'
import PictureManagePage from '@/pages/admin/PictureManagePage.vue'
import StatisticsPage from '@/pages/admin/StatisticsPage.vue'
import RoadDataPage from '@/pages/admin/RoadDataPage.vue'
import HomePage from '@/pages/HomePage.vue'


const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
  {
    path: '/',
    name: '主页',
    component: HomePage,
  },
  {
    path: '/admin/statistics',
    name: '控制台',
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
    path: '/roadData',
    name: '道路数据',
    component: RoadDataPage,
  },

  {
      path: '/about',
      name: 'about',
      component: () => import('../views/AboutView.vue'),
    },
  ],
})

export default router
