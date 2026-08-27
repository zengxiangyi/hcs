<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { ElMessage } from 'element-plus'
import MenuBar from './MenuBar.vue'
import { restoreCurrentUserRights } from './sys/permission'

defineOptions({ name: 'Web' })

const router = useRouter()
const route = useRoute()

/** 顶栏标题文本 */
const SYS_TITLE = '宝钢轧辊热处理工艺数智化系统'

/** 当前登录用户名；登录后由 loadUserInfo 填充真实值 */
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
    // await baseAPI.logout()
    localStorage.removeItem('token')
    localStorage.removeItem('rights')
    restoreCurrentUserRights()
    ElMessage.success('已退出登录')
    router.push('/login')
  } catch {
    // 接口异常不阻塞退出：本地清理凭证后仍可跳转登录页
  } finally {
    console.log('handleLogout')
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
  height: 56px;
  padding: 0 20px;
  color: #ffffff;
  /* 深蓝渐变 + 细高光，体现专业管理系统质感 */
  background: linear-gradient(90deg, #1e3c72 0%, #1e5aa8 55%, #1e90ff 100%);
  box-shadow: 0 2px 8px rgba(30, 92, 168, 0.25);
  position: relative;
  z-index: 20;
  flex-shrink: 0;
}

/* 顶栏底部细分割线，增强层次 */
.title::after {
  content: '';
  position: absolute;
  left: 0;
  right: 0;
  bottom: 0;
  height: 2px;
  background: linear-gradient(90deg, rgba(255, 255, 255, 0.35), rgba(255, 255, 255, 0.05));
  pointer-events: none;
}

.title .left {
  /* 靠左对齐 */
  display: flex;
  align-items: center;
  margin-left: 10px;
  font-size: 18px;
  font-weight: 600;
  letter-spacing: 1px;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.15);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.title .right {
  /* 靠右对齐 */
  display: flex;
  align-items: center;
  gap: 14px;
  margin-right: 10px;
  font-size: 13px;
  letter-spacing: 0.5px;
  color: rgba(255, 255, 255, 0.95);
  flex-shrink: 0;
}

.title .right-btn{
  margin-left: 0;
}

.layout {
  display: flex;
  width: 100%;
  height: calc(100vh - 56px);
  overflow: hidden;
  background-color: #f5f6fa;
}

.layout-left {
  flex: 0 0 15%;
  width: 15%;
  min-width: 200px; /* 固定 12% 宽度时用 min-width 防止窄屏下被挤压 */
  background-color: #ffffff;
  border-right: 1px solid #e4e7ed;
  overflow-y: auto;
  box-shadow: 1px 0 6px rgba(0, 0, 0, 0.04);
}

.layout-right {
  flex: 1;
  background-color: #f5f6fa;
  overflow: auto;
  padding: 16px;
  box-sizing: border-box;
}

/* 内容区白色卡片化，让子页面内容在浅灰背景上更突出 */
.layout-right > * {
  background-color: #ffffff;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  min-height: 100%;
  box-sizing: border-box;
}
</style>
