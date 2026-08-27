<script setup lang="ts">
import { ref, computed, watch, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { roleAPI, type RoleRow } from '../../api/role'
import { rightAPI, type RightRow } from '../../api/right'
import { roleRightAPI } from '../../api/roleRight'

defineOptions({ name: 'RoleRight' })

const roleOptions = ref<RoleRow[]>([])
const rightOptions = ref<RightRow[]>([])
const selectedRoleCode = ref<string>('')
const loading = ref(false)

// 分类中文映射（right.category 存的是 page/button）
const categoryLabels: Record<string, string> = { page: '页面', button: '按钮' }

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

async function loadRoles() {
  const res = await roleAPI.list({ pageSize: 99999 })
  roleOptions.value = res.data.content
  if (roleOptions.value.length && !selectedRoleCode.value) {
    selectedRoleCode.value = roleOptions.value[0].code
  }
}

async function loadRights() {
  const res = await rightAPI.list({ pageSize: 99999 })
  rightOptions.value = res.data.content
}

async function syncChecked() {
  if (!selectedRoleCode.value) {
    checkedRightCodes.value = []
    return
  }
  try {
    const res = await roleRightAPI.list(selectedRoleCode.value)
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
              v-model="checkedRightCodes"
              :value="r.code"
              class="right-item"
            >
              {{ r.name }}
              <el-tag size="small" type="info" style="margin-left: 6px">{{ r.code }}</el-tag>
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
.sys-page { padding: 20px; color: #333; }
.page-title { margin: 0 0 16px; font-size: 18px; color: #303133; }
.panel-title { font-weight: 600; margin-bottom: 10px; color: #303133; }
.role-menu { border-right: none; max-height: 420px; overflow: auto; }
.right-group { border: 1px solid #ebeef5; border-radius: 6px; margin-bottom: 12px; }
.group-head { padding: 10px 14px; background: #f5f7fa; border-bottom: 1px solid #ebeef5; }
.group-body { padding: 12px 14px; display: flex; flex-wrap: wrap; gap: 10px 24px; }
.right-item { margin-right: 0; }
.footer-bar { margin-top: 16px; text-align: right; }
</style>
