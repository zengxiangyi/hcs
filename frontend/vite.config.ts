import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import vueDevTools from 'vite-plugin-vue-devtools'
import Components from 'unplugin-vue-components/vite'
import { ElementPlusResolver } from 'unplugin-vue-components/resolvers'


// https://vite.dev/config/
export default defineConfig({
  server: {
    // 显式监听所有网络接口（含 IPv4/IPv6），避免默认只绑定 ::1 导致 127.0.0.1 访问被拒
    host: '0.0.0.0',
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
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
