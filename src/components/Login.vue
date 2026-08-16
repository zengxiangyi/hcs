<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
const router = useRouter()

const username = ref('')
const password = ref('')

function handleLogin() {
  // TODO: 接入登录逻辑
  console.log('username:', username.value, 'password:', password.value)
  if(username.value === 'admin' && password.value === '123456'){
    console.log('登录成功')
    // 跳转到main.vue
    router.push('/web')

  }else{
    console.log('登录失败')
    return
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
        <button class="login-button" type="submit">确定</button>
      </form>
    </div>
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
</style>
