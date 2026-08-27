<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { baseAPI } from '../api/auth'
import { md5 } from '../utils/md5'
import { setCurrentUser } from './sys/permission'
defineOptions({ name: 'Login' })

const router = useRouter()

const username = ref('')
const password = ref('')
const loading = ref(false)

async function handleLogin() {
  const u = username.value.trim()
  const p = password.value.trim()
  if (!u || !p) {
    ElMessage.warning('请输入用户名和密码')
    return
  }
  loading.value = true
  try {
    // baseAPI.login 返回 Promise<ApiResponse<LoginResult>>，取 data 为业务数据
    const { data } = await baseAPI.login({
      username: u,
      password: md5(p),
    })
    
    localStorage.setItem('token', data.token)
    localStorage.setItem('user', JSON.stringify(data.user))
    // 登录后把后端下发的权限列表持久化到 localStorage，供路由守卫/菜单过滤恢复使用
    setCurrentUser(data.rights)
    ElMessage.success('登录成功')
    router.push('/web')
  } catch (err: any) {
    // 登录失败（用户名或密码错误等）-> 弹出后端返回的具体错误提示，停留在登录页
    ElMessageBox.alert(err?.message || '登录失败', '提示', {
      type: 'error',
      confirmButtonText: '确定',
    }).catch(() => {})
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

const EMAIL_REG = /^[^\s@]+@[^\s@]+\.[^\s@]+$/

async function handleVerify() {
  if (!resetCellphone.value.trim() || !resetEmail.value.trim()) {
    ElMessage.warning('请输入手机号和邮箱地址')
    return
  }
  if (!EMAIL_REG.test(resetEmail.value.trim())) {
    ElMessage.warning('请输入正确的邮箱地址')
    return
  }
  verifyLoading.value = true
  try {
    await baseAPI.verifyIdentity({
      cellphone: resetCellphone.value.trim(),
      email: resetEmail.value.trim(),
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
  const newPwd = newPassword.value
  const confirmPwd = confirmPassword.value
  if (!newPwd || !confirmPwd) {
    ElMessage.warning('请输入新密码和确认密码')
    return
  }
  if (newPwd.length < 6) {
    ElMessage.warning('新密码长度不能少于 6 位')
    return
  }
  if (newPwd !== confirmPwd) {
    ElMessage.warning('两次输入的密码不一致')
    return
  }
  resetLoading.value = true
  try {
    await baseAPI.resetPassword({
      cellphone: resetCellphone.value,
      email: resetEmail.value,
      newPassword: md5(newPwd),
    })
    ElMessage.success('密码重置成功，请使用新密码登录')
    resetDialogVisible.value = false
    // 关闭弹窗后清空表单字段，避免下次打开时残留数据
    resetStep.value = 1
    resetCellphone.value = ''
    resetEmail.value = ''
    newPassword.value = ''
    confirmPassword.value = ''
  } catch (err: any) {
    ElMessageBox.alert(err?.message || '密码重置失败', '提示', {
      type: 'error',
      confirmButtonText: '确定',
    }).catch(() => {})
  } finally {
    resetLoading.value = false
  }
}

// ===== 注册账号 =====
const registerDialogVisible = ref(false)
const registerLoading = ref(false)
const registerFormRef = ref<FormInstance>()

const registerForm = reactive({
  username: '',
  password: '',
  cellphone: '',
  email: '',
})

const PHONE_REG = /^1[3-9]\d{9}$/

const registerRules: FormRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
  cellphone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: PHONE_REG, message: '请输入正确的手机号', trigger: 'blur' },
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' },
  ],
}

function openRegisterDialog() {
  registerFormRef.value?.clearValidate()
  registerForm.username = ''
  registerForm.password = ''
  registerForm.cellphone = ''
  registerForm.email = ''
  registerDialogVisible.value = true
}

// 取消：清空表单并关闭对话框
function handleRegisterCancel() {
  registerFormRef.value?.resetFields()
  registerDialogVisible.value = false
}

// 注册：先校验，再提交
async function handleRegister() {
  if (!registerFormRef.value) return
  try {
    await registerFormRef.value.validate()
  } catch {
    return
  }
  registerLoading.value = true
  try {
    await baseAPI.register({
      username: registerForm.username.trim(),
      password: md5(registerForm.password),
      cellphone: registerForm.cellphone.trim(),
      email: registerForm.email.trim(),
    })
    ElMessage.success('注册成功，请登录')
    registerDialogVisible.value = false
    registerFormRef.value.resetFields()
  } catch (err: any) {
    ElMessageBox.alert(err?.message || '注册失败', '提示', {
      type: 'error',
      confirmButtonText: '确定',
    }).catch(() => {})
  } finally {
    registerLoading.value = false
  }
}
</script>

<template>
  <div class="login-container">
    <div class="login-card">
      <form class="login-form" @submit.prevent="handleLogin">
        <h2 class="login-title">登录</h2>
        <label for="login-username" class="sr-only">用户名</label>
        <input
          id="login-username"
          v-model="username"
          class="login-input"
          type="text"
          placeholder="用户名"
          autocomplete="username"
        />
        <label for="login-password" class="sr-only">密码</label>
        <input
          id="login-password"
          v-model="password"
          class="login-input"
          type="password"
          placeholder="密码"
          autocomplete="current-password"
        />
        <button class="login-button" type="submit" :disabled="loading">{{ loading ? '登录中...' : '确定' }}</button>
        <div class="link-row">
          <a class="forgot-link" href="#" @click.prevent="openRegisterDialog">注册账号</a>
          <a class="forgot-link" href="#" @click.prevent="openResetDialog">忘记密码？</a>
        </div>
      </form>
    </div>

    <!-- 重置密码对话框 -->
    <el-dialog
      v-model="resetDialogVisible"
      title="重置密码"
      width="460px"
      :close-on-click-modal="false"
      append-to-body
    >
      <!-- 第一步：验证身份 -->
      <div v-if="resetStep === 1" class="reset-form">
        <p class="reset-tip">请输入注册时填写的手机号和邮箱地址进行验证</p>
        <input
          v-model="resetCellphone"
          class="login-input"
          type="text"
          placeholder="手机号"
          autocomplete="tel"
          aria-label="手机号"
        />
        <input
          v-model="resetEmail"
          class="login-input"
          type="email"
          placeholder="邮箱地址"
          autocomplete="email"
          aria-label="邮箱地址"
        />
        <button class="login-button" type="button" :disabled="verifyLoading" @click="handleVerify">
          {{ verifyLoading ? '验证中...' : '验证' }}
        </button>
      </div>
      <!-- 第二步：设置新密码 -->
      <div v-else class="reset-form">
        <p class="reset-tip">验证通过，请输入新密码</p>
        <input
          v-model="newPassword"
          class="login-input"
          type="password"
          placeholder="新密码（不少于 6 位）"
          autocomplete="new-password"
          aria-label="新密码"
        />
        <input
          v-model="confirmPassword"
          class="login-input"
          type="password"
          placeholder="确认新密码"
          autocomplete="new-password"
          aria-label="确认新密码"
        />
        <button class="login-button" type="button" :disabled="resetLoading" @click="handleReset">
          {{ resetLoading ? '重置中...' : '重置密码' }}
        </button>
      </div>
    </el-dialog>

    <!-- 注册账号对话框 -->
    <el-dialog
      v-model="registerDialogVisible"
      title="注册新账号"
      width="560px"
      :close-on-click-modal="false"
      append-to-body
    >
      <el-form
        ref="registerFormRef"
        :model="registerForm"
        :rules="registerRules"
        label-width="100px"
      >
        <el-form-item label="用户名" prop="username">
          <el-input v-model="registerForm.username" placeholder="请输入用户名" autocomplete="username" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input
            v-model="registerForm.password"
            type="password"
            show-password
            placeholder="请输入密码"
            autocomplete="new-password"
          />
        </el-form-item>
        <el-form-item label="手机号" prop="cellphone">
          <el-input v-model="registerForm.cellphone" placeholder="请输入手机号" autocomplete="tel" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="registerForm.email" placeholder="请输入邮箱" autocomplete="email" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" :loading="registerLoading" @click="handleRegister">注册</el-button>
          <el-button @click="handleRegisterCancel">取消</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<style>
:global(#app) {
  width: 100%;
  min-height: 100vh;
  border-inline: none;
}

.dialog-footer {
  display: flex;
  justify-content: center;
  gap: 12px;
}

.login-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;
  background: #EEF4FF;
}

.login-card {
  width: 360px;
  max-width: 100%;
  background-color: #fff;
  border-radius: 16px;
  padding: 44px 36px 36px;
  box-shadow: 0 20px 50px rgba(0, 0, 0, 0.25);
  min-width: 300px;
}

.login-title {
  margin: 0 0 28px;
  text-align: center;
  font-size: 26px;
  font-weight: 700;
  letter-spacing: 2px;
  background: linear-gradient(90deg, #1e3c72, #2a5298);
  -webkit-background-clip: text;
  background-clip: text;
  color: transparent;
}

.login-form {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.sr-only {
  position: absolute;
  width: 1px;
  height: 1px;
  padding: 0;
  margin: -1px;
  overflow: hidden;
  clip: rect(0, 0, 0, 0);
  white-space: nowrap;
  border: 0;
}

.login-input {
  box-sizing: border-box;
  width: 100%;
  height: 42px;
  padding: 10px 14px;
  border: 1px solid #e0e4ec;
  border-radius: 8px;
  background-color: #f7f9fc;
  color: #1f2d3d;
  font-size: 14px;
  transition: border-color 0.2s, box-shadow 0.2s, background-color 0.2s;
}

.login-input::placeholder {
  color: #a8b0bd;
}

.login-input:focus {
  outline: none;
  background-color: #fff;
  border-color: #2a5298;
  box-shadow: 0 0 0 3px rgba(42, 82, 152, 0.15);
}

.login-input:hover {
  border-color: #b8c2d4;
}

.login-button {
  box-sizing: border-box;
  width: 100%;
  height: 44px;
  margin-top: 4px;
  padding: 10px 12px;
  border: none;
  border-radius: 8px;
  background: linear-gradient(90deg, #1e3c72, #2a5298);
  color: #fff;
  font-size: 15px;
  font-weight: 600;
  letter-spacing: 1px;
  cursor: pointer;
  transition: transform 0.15s, box-shadow 0.2s, opacity 0.2s;
}

.login-button:hover {
  box-shadow: 0 8px 20px rgba(42, 82, 152, 0.35);
  transform: translateY(-1px);
}

.login-button:active {
  transform: translateY(0);
}

.login-button:disabled {
  cursor: not-allowed;
  opacity: 0.6;
  transform: none;
  box-shadow: none;
}

.forgot-link {
  color: #5a6b85;
  font-size: 13px;
  text-decoration: none;
  transition: color 0.2s;
}

.forgot-link:hover {
  color: #2a5298;
  text-decoration: underline;
}

.link-row {
  display: flex;
  flex-direction: row;
  justify-content: space-between;
  align-items: center;
  margin-top: 2px;
}

.reset-form {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.reset-tip {
  margin: 0;
  color: #8190a5;
  font-size: 13px;
  line-height: 1.5;
}

/* 对话框整体：加宽加高、品牌风格 */
.el-dialog {
  border-radius: 16px;
  box-shadow: 0 20px 50px rgba(0, 0, 0, 0.25);
  overflow: hidden;
}

.el-dialog__header {
  margin: 0;
  padding: 20px 24px;
}

.el-dialog__title {
  color: #1f2d3d;
  font-size: 17px;
  font-weight: 600;
  letter-spacing: 1px;
}

.el-dialog__body {
  padding: 28px 32px;
  min-height: 200px;
}

.el-dialog__footer {
  padding: 0 32px 28px;
}

/* 注册表单：增大行距与控件，与登录页风格统一 */
.register-form .el-form-item {
  margin-bottom: 20px;
}

.register-form .el-input__wrapper {
  border-radius: 8px;
  background-color: #f7f9fc;
  box-shadow: 0 0 0 1px #e0e4ec inset;
  padding: 1px 14px;
}

.register-form .el-input__wrapper.is-focus {
  background-color: #fff;
  box-shadow: 0 0 0 1px #2a5298 inset, 0 0 0 3px rgba(42, 82, 152, 0.15);
}

.register-form .el-input__inner {
  height: 40px;
  color: #1f2d3d;
  font-size: 14px;
}

.register-form .el-input__inner::placeholder {
  color: #a8b0bd;
}

.register-form .el-form-item__label {
  color: #5a6b85;
  font-weight: 500;
}

/* 对话框底部主按钮与登录按钮风格一致 */
.dialog-footer .el-button--primary {
  background: linear-gradient(90deg, #1e3c72, #2a5298);
  border: none;
  border-radius: 8px;
  padding: 10px 24px;
  font-weight: 600;
  letter-spacing: 1px;
  box-shadow: 0 6px 16px rgba(42, 82, 152, 0.3);
}

.dialog-footer .el-button--primary:hover {
  box-shadow: 0 8px 20px rgba(42, 82, 152, 0.4);
  transform: translateY(-1px);
  transition: transform 0.15s, box-shadow 0.2s;
}

.dialog-footer .el-button:not(.el-button--primary) {
  border-radius: 8px;
  padding: 10px 24px;
}

/* 对话框内输入框与按钮适配品牌风格 */
.el-dialog .login-input {
  height: 40px;
}</style>
