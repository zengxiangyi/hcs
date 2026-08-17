<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { baseAPI } from '../api/base'
import type { LoginResult } from '../api/base'

const router = useRouter()

const username = ref('')
const password = ref('')
const loading = ref(false)

async function handleLogin() {
  if (!username.value || !password.value) {
    ElMessage.warning('请输入用户名和密码')
    return
  }
  loading.value = true
  try {
    // baseAPI.login 返回 ApiResponse<LoginResult>，取 data 为业务数据
    const { data } = (await baseAPI.login({
      username: username.value,
      password: password.value,
    })) as { data: LoginResult }
    localStorage.setItem('token', data.token)
    ElMessage.success('登录成功')
    router.push('/web')
  } catch (err: any) {
    ElMessage.error(err?.message || '登录失败')
  } finally {
    loading.value = false
  }
}

// ===== 重置密码 =====
const resetDialogVisible = ref(false)
const resetStep = ref(1) // 1=验证身份，2=设置新密码
const resetCellphone = ref('')
const resetEmail = ref('')
const newPassword = ref('')
const confirmPassword = ref('')
const verifyLoading = ref(false)
const resetLoading = ref(false)

function openResetDialog() {
  resetStep.value = 1
  resetCellphone.value = ''
  resetEmail.value = ''
  newPassword.value = ''
  confirmPassword.value = ''
  resetDialogVisible.value = true
}

async function handleVerify() {
  if (!resetCellphone.value || !resetEmail.value) {
    ElMessage.warning('请输入手机号和邮箱地址')
    return
  }
  verifyLoading.value = true
  try {
    await baseAPI.verifyIdentity({
      cellphone: resetCellphone.value,
      email: resetEmail.value,
    })
    // 匹配成功 -> 进入设置新密码步骤
    resetStep.value = 2
  } catch (err: any) {
    // 不匹配 -> 弹出提示对话框
    ElMessageBox.alert(err?.message || '验证信息错误', '提示', { type: 'error' }).catch(() => {})
  } finally {
    verifyLoading.value = false
  }
}

async function handleReset() {
  if (!newPassword.value || !confirmPassword.value) {
    ElMessage.warning('请输入新密码和确认密码')
    return
  }
  if (newPassword.value !== confirmPassword.value) {
    ElMessage.warning('两次输入的密码不一致')
    return
  }
  resetLoading.value = true
  try {
    await baseAPI.resetPassword({
      cellphone: resetCellphone.value,
      email: resetEmail.value,
      newPassword: newPassword.value,
    })
    ElMessage.success('密码重置成功，请使用新密码登录')
    resetDialogVisible.value = false
  } catch (err: any) {
    ElMessage.error(err?.message || '密码重置失败')
  } finally {
    resetLoading.value = false
  }
}
</script>

<template>
  <div class="login-container">
    <div class="login-card">
      <form class="login-form" @submit.prevent="handleLogin">
        <h2 class="login-title">登录</h2>
        <input
          v-model="username"
          class="login-input"
          type="text"
          placeholder="用户名"
          autocomplete="username"
        />
        <input
          v-model="password"
          class="login-input"
          type="password"
          placeholder="密码"
          autocomplete="current-password"
        />
        <button class="login-button" type="submit" :disabled="loading">{{ loading ? '登录中...' : '确定' }}</button>
        <a class="forgot-link" href="#" @click.prevent="openResetDialog">忘记密码？</a>
      </form>
    </div>

    <!-- 重置密码对话框 -->
    <el-dialog
      v-model="resetDialogVisible"
      title="重置密码"
      width="360px"
      :close-on-click-modal="false"
      append-to-body
    >
      <!-- 第一步：验证身份 -->
      <div v-if="resetStep === 1" class="reset-form">
        <p class="reset-tip">请输入注册时填写的手机号和邮箱地址进行验证</p>
        <input v-model="resetCellphone" class="login-input" type="text" placeholder="手机号" />
        <input v-model="resetEmail" class="login-input" type="email" placeholder="邮箱地址" />
        <button class="login-button" :disabled="verifyLoading" @click="handleVerify">
          {{ verifyLoading ? '验证中...' : '验证' }}
        </button>
      </div>
      <!-- 第二步：设置新密码 -->
      <div v-else class="reset-form">
        <p class="reset-tip">验证通过，请输入新密码</p>
        <input v-model="newPassword" class="login-input" type="password" placeholder="新密码" />
        <input v-model="confirmPassword" class="login-input" type="password" placeholder="确认新密码" />
        <button class="login-button" :disabled="resetLoading" @click="handleReset">
          {{ resetLoading ? '重置中...' : '重置密码' }}
        </button>
      </div>
    </el-dialog>
  </div>
</template>

<style scoped>
:global(#app) {
  width: 100%;
  min-height: 100vh;
  border-inline: none;
}

.login-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: lightblue;
}

.login-card {
  background-color: lightgray;
  border-radius: 8px;
  padding: 40px 32px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  min-width: 300px;
}

.login-title {
  margin: 0 0 24px;
  text-align: center;
  color: #000;
}

.login-form {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.login-input {
  box-sizing: border-box;
  width: 100%;
  padding: 10px 12px;
  border: 1px solid #ccc;
  border-radius: 4px;
  background-color: #fff;
  color: #000;
  font-size: 14px;
}

.login-input:focus {
  outline: none;
  border-color: orange;
}

.login-button {
  box-sizing: border-box;
  width: 100%;
  padding: 10px 12px;
  border: none;
  border-radius: 4px;
  background-color: orange;
  color: #000;
  font-size: 14px;
  cursor: pointer;
}

.login-button:hover {
  background-color: darkorange;
}

.forgot-link {
  align-self: flex-end;
  color: #666;
  font-size: 13px;
  text-decoration: none;
}

.forgot-link:hover {
  color: orange;
}

.reset-form {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.reset-tip {
  margin: 0;
  color: #666;
  font-size: 13px;
}
</style>
