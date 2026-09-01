<script setup lang="ts">
import { ref } from 'vue'
import type { StepForm } from './shared'

defineOptions({ name: 'StepS01' })

const form = defineModel<StepForm>({ required: true })
const formRef = ref()

const rules = {
  code: [{ required: true, message: '请输入工序编号', trigger: 'blur' }],
  name: [{ required: true, message: '请输入工序名称', trigger: 'blur' }],
  type: [{ required: true, message: '请选择工序类型', trigger: 'change' }]
}

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
        <el-form-item label="客户" prop="code">
          <el-input v-model="form.code" placeholder="请输入客户名称" />
        </el-form-item>
      </el-col>
      <el-col :span="12">
        <el-form-item label="图号" prop="name">
          <el-input v-model="form.name" placeholder="请输入图号" />
        </el-form-item>
      </el-col>
      <el-col :span="12">
        <el-form-item label="辊号" prop="type">
          <el-input v-model="form.name" placeholder="请输入辊号" />
        </el-form-item>
      </el-col>
      <el-col :span="12">
        <el-form-item label="材质" prop="product">
          <el-input v-model="form.product" placeholder="请输入材质" />
        </el-form-item>
      </el-col>
      <el-col :span="12">
        <el-form-item label="要求" prop="version">
          <el-input v-model="form.version" placeholder="如 V1.0" />
        </el-form-item>
      </el-col>
      <el-col :span="12">
        <el-form-item label="单重" prop="version">
          <el-input v-model="form.version" placeholder="如 V1.0" />
        </el-form-item>
      </el-col>
      <el-col :span="12">
        <el-form-item label="主要外形尺寸" prop="version">
          <el-input v-model="form.version" placeholder="如 V1.0" />
        </el-form-item>
      </el-col>
    </el-row>
  </el-form>
</template>
