<script setup lang="ts">
import { ref } from 'vue'
import type { StepForm } from './shared'

defineOptions({ name: 'StepS14' })

const form = defineModel<StepForm>({ required: true })
const formRef = ref()

const rules = {
  method: [{ required: true, message: '请选择包装方式', trigger: 'change' }],
  antirust: [{ required: true, message: '请输入防锈处理', trigger: 'blur' }],
  label: [{ required: true, message: '请输入标识内容', trigger: 'blur' }]
}

const methodOptions = ['木箱', '托盘', '裸装', '缠绕膜']

async function validate() {
  if (!formRef.value) return false
  try {
    await formRef.value.validate()
    return true
  } catch {
    return false
  }
}

function reset() {
  formRef.value?.resetFields()
}

defineExpose({ validate, reset })
</script>

<template>
  <el-form ref="formRef" :model="form" :rules="rules" label-width="120px">
    <el-row :gutter="16">
      <el-col :span="12">
        <el-form-item label="包装方式" prop="method">
          <el-select v-model="form.method" placeholder="请选择包装方式" style="width: 100%">
            <el-option v-for="m in methodOptions" :key="m" :label="m" :value="m" />
          </el-select>
        </el-form-item>
      </el-col>
      <el-col :span="12">
        <el-form-item label="防锈处理" prop="antirust">
          <el-input v-model="form.antirust" placeholder="如：涂防锈油" />
        </el-form-item>
      </el-col>
      <el-col :span="12">
        <el-form-item label="标识内容" prop="label">
          <el-input v-model="form.label" placeholder="如：辊号 / 规格 / 客户" />
        </el-form-item>
      </el-col>
      <el-col :span="12">
        <el-form-item label="包装责任人" prop="owner">
          <el-input v-model="form.owner" placeholder="请输入包装责任人" />
        </el-form-item>
      </el-col>
    </el-row>
    <el-form-item label="备注" prop="remark">
      <el-input v-model="form.remark" type="textarea" :rows="3" placeholder="请填写包装备注" />
    </el-form-item>
  </el-form>
</template>
