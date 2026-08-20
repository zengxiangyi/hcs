import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'
import { restoreCurrentUserRights, hasRight } from '../components/hcs/sys/permission'

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
        path: 'data/data2',
        name: 'Data2',
        component: () => import('../components/data/data2.vue'),
      },
      {
        path: 'data/data1',
        name: 'Data1',
        component: () => import('../components/data/data1.vue'),
      },
      {
        path: 'hcs/flow/closed',
        name: 'Closed',
        component: () => import('../components/hcs/flow/closed.vue'),
      },
      {
        path: 'hcs/tech/board',
        name: 'Board',
        component: () => import('../components/hcs/tech/board.vue'),
      },
      {
        path: 'hcs/info/file',
        name: 'File',
        component: () => import('../components/hcs/info/file.vue'),
      },
      {
        path: 'hcs/process/job',
        name: 'ProcessJob',
        component: () => import('../components/hcs/process/job.vue'),
      },
      // 系统管理：用户/角色/权限
      {
        path: 'hcs/sys/user',
        name: 'SysUser',
        component: () => import('../components/hcs/sys/user.vue'),
        meta: { right: 'page:user' },
      },
      {
        path: 'hcs/sys/role',
        name: 'SysRole',
        component: () => import('../components/hcs/sys/role.vue'),
        meta: { right: 'page:role' },
      },
      {
        path: 'hcs/sys/right',
        name: 'SysRight',
        component: () => import('../components/hcs/sys/right.vue'),
        meta: { right: 'page:right' },
      },
      {
        path: 'hcs/sys/roleUser',
        name: 'SysRoleUser',
        component: () => import('../components/hcs/sys/roleUser.vue'),
        meta: { right: 'page:roleUser' },
      },
      {
        path: 'hcs/sys/roleRight',
        name: 'SysRoleRight',
        component: () => import('../components/hcs/sys/roleRright.vue'),
        meta: { right: 'page:roleRight' },
      },
    ],
  },
]

const WHITE_LIST = ['Login']

const router = createRouter({
  history: createWebHistory(),
  routes,
})

// 进入任意路由前：恢复权限缓存，并对带 page:* 权限标记的路由做页面级拦截
router.beforeEach((to) => {
  restoreCurrentUserRights()

  // 白名单（登录页）直接放行
  if (WHITE_LIST.includes(to.name as string)) return true

  // 未登录：跳登录页
  const token = localStorage.getItem('token')
  if (!token) return { name: 'Login', query: { redirect: to.fullPath } }

  // 页面权限校验：路由声明了 meta.right（page:*）时，当前用户必须拥有该权限
  const need = to.meta.right as string | undefined
  if (need && !hasRight(need)) {
    return { name: 'Web', query: { noAuth: to.fullPath } }
  }
  return true
})

export default router
