<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { roles, users, userRoleMap, getRoleIdsByUser } from './mock'

defineOptions({ name: 'SysRoleUser' })

const roleOptions = roles
const selectedRoleId = ref<number>(roles[0]?.id ?? 0)

// 当前角色下的用户 id 集合（用于勾选）
const checkedUserIds = ref<number[]>([])

// 计算每个用户拥有哪些角色（表格展示）
const tableData = ref(
  users.map((u) => ({
    ...u,
    roleNames: getRoleIdsByUser(u.id)
      .map((rid) => roles.find((r) => r.id === rid)?.name)
      .filter(Boolean)
      .join('，') || '—',
  }))
)

function syncChecked() {
  if (!selectedRoleId.value) {
    checkedUserIds.value = []
    return
  }
  checkedUserIds.value = users
    .filter((u) => (userRoleMap[u.id] ?? []).includes(selectedRoleId.value))
    .map((u) => u.id)
}

watch(selectedRoleId, syncChecked, { immediate: true })

// 过滤后用于表格展示（可选：按角色过滤查看）
const onlyThisRole = ref(false)
const displayData = computed(() => {
  if (!onlyThisRole.value || !selectedRoleId.value) return tableData.value
  return tableData.value.filter((u) => (userRoleMap[u.id] ?? []).includes(selectedRoleId.value))
})

function handleSave() {
  if (!selectedRoleId.value) return ElMessage.warning('请先选择角色')
  // 反向维护 userRoleMap：把勾选的用户加入该角色，未勾选的移除
  users.forEach((u) => {
    const list = userRoleMap[u.id] ?? (userRoleMap[u.id] = [])
    const idx = list.indexOf(selectedRoleId.value)
    if (checkedUserIds.value.includes(u.id)) {
      if (idx === -1) list.push(selectedRoleId.value)
    } else {
      if (idx > -1) list.splice(idx, 1)
    }
  })
  // 同步表格展示
  tableData.value = users.map((u) => ({
    ...u,
    roleNames: getRoleIdsByUser(u.id)
      .map((rid) => roles.find((r) => r.id === rid)?.name)
      .filter(Boolean)
      .join('，') || '—',
  }))
  ElMessage.success('已保存角色-用户关联')
}
</script>

<template>
  <div class="sys-page">
    <h3 class="page-title">角色用户关联</h3>

    <el-alert
      type="info"
      :closable="false"
      show-icon
      title="选择左侧角色，在右侧勾选属于该角色的用户，点击保存即完成授权。"
      style="margin-bottom: 16px"
    />

    <el-row :gutter="16">
      <!-- 角色选择 -->
      <el-col :span="6">
        <div class="panel-title">角色</div>
        <el-menu :default-active="String(selectedRoleId)" class="role-menu" @select="(i: string) => (selectedRoleId = Number(i))">
          <el-menu-item v-for="r in roleOptions" :key="r.id" :index="String(r.id)">
            {{ r.name }}
          </el-menu-item>
        </el-menu>
      </el-col>

      <!-- 用户勾选 -->
      <el-col :span="18">
        <div class="panel-title">
          用户列表
          <el-checkbox v-model="onlyThisRole" style="margin-left: 12px">仅看本角色</el-checkbox>
        </div>
        <el-table :data="displayData" border stripe style="width: 100%">
          <el-table-column width="60">
            <template #header>
              <span>选</span>
            </template>
            <template #default="{ row }">
              <el-checkbox v-model="checkedUserIds" :value="row.id" />
            </template>
          </el-table-column>
          <el-table-column prop="username" label="账号" min-width="120" />
          <el-table-column prop="nickname" label="昵称" min-width="120" />
          <el-table-column prop="dept" label="部门" min-width="120" />
          <el-table-column prop="roleNames" label="当前角色" min-width="180" />
        </el-table>
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
.footer-bar { margin-top: 16px; text-align: right; }
</style>
