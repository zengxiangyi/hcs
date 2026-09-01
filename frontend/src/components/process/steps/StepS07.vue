<script setup lang="ts">
import { ref } from 'vue'
import type { StepForm } from './shared'

defineOptions({ name: 'StepS07' })

const form = defineModel<StepForm>({ required: true })
const formRef = ref()

const rules = {
  part: [{ required: true, message: '请输入测量部位', trigger: 'blur' }],
  value: [{ required: true, message: '请输入变形量', trigger: 'blur' }],
  allow: [{ required: true, message: '请输入允许范围', trigger: 'blur' }]
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
        <el-form-item label="测量部位" prop="part">
          <el-input v-model="form.part" placeholder="如：辊身中部" />
        </el-form-item>
      </el-col>
      <el-col :span="12">
        <el-form-item label="变形量(mm)" prop="value">
          <el-input v-model="form.value" placeholder="请输入实测变形量" />
        </el-form-item>
      </el-col>
      <el-col :span="12">
        <el-form-item label="允许范围(mm)" prop="allow">
          <el-input v-model="form.allow" placeholder="如：≤0.05" />
        </el-form-item>
      </el-col>
      <el-col :span="12">
        <el-form-item label="测量工具" prop="tool">
          <el-input v-model="form.tool" placeholder="如：百分表" />
        </el-form-item>
      </el-col>
      <el-col :span="12">
        <el-form-item label="测量人" prop="operator">
          <el-input v-model="form.operator" placeholder="请输入测量人" />
        </el-form-item>
      </el-col>
    </el-row>
  </el-form>
</template>
