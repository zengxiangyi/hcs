import { createApp } from 'vue'
import './style.css'
import './style/common.css'
import App from './App.vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import zhCn from 'element-plus/es/locale/lang/zh-cn.mjs'
import 'element-plus/dist/index.css'
import router from './router'

const app = createApp(App)
app.use(createPinia())
app.use(ElementPlus, { locale: zhCn })
app.use(router)
app.mount('#app')
