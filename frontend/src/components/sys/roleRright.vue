<script setup lang="ts">
import { ref, computed, watch, onMounted, type Ref } from 'vue'
import { ElMessage } from 'element-plus'
import { roleAPI, type RoleRow } from '../../api/sysRole'
import { rightAPI, type RightRow } from '../../api/sysRight'
import { roleRightAPI } from '../../api/sysRoleRight'

defineOptions({ name: 'RoleRight' })

const roleOptions = ref<RoleRow[]>([])
const rightOptions = ref<RightRow[]>([])
const selectedRoleCode = ref<string>('')
const loading = ref(false)

// 分类中文映射（right.category 存的是 page/button）
const categoryLabels: Record<string, string> = { page: '页面', button: '按钮', directory: '目录'}

// 按分类分组展示权限
const groupedRights = computed(() => {
  const map = new Map<string, RightRow[]>()
  rightOptions.value.forEach((r) => {
    if (!map.has(r.category)) map.set(r.category, [])
    map.get(r.category)!.push(r)
  })
  return [...map.entries()].map(([category, list]) => ({ category, label: categoryLabels[category] || category, list }))
})

// 当前角色勾选的权限编码
const checkedRightCodes = ref<string[]>([])

/**
 * el-checkbox 的 modelValue 类型只声明为标量，但运行时支持数组模型
 * （element-plus useCheckbox#addToStore 会 push/remove 数组元素）。
 * 此处断言为标量 Ref，仍指向同一个数组实例，勾选结果写回 checkedRightCodes。
 */
const rightCheckModel = checkedRightCodes as unknown as Ref<string | number | boolean>

async function loadRoles() {
  const res = await roleAPI.search({page:1, pageSize: 99999 })
  roleOptions.value = res.data.content
  if (roleOptions.value.length && !selectedRoleCode.value) {
    selectedRoleCode.value = roleOptions.value[0].code
  }
}

async function loadRights() {
  const res = await rightAPI.search({page:1,pageSize: 99999 })
  rightOptions.value = res.data.content
}

async function syncChecked() {
  if (!selectedRoleCode.value) {
    checkedRightCodes.value = []
    return
  }
  try {
    const res = await roleRightAPI.listByRole(selectedRoleCode.value)
    checkedRightCodes.value = res.data.rightCodes
  } catch (e) {
    ElMessage.error((e as Error).message || '加载关联失败')
    checkedRightCodes.value = []
  }
}

watch(selectedRoleCode, syncChecked)

onMounted(async () => {
  loading.value = true
  try {
    await Promise.all([loadRoles(), loadRights()])
    await syncChecked()
  } finally {
    loading.value = false
  }
})

// 模块全选切换
function moduleAllChecked(module: string): boolean {
  const codes = rightOptions.value.filter((r) => r.category === module).map((r) => r.code)
  return codes.length > 0 && codes.every((c) => checkedRightCodes.value.includes(c))
}
function toggleModule(module: string, checked: boolean) {
  const codes = rightOptions.value.filter((r) => r.category === module).map((r) => r.code)
  const set = new Set(checkedRightCodes.value)
  codes.forEach((c) => (checked ? set.add(c) : set.delete(c)))
  checkedRightCodes.value = [...set]
}

async function handleSave() {
  if (!selectedRoleCode.value) return ElMessage.warning('请先选择角色')
  try {
    await roleRightAPI.save({ roleCode: selectedRoleCode.value, rightCodes: checkedRightCodes.value })
    ElMessage.success('已保存角色-权限关联')
  } catch (e) {
    ElMessage.error((e as Error).message || '保存失败')
  }
}

// 模块是否半选（部分勾选）
function groupIndeterminate(group: { category: string; label: string; list: RightRow[] }): boolean {
  const has = group.list.some((r) => checkedRightCodes.value.includes(r.code))
  return has && !moduleAllChecked(group.category)
}
</script>

