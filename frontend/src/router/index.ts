import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'
import { restoreCurrentUserRights, hasRight } from '../components/sys/permission'

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
        path: 'bluePrint/list',
        name: 'BluePrintList',
        component: () => import('../components/bluePrint/list.vue'),
      },
      {
        path: 'approval/send',
        name: 'Send',
        component: () => import('../components/approval/send.vue'),
      },
      {
        path: 'approval/todo',
        name: 'Todo',
        component: () => import('../components/approval/todo.vue'),
      },
      {
        path: 'approval/done',
        name: 'Done',
        component: () => import('../components/approval/done.vue'),
      },
      {
        path: 'tech/board',
        name: 'Board',
        component: () => import('../components/tech/board.vue'),
      },
      {
        path: 'info/file',
        name: 'File',
        component: () => import('../components/info/file.vue'),
      },
      {
        path: 'process/task',
        name: 'Task',
        component: () => import('../components/process/task.vue'),
      },
      {
        path: 'process/flow',
        name: 'ProcessFlow',
        component: () => import('../components/process/process.vue'),
      },
      {
        path: 'process/flow-h',
        name: 'ProcessFlowH',
        component: () => import('../components/process/process_h.vue'),
      },
      {
        path: 'product/transfer',
        name: 'Transfer',
        component: () => import('../components/product/transfer.vue'),
      },
      {
        path: 'product/state',
        name: 'State',
        component: () => import('../components/product/state.vue'),
      },
      // 系统管理：用户/角色/权限
      {
        path: 'sys/user',
        name: 'SysUser',
        component: () => import('../components/sys/user.vue'),
      },
      {
        path: 'sys/role',
        name: 'SysRole',
        component: () => import('../components/sys/role.vue'),
      },
      {
        path: 'sys/right',
        name: 'SysRight',
        component: () => import('../components/sys/right.vue'),
      },
      {
        path: 'sys/roleUser',
        name: 'SysRoleUser',
        component: () => import('../components/sys/roleUser.vue'),
      },
      {
        path: 'sys/roleRight',
        name: 'SysRoleRight',
        component: () => import('../components/sys/roleRright.vue'),
      },
      {
        path: 'sys/constValue',
        name: 'SysConstValue',
        component: () => import('../components/sys/constValue.vue')
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
