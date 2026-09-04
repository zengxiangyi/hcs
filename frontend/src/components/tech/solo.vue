<script setup lang="ts">
import { reactive } from 'vue'
import type { StepForm } from '../process/steps/shared.js'

defineOptions({ name: 'Solo' })

interface BlockState {
  /** 内容区是否展开 */
  open: boolean
  /** 整个区块是否已被删除（隐藏） */
  removed: boolean
}

/**
 * 20 个区块的显隐状态（仅状态集中管理，DOM 结构在模板中平铺展开）。
 * 后期每个区块填充表单/表格时，各自的业务数据另行单独声明即可。
 */
const blocks = reactive<BlockState[]>(
  Array.from({ length: 20 }, () => ({ open: true, removed: false })),
)

/**
 * 内嵌步骤表单的数据：按区块序号（1 起）存放。
 * StepS13 的 modelValue 为必填，必须由父级提供并双向绑定，
 * 与 process.vue 的 formData 保持一致，折叠区块不会丢失已填内容。
 */
const stepForms = reactive<Record<number, StepForm>>({
  2: {},
  3: {},
  4: {},
  5: {},
  6: {},
  7: {},
  8: {},
  9: {},
  10: {},
  11: {},
  12: {},
  13: {},
  14: {},
  15: {},
  16: {},
  17: {},
  18: {},
  19: {},
  20: {},
})

/** 折叠 / 展开：切换第 index 个区块（1 起）的内容区 */
function toggle(index: number) {
  const block = blocks[index - 1]
  if (!block) return
  block.open = !block.open
}

/** 删除：隐藏第 index 个区块整体 */
function remove(index: number) {
  const block = blocks[index - 1]
  if (!block) return
  block.removed = true
}
</script>

