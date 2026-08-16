import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'

const routes: RouteRecordRaw[] = [
  {
    path: '/',
    name: 'Login',
    component: () => import('../components/Login.vue'),
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('../components/Login.vue'),
  },
  {
    path: '/web',
    name: 'Web',
    component: () => import('../components/Web.vue'),
    children: [
      {
        path: 'data/data1',
        name: 'Data1',
        component: () => import('../components/data/data1.vue'),
      },
      {
        path: 'data/data2',
        name: 'Data2',
        component: () => import('../components/data/data2.vue'),
      },
      {
        path: 'hello',
        name: 'Hello',
        component: () => import('../components/HelloWorld.vue'),
      },
    ],
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

export default router
