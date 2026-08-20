<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { roles, rights, roleRightMap, getRightCodesByRole, type Right } from './mock'

defineOptions({ name: 'SysRoleRight' })

const roleOptions = roles
const selectedRoleId = ref<number>(roles[0]?.id ?? 0)

// 按模块分组展示权限
const groupedRights = computed(() => {
  const map = new Map<string, Right[]>()
  rights.forEach((r) => {
    if (!map.has(r.module)) map.set(r.module, [])
    map.get(r.module)!.push(r)
  })
  return [...map.entries()].map(([module, list]) => ({ module, list }))
})

// 当前角色勾选的权限 id
const checkedRightIds = ref<number[]>([])

function syncChecked() {
  if (!selectedRoleId.value) {
    checkedRightIds.value = []
    return
  }
  const codes = getRightCodesByRole(selectedRoleId.value)
  checkedRightIds.value = rights.filter((r) => codes.includes(r.code)).map((r) => r.id)
}

watch(selectedRoleId, syncChecked, { immediate: true })

// 模块全选切换
function moduleAllChecked(module: string): boolean {
  const ids = rights.filter((r) => r.module === module).map((r) => r.id)
  return ids.length > 0 && ids.every((id) => checkedRightIds.value.includes(id))
}
function toggleModule(module: string, checked: boolean) {
  const ids = rights.filter((r) => r.module === module).map((r) => r.id)
  const set = new Set(checkedRightIds.value)
  ids.forEach((id) => (checked ? set.add(id) : set.delete(id)))
  checkedRightIds.value = [...set]
}

function handleSave() {
  if (!selectedRoleId.value) return ElMessage.warning('请先选择角色')
  roleRightMap[selectedRoleId.value] = [...checkedRightIds.value]
  ElMessage.success('已保存角色-权限关联')
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
      <el-col :span="6">
        <div class="panel-title">角色</div>
        <el-menu
          :default-active="String(selectedRoleId)"
          class="role-menu"
          @select="(i: string) => (selectedRoleId = Number(i))"
        >
          <el-menu-item v-for="r in roleOptions" :key="r.id" :index="String(r.id)">
            {{ r.name }}
          </el-menu-item>
        </el-menu>
      </el-col>

      <el-col :span="18">
        <div class="panel-title">权限分配</div>
        <div v-for="group in groupedRights" :key="group.module" class="right-group">
          <div class="group-head">
            <el-checkbox
              :model-value="moduleAllChecked(group.module)"
              :indeterminate="checkedRightIds.some((id) => group.list.some((r) => r.id === id)) && !moduleAllChecked(group.module)"
              @change="(v: any) => toggleModule(group.module, v)"
            >
              <strong>{{ group.module }}</strong>
            </el-checkbox>
          </div>
          <div class="group-body">
            <el-checkbox
              v-for="r in group.list"
              :key="r.id"
              v-model="checkedRightIds"
              :value="r.id"
              class="right-item"
            >
              {{ r.name }}
              <el-tag size="small" :type="r.type === 'page' ? 'primary' : 'warning'" style="margin-left: 6px">
                {{ r.type === 'page' ? '页面' : '按钮' }}
              </el-tag>
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