<template>
  <!--工艺编制-->
  <!---->
  <!--工艺编制-->
  <div class="solo-page">
    <!-- ---------- 工艺编制：3 行 × 8 列表格 ---------- -->
    <div class="panel">
      <div class="panel-header">
        <span class="panel-title">工艺编制-卧式中频</span>
      </div>
      <div class="panel-body">
        <table class="grid-table">
          <tbody>
            <tr>
              <th>客户</th>
              <th>图号</th>
              <th>辊号</th>
              <th>材质</th>
              <th>要求</th>
              <th>单重</th>
              <th>主要外形尺寸</th>
            </tr>
            <tr>
              <td>东方集团</td>
              <td>987654321</td>
              <td>324</td>
              <td>304L</td>
              <td>大于100MM</td>
              <td>890KG</td>
              <td>30*30*30</td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <!-- ---------- 区块 1 ---------- -->
    <div v-show="!blocks[0].removed" class="panel">
      <div class="panel-header">
        <span class="panel-title">辊颈硬度检测</span>
        <div class="panel-actions">
          <button class="icon-btn" :title="blocks[0].open ? '折叠' : '展开'" @click="toggle(1)">
            <span class="arrow" :class="{ 'is-collapsed': !blocks[0].open }"></span>
          </button>
          <button class="icon-btn" title="删除" @click="remove(1)">
            <span class="close"></span>
          </button>
        </div>
      </div>
      <div v-show="blocks[0].open" class="panel-body">
        <!-- TODO: 区块 1 的表单 / 表格 -->
      </div>
    </div>

    <!-- ---------- 区块 2 ---------- -->
    <div v-show="!blocks[1].removed" class="panel">
      <div class="panel-header">
        <span class="panel-title">箱炉预热</span>
        <div class="panel-actions">
          <button class="icon-btn" :title="blocks[1].open ? '折叠' : '展开'" @click="toggle(2)">
            <span class="arrow" :class="{ 'is-collapsed': !blocks[1].open }"></span>
          </button>
          <button class="icon-btn" title="删除" @click="remove(2)">
            <span class="close"></span>
          </button>
        </div>
      </div>
      <div v-show="blocks[1].open" class="panel-body">
        <!-- 区块 2 的表单：StepS13 的 modelValue 必填，用 v-model 绑定父级数据 -->
        <StepS13 v-model="stepForms[2]" />
      </div>
    </div>

    <!-- ---------- 区块 3 ---------- -->
    <div v-show="!blocks[2].removed" class="panel">
      <div class="panel-header">
        <span class="panel-title">机床淬火</span>
        <div class="panel-actions">
          <button class="icon-btn" :title="blocks[2].open ? '折叠' : '展开'" @click="toggle(3)">
            <span class="arrow" :class="{ 'is-collapsed': !blocks[2].open }"></span>
          </button>
          <button class="icon-btn" title="删除" @click="remove(3)">
            <span class="close"></span>
          </button>
        </div>
      </div>
      <div v-show="blocks[2].open" class="panel-body">
        <!-- TODO: 区块 3 的表单 / 表格 -->
      </div>
    </div>

    <!-- ---------- 区块 4 ---------- -->
    <div v-show="!blocks[3].removed" class="panel">
      <div class="panel-header">
        <span class="panel-title">续冷</span>
        <div class="panel-actions">
          <button class="icon-btn" :title="blocks[3].open ? '折叠' : '展开'" @click="toggle(4)">
            <span class="arrow" :class="{ 'is-collapsed': !blocks[3].open }"></span>
          </button>
          <button class="icon-btn" title="删除" @click="remove(4)">
            <span class="close"></span>
          </button>
        </div>
      </div>
      <div v-show="blocks[3].open" class="panel-body">
        <!-- TODO: 区块 4 的表单 / 表格 -->
      </div>
    </div>

    <!-- ---------- 区块 5 ---------- -->
    <div v-show="!blocks[4].removed" class="panel">
      <div class="panel-header">
        <span class="panel-title">首检</span>
        <div class="panel-actions">
          <button class="icon-btn" :title="blocks[4].open ? '折叠' : '展开'" @click="toggle(5)">
            <span class="arrow" :class="{ 'is-collapsed': !blocks[4].open }"></span>
          </button>
          <button class="icon-btn" title="删除" @click="remove(5)">
            <span class="close"></span>
          </button>
        </div>
      </div>
      <div v-show="blocks[4].open" class="panel-body">
        <!-- TODO: 区块 5 的表单 / 表格 -->
      </div>
    </div>

    <!-- ---------- 区块 6 ---------- -->
    <div v-show="!blocks[5].removed" class="panel">
      <div class="panel-header">
        <span class="panel-title">测变形</span>
        <div class="panel-actions">
          <button class="icon-btn" :title="blocks[5].open ? '折叠' : '展开'" @click="toggle(6)">
            <span class="arrow" :class="{ 'is-collapsed': !blocks[5].open }"></span>
          </button>
          <button class="icon-btn" title="删除" @click="remove(6)">
            <span class="close"></span>
          </button>
        </div>
      </div>
      <div v-show="blocks[5].open" class="panel-body">
        <!-- TODO: 区块 6 的表单 / 表格 -->
      </div>
    </div>

    <!-- ---------- 区块 7 ---------- -->
    <div v-show="!blocks[6].removed" class="panel">
      <div class="panel-header">
        <span class="panel-title">暂焖</span>
        <div class="panel-actions">
          <button class="icon-btn" :title="blocks[6].open ? '折叠' : '展开'" @click="toggle(7)">
            <span class="arrow" :class="{ 'is-collapsed': !blocks[6].open }"></span>
          </button>
          <button class="icon-btn" title="删除" @click="remove(7)">
            <span class="close"></span>
          </button>
        </div>
      </div>
      <div v-show="blocks[6].open" class="panel-body">
        <!-- TODO: 区块 7 的表单 / 表格 -->
        <StepS13 v-model="stepForms[7]" />
      </div>
    </div>

    <!-- ---------- 区块 8 ---------- -->
    <div v-show="!blocks[7].removed" class="panel">
      <div class="panel-header">
        <span class="panel-title">冷处理</span>
        <div class="panel-actions">
          <button class="icon-btn" :title="blocks[7].open ? '折叠' : '展开'" @click="toggle(8)">
            <span class="arrow" :class="{ 'is-collapsed': !blocks[7].open }"></span>
          </button>
          <button class="icon-btn" title="删除" @click="remove(8)">
            <span class="close"></span>
          </button>
        </div>
      </div>
      <div v-show="blocks[7].open" class="panel-body">
        <!-- TODO: 区块 8 的表单 / 表格 -->
      </div>
    </div>

    <!-- ---------- 区块 9 ---------- -->
    <div v-show="!blocks[8].removed" class="panel">
      <div class="panel-header">
        <span class="panel-title">淬颈</span>
        <div class="panel-actions">
          <button class="icon-btn" :title="blocks[8].open ? '折叠' : '展开'" @click="toggle(9)">
            <span class="arrow" :class="{ 'is-collapsed': !blocks[8].open }"></span>
          </button>
          <button class="icon-btn" title="删除" @click="remove(9)">
            <span class="close"></span>
          </button>
        </div>
      </div>
      <div v-show="blocks[8].open" class="panel-body">
        <!-- TODO: 区块 9 的表单 / 表格 -->
      </div>
    </div>

    <!-- ---------- 区块 10 ---------- -->
    <div v-show="!blocks[9].removed" class="panel">
      <div class="panel-header">
        <span class="panel-title">回火</span>
        <div class="panel-actions">
          <button class="icon-btn" :title="blocks[9].open ? '折叠' : '展开'" @click="toggle(10)">
            <span class="arrow" :class="{ 'is-collapsed': !blocks[9].open }"></span>
          </button>
          <button class="icon-btn" title="删除" @click="remove(10)">
            <span class="close"></span>
          </button>
        </div>
      </div>
      <div v-show="blocks[9].open" class="panel-body">
        <!-- TODO: 区块 10 的表单 / 表格 -->
        <StepS13 v-model="stepForms[10]" />
      </div>
    </div>

    <!-- ---------- 区块 11 ---------- -->
    <div v-show="!blocks[10].removed" class="panel">
      <div class="panel-header">
        <span class="panel-title">测变形</span>
        <div class="panel-actions">
          <button class="icon-btn" :title="blocks[10].open ? '折叠' : '展开'" @click="toggle(11)">
            <span class="arrow" :class="{ 'is-collapsed': !blocks[10].open }"></span>
          </button>
          <button class="icon-btn" title="删除" @click="remove(11)">
            <span class="close"></span>
          </button>
        </div>
      </div>
      <div v-show="blocks[10].open" class="panel-body">
        <!-- TODO: 区块 11 的表单 / 表格 -->
        <StepS07 v-model="stepForms[11]" />
      </div>
    </div>

    <!-- ---------- 区块 12 ---------- -->
    <div v-show="!blocks[11].removed" class="panel">
      <div class="panel-header">
        <span class="panel-title">矫直</span>
        <div class="panel-actions">
          <button class="icon-btn" :title="blocks[11].open ? '折叠' : '展开'" @click="toggle(12)">
            <span class="arrow" :class="{ 'is-collapsed': !blocks[11].open }"></span>
          </button>
          <button class="icon-btn" title="删除" @click="remove(12)">
            <span class="close"></span>
          </button>
        </div>
      </div>
      <div v-show="blocks[11].open" class="panel-body">
        <!-- TODO: 区块 12 的表单 / 表格 -->
      </div>
    </div>

    <!-- ---------- 区块 13 ---------- -->
    <div v-show="!blocks[12].removed" class="panel">
      <div class="panel-header">
        <span class="panel-title">除应力</span>
        <div class="panel-actions">
          <button class="icon-btn" :title="blocks[12].open ? '折叠' : '展开'" @click="toggle(13)">
            <span class="arrow" :class="{ 'is-collapsed': !blocks[12].open }"></span>
          </button>
          <button class="icon-btn" title="删除" @click="remove(13)">
            <span class="close"></span>
          </button>
        </div>
      </div>
      <div v-show="blocks[12].open" class="panel-body">
        <!-- TODO: 区块 13 的表单 / 表格 -->
         <StepS13 v-model="stepForms[13]" />
      </div>
    </div>

    <!-- ---------- 区块 14 ---------- -->
    <div v-show="!blocks[13].removed" class="panel">
      <div class="panel-header">
        <span class="panel-title">硬度叫检</span>
        <div class="panel-actions">
          <button class="icon-btn" :title="blocks[13].open ? '折叠' : '展开'" @click="toggle(14)">
            <span class="arrow" :class="{ 'is-collapsed': !blocks[13].open }"></span>
          </button>
          <button class="icon-btn" title="删除" @click="remove(14)">
            <span class="close"></span>
          </button>
        </div>
      </div>
      <div v-show="blocks[13].open" class="panel-body">
        <!-- TODO: 区块 14 的表单 / 表格 -->
      </div>
    </div>

    <!-- ---------- 区块 15 ---------- -->
    <div v-show="!blocks[14].removed" class="panel">
      <div class="panel-header">
        <span class="panel-title">检硬度</span>
        <div class="panel-actions">
          <button class="icon-btn" :title="blocks[14].open ? '折叠' : '展开'" @click="toggle(15)">
            <span class="arrow" :class="{ 'is-collapsed': !blocks[14].open }"></span>
          </button>
          <button class="icon-btn" title="删除" @click="remove(15)">
            <span class="close"></span>
          </button>
        </div>
      </div>
      <div v-show="blocks[14].open" class="panel-body">
        <!-- TODO: 区块 15 的表单 / 表格 -->
                <table class="grid-table">
          <tbody>
            <tr>
              <th>母线</th>
              <th>1</th>
              <th>2</th>
              <th>3</th>
              <th>4</th>
              <th>5</th>
              <th>6</th>
              <th>7</th>
            </tr>
            <tr>
              <td>母线1</td>
              <td><el-input v-model="stepForms[15].m1_1"></el-input></td>
              <td><el-input v-model="stepForms[15].m1_2"></el-input></td>
              <td><el-input v-model="stepForms[15].m1_3"></el-input></td>
              <td><el-input v-model="stepForms[15].m1_4"></el-input></td>
              <td><el-input v-model="stepForms[15].m1_5"></el-input></td>
              <td><el-input v-model="stepForms[15].m1_6"></el-input></td>
              <td><el-input v-model="stepForms[15].m1_7"></el-input></td>
            </tr>
            <tr>
              <td>母线2</td>
              <td><el-input v-model="stepForms[15].m2_1"></el-input></td>
              <td><el-input v-model="stepForms[15].m2_2"></el-input></td>
              <td><el-input v-model="stepForms[15].m2_3"></el-input></td>
              <td><el-input v-model="stepForms[15].m2_4"></el-input></td>
              <td><el-input v-model="stepForms[15].m2_5"></el-input></td>
              <td><el-input v-model="stepForms[15].m2_6"></el-input></td>
              <td><el-input v-model="stepForms[15].m2_7"></el-input></td>
            </tr>
            <tr>
              <td>母线3</td>
              <td><el-input v-model="stepForms[15].m3_1"></el-input></td>
              <td><el-input v-model="stepForms[15].m3_2"></el-input></td>
              <td><el-input v-model="stepForms[15].m3_3"></el-input></td>
              <td><el-input v-model="stepForms[15].m3_4"></el-input></td>
              <td><el-input v-model="stepForms[15].m3_5"></el-input></td>
              <td><el-input v-model="stepForms[15].m3_6"></el-input></td>
              <td><el-input v-model="stepForms[15].m3_7"></el-input></td>
            </tr>
            <tr>
              <td>母线4</td>
              <td><el-input v-model="stepForms[15].m4_1"></el-input></td>
              <td><el-input v-model="stepForms[15].m4_2"></el-input></td>
              <td><el-input v-model="stepForms[15].m4_3"></el-input></td>
              <td><el-input v-model="stepForms[15].m4_4"></el-input></td>
              <td><el-input v-model="stepForms[15].m4_5"></el-input></td>
              <td><el-input v-model="stepForms[15].m4_6"></el-input></td>
              <td><el-input v-model="stepForms[15].m4_7"></el-input></td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <!-- ---------- 区块 16 ---------- -->
    <div v-show="!blocks[15].removed" class="panel">
      <div class="panel-header">
        <span class="panel-title">合格判定</span>
        <div class="panel-actions">
          <button class="icon-btn" :title="blocks[15].open ? '折叠' : '展开'" @click="toggle(16)">
            <span class="arrow" :class="{ 'is-collapsed': !blocks[15].open }"></span>
          </button>
          <button class="icon-btn" title="删除" @click="remove(16)">
            <span class="close"></span>
          </button>
        </div>
      </div>
      <div v-show="blocks[15].open" class="panel-body">
        <!-- TODO: 区块 16 的表单 / 表格 -->
      </div>
    </div>

    <!-- ---------- 区块 17 ---------- -->
    <div v-show="!blocks[16].removed" class="panel">
      <div class="panel-header">
        <span class="panel-title">冷处理</span>
        <div class="panel-actions">
          <button class="icon-btn" :title="blocks[16].open ? '折叠' : '展开'" @click="toggle(17)">
            <span class="arrow" :class="{ 'is-collapsed': !blocks[16].open }"></span>
          </button>
          <button class="icon-btn" title="删除" @click="remove(17)">
            <span class="close"></span>
          </button>
        </div>
      </div>
      <div v-show="blocks[16].open" class="panel-body">
        <!-- TODO: 区块 17 的表单 / 表格 -->
      </div>
    </div>

    <!-- ---------- 区块 18 ---------- -->
    <div v-show="!blocks[17].removed" class="panel">
      <div class="panel-header">
        <span class="panel-title">二次回火</span>
        <div class="panel-actions">
          <button class="icon-btn" :title="blocks[17].open ? '折叠' : '展开'" @click="toggle(18)">
            <span class="arrow" :class="{ 'is-collapsed': !blocks[17].open }"></span>
          </button>
          <button class="icon-btn" title="删除" @click="remove(18)">
            <span class="close"></span>
          </button>
        </div>
      </div>
      <div v-show="blocks[17].open" class="panel-body">
        <!-- TODO: 区块 18 的表单 / 表格 -->
      </div>
    </div>

    <!-- ---------- 区块 19 ---------- -->
    <div v-show="!blocks[18].removed" class="panel">
      <div class="panel-header">
        <span class="panel-title">二次回火测量</span>
        <div class="panel-actions">
          <button class="icon-btn" :title="blocks[18].open ? '折叠' : '展开'" @click="toggle(19)">
            <span class="arrow" :class="{ 'is-collapsed': !blocks[18].open }"></span>
          </button>
          <button class="icon-btn" title="删除" @click="remove(19)">
            <span class="close"></span>
          </button>
        </div>
      </div>
      <div v-show="blocks[18].open" class="panel-body">
        <!-- TODO: 区块 19 的表单 / 表格 -->
      </div>
    </div>

    <!-- ---------- 区块 20 ---------- -->
    <div v-show="!blocks[19].removed" class="panel">
      <div class="panel-header">
        <span class="panel-title">合格判定</span>
        <div class="panel-actions">
          <button class="icon-btn" :title="blocks[19].open ? '折叠' : '展开'" @click="toggle(20)">
            <span class="arrow" :class="{ 'is-collapsed': !blocks[19].open }"></span>
          </button>
          <button class="icon-btn" title="删除" @click="remove(20)">
            <span class="close"></span>
          </button>
        </div>
      </div>
      <div v-show="blocks[19].open" class="panel-body">
        <!-- TODO: 区块 20 的表单 / 表格 -->
      </div>
    </div>
  </div>
