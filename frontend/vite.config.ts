import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import vueDevTools from 'vite-plugin-vue-devtools'
import Components from 'unplugin-vue-components/vite'
import { ElementPlusResolver } from 'unplugin-vue-components/resolvers'


// https://vite.dev/config/
// 生产构建产物单独打成 ROOT.war（context-path 为 /），与后端 api.war 同 Tomcat 部署，
// 故 base 恒为 '/'；dev 同样为 '/'，靠下面的 /api 代理转发到后端。
// 接口基础地址见 .env.production 的 VITE_API_BASE_URL=/api（同源，无跨域）。
export default defineConfig({
  base: '/',
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
