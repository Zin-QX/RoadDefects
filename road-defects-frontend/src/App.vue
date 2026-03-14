<template>
  <div id="app">
    <BasicLayout v-if="shouldShowLayout" />
    <RouterView v-else />
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import BasicLayout from '@/layouts/BasicLayout.vue'
import { useLoginUserStore } from '@/stores/useLoginUserStore.ts'

const route = useRoute()
const loginUserStore = useLoginUserStore()
loginUserStore.fetchLoginUser()

const shouldShowLayout = computed(() => {
  const noLayoutRoutes = ['/user/login', '/user/register']
  return !noLayoutRoutes.includes(route.path)
})
</script>
