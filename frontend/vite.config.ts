import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import vueDevTools from 'vite-plugin-vue-devtools'
import Components from 'unplugin-vue-components/vite'
import { ElementPlusResolver } from 'unplugin-vue-components/resolvers'


// https://vite.dev/config/
// 部署形态：前端静态资源部署到 Tomcat 的 hcs 目录（context-path /hcs），故 base = '/hcs/'；
// 路由 history 基础路径由 src/router/index.ts 用 import.meta.env.BASE_URL 自动跟随。
//
// 接口地址：后端为 Spring Boot（context-path /api），路由自带 /api 前缀
// （如 /api/auth/login），而 src/api/*.ts 中写的也是完整路径 '/api/auth/login'，所以：
//   - dev：下面的 server.proxy 生效（改后端地址改这里，重启 dev 即可）；
//   - 生产：server.proxy 不参与打包！地址来自 .env.production 的 VITE_API_BASE_URL，
//     改完必须重新 npm run build:war。baseURL 只填后端 origin 根，切勿再带 /api，
//     避免 /api/api/... 重复前缀。
export default defineConfig({
  base: '/hcs/',
  server: {
    // 显式监听所有网络接口（含 IPv4/IPv6），避免默认只绑定 ::1 导致 127.0.0.1 访问被拒
    host: '0.0.0.0',
    proxy: {
      '/api': {
        target: 'http://127.0.0.1:8080',
        changeOrigin: true,
      },
    },
  },
  plugins: [
    Components({
      resolvers: [ElementPlusResolver()],
      dts: true
    }),
    vue(),
    vueDevTools()
  ],
})