</template>

<style scoped>
.solo-page {
  padding: 20px;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.panel {
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  background: #fff;
  overflow: hidden;
}

.panel-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 14px;
  background: #f5f7fa;
  border-bottom: 1px solid #dcdfe6;
}

.panel-title {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
}

.panel-actions {
  display: flex;
  align-items: center;
  gap: 10px;
}

.icon-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 24px;
  height: 24px;
  padding: 0;
  border: none;
  border-radius: 4px;
  background: transparent;
  cursor: pointer;
}

.icon-btn:hover {
  background: #e4e7ed;
}

/* 折叠箭头：默认向上（展开态），加 .is-collapsed 后向下 */
.arrow {
  width: 0;
  height: 0;
  border-left: 6px solid transparent;
  border-right: 6px solid transparent;
  border-bottom: 7px solid #909399;
  transition: transform 0.2s;
}

.arrow.is-collapsed {
  transform: rotate(180deg);
}

/* 删除叉号 */
.close {
  position: relative;
  width: 14px;
  height: 14px;
}

.close::before,
.close::after {
  content: '';
  position: absolute;
  top: 6px;
  left: 0;
  width: 14px;
  height: 2px;
  background: #909399;
}

.close::before {
  transform: rotate(45deg);
}

.close::after {
  transform: rotate(-45deg);
}

.icon-btn:hover .arrow {
  border-bottom-color: #409eff;
}

.icon-btn:hover .close::before,
.icon-btn:hover .close::after {
  background: #f56c6c;
}

.panel-body {
  padding: 14px;
  font-size: 14px;
  line-height: 1.7;
  color: #606266;
}

/* 工艺编制表格 */
.grid-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 14px;
  color: #606266;
  table-layout: fixed;
}

.grid-table th,
.grid-table td {
  border: 1px solid #dcdfe6;
  padding: 8px 12px;
  text-align: left;
  line-height: 1.6;
  word-break: break-all;
}

/* 行标题（项目 / 内容 / 备注） */
.grid-table tbody th {
  width: 88px;
  background: #f5f7fa;
  color: #303133;
  font-weight: 600;
  white-space: nowrap;
}

.grid-table tbody tr:hover td {
  background: #f5f7fa;
}
</style>
