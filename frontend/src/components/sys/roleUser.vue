<script setup lang="ts">
import { ref, watch, onMounted, type Ref } from 'vue'
import { ElMessage } from 'element-plus'
import { roleAPI, type RoleRow } from '../../api/sysRole'
import { sysUserAPI, type SysUserRow } from '../../api/sysUser'
import { roleUserAPI } from '../../api/sysRoleUser'

defineOptions({ name: 'RoleUser' })

const roleOptions = ref<RoleRow[]>([])
const userOptions = ref<SysUserRow[]>([])
const selectedRoleCode = ref<string>('')
const loading = ref(false)

// 当前角色下的用户 code 集合（用于勾选）
const checkedUserCodes = ref<string[]>([])

/**
 * el-checkbox 的 modelValue 类型只声明为标量，但运行时支持数组模型
 * （element-plus useCheckbox#addToStore 会 push/remove 数组元素）。
 * 此处断言为标量 Ref，仍指向同一个数组实例，勾选结果写回 checkedUserCodes。
 */
const userCheckModel = checkedUserCodes as unknown as Ref<string | number | boolean>
const tableData = ref<SysUserRow[]>([])

// 分页参数
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

async function loadRoles() {
  const res = await roleAPI.search({page:1, pageSize: 99999 })
  roleOptions.value = res.data.content
  if (roleOptions.value.length && !selectedRoleCode.value) {
    selectedRoleCode.value = roleOptions.value[0].code
  }
}

async function loadUsers() {
  const res = await sysUserAPI.search({ page: currentPage.value, pageSize: pageSize.value })
  userOptions.value = res.data.content
  tableData.value = res.data.content
  total.value = res.data.total
}

async function syncChecked() {
  if (!selectedRoleCode.value) {
    checkedUserCodes.value = []
    return
  }
  try {
    const res = await roleUserAPI.listByRole(selectedRoleCode.value)
    checkedUserCodes.value = res.data.userCodes
  } catch (e) {
    ElMessage.error((e as Error).message || '加载关联失败')
    checkedUserCodes.value = []
  }
}

watch(selectedRoleCode, () => {
  currentPage.value = 1
  syncChecked()
})

onMounted(async () => {
  loading.value = true
  try {
    await Promise.all([loadRoles(), loadUsers()])
    await syncChecked()
  } finally {
    loading.value = false
  }
})

// 翻页/改页大小后重新拉取数据
function handlePageChange() {
  loadUsers()
}

async function handleSave() {
  if (!selectedRoleCode.value) return ElMessage.warning('请先选择角色')
  try {
    await roleUserAPI.save({ roleCode: selectedRoleCode.value, userCodes: checkedUserCodes.value })
    ElMessage.success('已保存角色-用户关联')
  } catch (e) {
    ElMessage.error((e as Error).message || '保存失败')
  }
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
      <el-col :span="4">
        <div class="panel-title">角色</div>
        <el-menu :default-active="selectedRoleCode" class="role-menu" @select="(i: string) => (selectedRoleCode = i)">
          <el-menu-item v-for="r in roleOptions" :key="r.code" :index="r.code">
            {{ r.name }}
          </el-menu-item>
        </el-menu>
      </el-col>

      <!-- 用户勾选 -->
      <el-col :span="20">
        <div class="panel-title">
          用户列表
        </div>
        <div class="footer-bar">
          <el-button type="primary" @click="handleSave">保存关联</el-button>
        </div>
        <el-table :data="tableData" border stripe style="width: 100%">
          <el-table-column width="60">
            <template #header>
              <span>选</span>
            </template>
            <template #default="{ row }">
              <el-checkbox v-model="userCheckModel" :value="row.code" />
            </template>
          </el-table-column>
          <el-table-column prop="code" label="工号" min-width="120" />
          <el-table-column prop="name" label="姓名" min-width="120" />
          <el-table-column prop="department" label="部门" min-width="120" />
          <el-table-column prop="position" label="岗位" min-width="100" />
        </el-table>
        <div class="pager">
          <el-pagination
            layout="total, sizes, prev, pager, next, jumper"
            :total="total"
            :current-page="currentPage"
            :page-size="pageSize"
            :page-sizes="[10, 20, 50, 100]"
            @current-change="(p: number) => { currentPage = p; handlePageChange() }"
            @size-change="(s: number) => { pageSize = s; currentPage = 1; handlePageChange() }"
          />
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<style scoped>
.sys-page { padding: 20px; color: #333; }
.panel-title { font-weight: 600; margin-bottom: 10px; color: #303133; }
.role-menu { border-right: none; max-height: 420px; overflow: auto; }
.footer-bar { margin-top: 16px; text-align: right;margin-bottom: 16px; }
.pager { margin-top: 14px; display: flex; justify-content: flex-end; }
</style>
