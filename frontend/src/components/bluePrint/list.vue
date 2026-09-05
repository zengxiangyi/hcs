<script setup lang="ts">
import { ref, computed, watch, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { blueprintAPI, type BluePrintRow } from '../../api/blueprint'

const router = useRouter()

defineOptions({ name: 'BluePrintList' })

/** 查询表单 */
interface QueryForm {
  code: string
  name: string
  state: string
  firstLevel: string
  secondLevel: string
}

// 查询条件
const query = ref<QueryForm>({
  code: '',
  name: '',
  state: '',
  firstLevel: '',
  secondLevel: '',
})

/** 一级工艺 → 二级工艺 层级关系 */
interface CraftNode {
  value: string
  label: string
  children: { value: string; label: string }[]
}
const craftTree: CraftNode[] = [
  {
    value: 'TZ',
    label: '调质',
    children: [
      { value: 'TZ01', label: '油冷调质' },
      { value: 'TZ02', label: '喷雾调质' },
      { value: 'TZ03', label: '正火调质' },
    ],
  },
  {
    value: 'CH',
    label: '辊身淬火',
    children: [
      { value: 'CH01', label: '工频感应淬火' },
      { value: 'CH02', label: '立式中频感应淬火' },
      { value: 'CH03', label: '双频感应淬火' },
      { value: 'CH04', label: '整体感应淬火' },
      { value: 'CH05', label: '森辊整体淬火' },
      { value: 'CH06', label: '卧式中频感应辊身淬火' },
    ],
  },
  {
    value: 'TH',
    label: '退火',
    children: [
      { value: 'TH01', label: '去应力退火' },
      { value: 'TH02', label: '卧式中频感应辊颈退火' }
    ],
  },
  {
    value: 'ZH',
    label: '正火',
    children: [
      { value: 'ZH01', label: '正火球化' }
    ]
  },
    {
    value: 'TP',
    label: '临时',
    children: [
      { value: 'TP01', label: '高温回火' },
      { value: 'TP02', label: '预热矫直' },
      { value: 'TP03', label: '回火' },
      { value: 'TP04', label: '冷处理' },
      { value: 'TP05', label: '调质正火' },
      { value: 'TP06', label: '工频感应正火' },
      { value: 'TP07', label: '整体感应正火' },
    ],
  }
]

/** 一级工艺下拉选项 */
const firstOptions = computed(() => craftTree.map((n) => ({ value: n.value, label: n.label })))

/** 二级工艺下拉选项：随一级工艺联动 */
const secondOptions = computed(() => {
  const node = craftTree.find((n) => n.value === query.value.firstLevel)
  return node ? node.children.map((c) => ({ value: c.value, label: c.label })) : []
})

/** 编码 → 中文 映射，用于表格回显 */
const firstLabelMap = computed<Record<string, string>>(() => {
  const map: Record<string, string> = {}
  craftTree.forEach((n) => (map[n.value] = n.label))
  return map
})
const secondLabelMap = computed<Record<string, string>>(() => {
  const map: Record<string, string> = {}
  craftTree.forEach((n) => n.children.forEach((c) => (map[c.value] = c.label)))
  return map
})

/** 一级工艺变更：清空二级工艺（参考 board.vue 的联动逻辑） */
watch(
  () => query.value.firstLevel,
  () => {
    query.value.secondLevel = ''
  },
)

// 状态选项
const stateOptions = ['草稿', '已发布', '已归档', '作废']

// 服务端分页数据
const tableData = ref<BluePrintRow[]>([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)
const loading = ref(false)

// 查询（服务端分页 + 过滤）
async function fetchData() {
  loading.value = true
  try {
    const res = await blueprintAPI.search({
      code: query.value.code.trim() || undefined,
      name: query.value.name.trim() || undefined,
      state: query.value.state || undefined,
      firstLevel: query.value.firstLevel || undefined,
      secondLevel: query.value.secondLevel || undefined,
      page: currentPage.value,
      pageSize: pageSize.value,
    })
    tableData.value = res.data.content
    total.value = res.data.total
  } catch (err) {
    ElMessage.error((err as Error).message || '查询失败')
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  currentPage.value = 1
  fetchData()
}

function handleReset() {
  query.value = { code: '', name: '', state: '', firstLevel: '', secondLevel: ''}
  currentPage.value = 1
  fetchData()
}

function handlePageChange() {
  fetchData()
}

function handleSizeChange() {
  currentPage.value = 1
  fetchData()
}

function stateTagType(state: string): 'info' | 'warning' | 'success' | 'danger' {
  switch (state) {
    case '草稿':
      return 'info'
    case '已发布':
      return 'success'
    case '已归档':
      return 'warning'
    case '作废':
      return 'danger'
    default:
      return 'info'
  }
}

function handleEdit(row: BluePrintRow) {
  // 跳转到蓝本草稿页，通过 code 参数加载编辑信息
  router.push({ name: 'Draft', query: { code: row.code, edition: row.edition} })
}

function handleCopy(row: BluePrintRow){
  router.push({ name: 'Draft', query: { code: row.code, edition: row.edition} })
}

async function handleDelete(row: BluePrintRow) {
  try {
    await ElMessageBox.confirm(`确认删除蓝本「${row.code} ${row.name}」？`, '提示', {
      type: 'warning',
    })
  } catch {
    return
  }
  try {
    await blueprintAPI.remove(row.code,row.edition)
    ElMessage.success(`已删除：${row.code}`)
    // 删除当前页最后一条时回退一页
    if (tableData.value.length === 1 && currentPage.value > 1) {
      currentPage.value -= 1
    }
    fetchData()
  } catch (err) {
    ElMessage.error((err as Error).message || '删除失败')
  }
}

onMounted(fetchData)
</script>

<template>
  <div class="blueprint-page">
    <h3 class="page-title">蓝本清单管理</h3>

    <!-- 查询区 -->
    <el-form :inline="true" class="query-form" @submit.prevent>
      <el-form-item label="蓝本编码">
        <el-input
          v-model="query.code"
          placeholder="蓝本编码"
          clearable
          style="width: 180px"
        />
      </el-form-item>
      <el-form-item label="蓝本名称">
        <el-input
          v-model="query.name"
          placeholder="蓝本名称"
          clearable
          style="width: 180px"
        />
      </el-form-item>
       <el-form-item label="一级工艺">
      <el-select v-model="query.firstLevel" placeholder="请选择一级工艺" clearable>
            <el-option
              v-for="opt in firstOptions"
              :key="opt.value"
              :label="opt.label"
              :value="opt.value"
            />
      </el-select>
      </el-form-item>
       <el-form-item label="二级工艺">
          <el-select v-model="query.secondLevel" placeholder="请选择二级工艺" clearable :disabled="!query.firstLevel">
            <el-option
              v-for="opt in secondOptions"
              :key="opt.value"
              :label="opt.label"
              :value="opt.value"
            />
          </el-select>
        </el-form-item>

      <el-form-item label="状态">
        <el-select v-model="query.state" placeholder="全部状态" clearable style="width: 140px">
          <el-option v-for="s in stateOptions" :key="s" :label="s" :value="s" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="handleSearch">查询</el-button>
        <el-button @click="handleReset">重置</el-button>
      </el-form-item>
    </el-form>

    <!-- 表格 -->
    <el-table v-loading="loading" :data="tableData" border stripe style="width: 100%">
      <el-table-column type="index" label="序号" width="70" fixed="left" />
      <el-table-column prop="code" label="蓝本工艺编码" width="120" />
      <el-table-column prop="name" label="蓝本名称" min-width="140" />
      <el-table-column prop="graph" label="图号" width="110" />
      <el-table-column prop="firstLevel" label="一级分类" width="110">
        <template #default="{ row }">
          {{ firstLabelMap[row.firstLevel] || row.firstLevel }}
        </template>
      </el-table-column>
      <el-table-column prop="secondLevel" label="二级分类" width="110">
        <template #default="{ row }">
          {{ secondLabelMap[row.secondLevel] || row.secondLevel }}
        </template>
      </el-table-column>
      <el-table-column prop="materialName" label="物料名称" min-width="120" />
      <el-table-column prop="materialCode" label="材料编码" width="120" />
      <el-table-column prop="weight" label="单重" width="100" />
  
      <el-table-column prop="isFirstCheck" label="是否首检" width="100" />
      <el-table-column prop="firstHardness" label="首检硬度要求" width="120" />
      <el-table-column prop="lastHardness" label="完工硬度要求" width="120" />
      <el-table-column prop="busbarNum" label="母线数量" width="100" />
      <el-table-column prop="testNum" label="测点数量" width="110" />
      <el-table-column prop="coolTime" label="冷却时间" width="100" />
      <el-table-column prop="hardnessDepth" label="硬化层深度" width="110" />
      <el-table-column prop="chamfer" label="辊身倒角" width="90" />
      <el-table-column prop="fallHead" label="落差" width="90" />
      <el-table-column prop="quenching" label="淬火部位" width="100" />
      <el-table-column prop="attention" label="注意事项" min-width="140" show-overflow-tooltip />
      <el-table-column prop="model" label="型号" width="100" />
      <el-table-column prop="specs" label="规格" width="120" />
      <el-table-column prop="customer" label="客户名称" width="100" />
      <el-table-column prop="edition" label="版本" width="90" />
      <el-table-column prop="state" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="stateTagType(row.state)">{{ row.state }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="remark" label="备注" min-width="140" show-overflow-tooltip />
      <el-table-column prop="createUser" label="创建人" width="100" />
      <el-table-column prop="createTime" label="创建时间" width="180" />
      <el-table-column label="操作" width="200" fixed="right">
        <template #default="{ row }">
          <el-button size="small" type="warning" @click="handleCopy(row as BluePrintRow)">复制</el-button>
          <el-button size="small" type="primary" @click="handleEdit(row as BluePrintRow)">编辑</el-button>
          <el-button size="small" type="danger" @click="handleDelete(row as BluePrintRow)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <el-pagination
      v-model:current-page="currentPage"
      v-model:page-size="pageSize"
      :total="total"
      :page-sizes="[10, 20, 50, 100]"
      layout="total, sizes, prev, pager, next, jumper"
      class="pagination"
      @current-change="handlePageChange"
      @size-change="handleSizeChange"
    />
  </div>
</template>

<style scoped>
.blueprint-page {
  padding: 20px;
  color: #333;
}

.query-form :deep(.el-select) {
  width: 200px;
}
</style>
