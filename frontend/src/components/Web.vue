<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { ElMessage } from 'element-plus'
import MenuBar from './MenuBar.vue'
import { baseAPI } from '../api/base';

defineOptions({ name: 'Web' })

const router = useRouter()
const route = useRoute()

/** 顶栏标题文案（中立化，不含具体技术栈版本号，便于长期复用） */
const SYS_TITLE = '宝钢轧辊热处理工艺管理系统'

/** 当前登录用户名；登录后由 getUserInfo 填充真实值 */
const username = ref('')

/** 加载当前登录用户信息，展示真实用户名；失败时保留空串占位 */
async function loadUserInfo() {
  try {
    // 通过localStorage获取用户信息
    const user = localStorage.getItem('user')
    if(user){
      const userInfo = JSON.parse(user)
      username.value = userInfo.username
    }
  } catch {
    // 路由守卫已校验 token；此处异常仅影响用户名展示，不阻塞页面
  }
}
onMounted(() => {
  loadUserInfo()
  // 路由守卫拦截无权限页面后回跳 Web 并携带 noAuth，这里给出提示
  if (route.query.noAuth) {
    ElMessage.warning('无权访问该页面，请联系管理员授权')
  }
})

async function handleLogout() {
  try {
    await baseAPI.logout()
  } catch {
    // 接口异常不阻塞退出：本地清理凭证后仍可跳转登录页
  } finally {
    localStorage.removeItem('token')
    ElMessage.success('已退出登录')
    router.push('/login')
  }
}
</script>

<template>
  <div class="title">
    <div class="left">{{ SYS_TITLE }}</div>
    <div class="right">
      <div>当前用户：{{username}}</div>
      <div class="right-btn">
        <el-button type="danger" @click="handleLogout">退出</el-button>
      </div>
    </div>
  </div>
  <div class="layout">
    <aside class="layout-left">
      <!-- 左侧内容 -->
       <MenuBar />
    </aside>
    <main class="layout-right">
      <!-- 路由跳转后的新页面嵌入此处 -->
      <router-view />
    </main>
  </div>
</template>

<style scoped>
/* 说明：原 ::global(#app) 全局重置（width/max-width/margin/border-inline/text-align）已移除。
   这些属性均为块级元素的默认值，与全局样式 style.css 中的基础重置重复，属侵入性 no-op。
   若需对根节点做全局样式定制，请统一维护在全局样式文件 style.css，避免 scoped 组件泄漏全局样式。 */

.title {
  display: flex;
  justify-content: space-between;
  align-items: center;
  height: 50px;
  padding: 0 20px;
  color:#ffffff;
  background-color: #1E90FF;
}

.title .left {
  /* 靠左对齐 */
  display: flex;
  align-items: center;
  margin-left:10px;
}

.title .right {
  /* 靠右对齐 */
  display: flex;
  align-items: center;
  margin-right:10px;
}
.title .right-btn{
  margin-left:10px;
}

.layout {
  display: flex;
  width: 100%;
  height: calc(100vh - 50px);
  overflow: hidden;
}

.layout-left {
  flex: 0 0 15%;
  width: 15%;
  min-width: 150px; /* 固定 15% 宽度时用 min-width 防止窄屏下被挤压 */
  background-color: #EDEDED;
}

.layout-right {
  flex: 1;
  background-color: #ffffff;
  overflow: auto;
}
</style>