<template>
  <div class="sys-page">
    <h3 class="page-title">角色权限关联</h3>

    <el-alert
      type="info"
      :closable="false"
      show-icon
      title="选择左侧角色，在右侧勾选授予的权限（页面权限控制菜单/路由可见性，按钮权限控制操作按钮）。"
      style="margin-bottom: 16px"
    />

    <el-row :gutter="16">
      <el-col :span="4">
        <div class="panel-title">角色</div>
        <el-menu
          :default-active="selectedRoleCode"
          class="role-menu"
          @select="(i: string) => (selectedRoleCode = i)"
        >
          <el-menu-item v-for="r in roleOptions" :key="r.code" :index="r.code">
            {{ r.name }}
          </el-menu-item>
        </el-menu>
      </el-col>

      <el-col :span="20">
        <div class="panel-title">权限分配</div>
        <div v-for="group in groupedRights" :key="group.category" class="right-group">
          <div class="group-head">
            <el-checkbox
              :model-value="moduleAllChecked(group.category)"
              :indeterminate="groupIndeterminate(group)"
              @change="(v: any) => toggleModule(group.category, v)"
            >
              <strong>{{ group.label }}</strong>
            </el-checkbox>
          </div>
          <div class="group-body">
            <el-checkbox
              v-for="r in group.list"
              :key="r.code"
              v-model="rightCheckModel"
              :value="r.code"
              class="right-item"
            >
              {{ r.name }}
            </el-checkbox>
          </div>
        </div>
        <div class="footer-bar">
          <el-button type="primary" @click="handleSave">保存关联</el-button>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<style scoped>
/* ===================== 页面整体 ===================== */
.sys-page {
  padding: 24px;
  color: #303133;
  background: linear-gradient(180deg, #f6f8fc 0%, #f2f5fa 100%);
  min-height: 100%;
  border-radius: 12px;
}

.page-title {
  font-size: 20px;
  font-weight: 700;
  color: #1f2d3d;
  margin: 0 0 6px;
  letter-spacing: 0.5px;
  display: flex;
  align-items: center;
  gap: 10px;
}
.page-title::before {
  content: '';
  width: 4px;
  height: 20px;
  border-radius: 2px;
  background: linear-gradient(180deg, #409eff, #79bbff);
}

/* 顶部提示条美化 */
.sys-page :deep(.el-alert) {
  border-radius: 10px;
  border: 1px solid #d9ecff;
  background: linear-gradient(90deg, #ecf5ff, #f4f8ff);
  box-shadow: 0 2px 6px rgba(64, 158, 255, 0.06);
}

/* ===================== 左右面板容器 ===================== */
.sys-page :deep(.el-row) {
  margin-top: 16px;
}

/* 角色列面板 */
.sys-page > div:not(.page-title) .el-col:nth-child(1),
.sys-page .el-col-4 {
  background: #fff;
  border: 1px solid #e6ebf2;
  border-radius: 12px;
  padding: 14px;
  box-shadow: 0 4px 16px rgba(31, 45, 61, 0.06);
}

/* 权限分配面板 */
.sys-page .el-col-20 {
  background: #fff;
  border: 1px solid #e6ebf2;
  border-radius: 12px;
  padding: 14px;
  box-shadow: 0 4px 16px rgba(31, 45, 61, 0.06);
}

.panel-title {
  font-weight: 700;
  margin-bottom: 12px;
  color: #1f2d3d;
  font-size: 14px;
  display: flex;
  align-items: center;
  gap: 8px;
}
.panel-title::before {
  content: '';
  width: 4px;
  height: 14px;
  border-radius: 2px;
  background: #409eff;
}

/* ===================== 角色菜单（回退为默认样式） ===================== */
.role-menu {
  border-right: none;
  max-height: 420px;
  overflow: auto;
}

/* ===================== 权限分组卡片（回退为默认样式） ===================== */
.right-group {
  border: 1px solid #ebeef5;
  border-radius: 6px;
  margin-bottom: 12px;
}
.group-head {
  padding: 10px 14px;
  background: #f5f7fa;
  border-bottom: 1px solid #ebeef5;
}
.group-body {
  padding: 12px 14px;
  display: flex;
  flex-wrap: wrap;
  gap: 10px 24px;
}
.right-item { margin-right: 0; }

/* ===================== 底部保存栏 ===================== */
.footer-bar {
  margin-top: 16px;
  text-align: right;
}
</style>
