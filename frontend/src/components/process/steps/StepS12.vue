<script setup lang="ts">
import { ref } from 'vue'
import type { StepForm } from './shared'

defineOptions({ name: 'StepS12' })

const form = defineModel<StepForm>({ required: true })
const formRef = ref()

const rules = {
  method: [{ required: true, message: '请选择矫直方式', trigger: 'change' }],
  runout: [{ required: true, message: '请输入矫直后跳动', trigger: 'blur' }],
  operator: [{ required: true, message: '请输入操作人', trigger: 'blur' }]
}

const methodOptions = ['压力矫直', '火焰矫直', '辊式矫直']

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
        <el-form-item label="矫直方式" prop="method">
          <el-select v-model="form.method" placeholder="请选择矫直方式" style="width: 100%">
            <el-option v-for="m in methodOptions" :key="m" :label="m" :value="m" />
          </el-select>
        </el-form-item>
      </el-col>
      <el-col :span="12">
        <el-form-item label="矫直压力(t)" prop="force">
          <el-input-number v-model="form.force" :min="0" style="width: 100%" />
        </el-form-item>
      </el-col>
      <el-col :span="12">
        <el-form-item label="矫直次数" prop="times">
          <el-input-number v-model="form.times" :min="0" style="width: 100%" />
        </el-form-item>
      </el-col>
      <el-col :span="12">
        <el-form-item label="矫直后跳动(mm)" prop="runout">
          <el-input v-model="form.runout" placeholder="请输入矫直后跳动值" />
        </el-form-item>
      </el-col>
      <el-col :span="12">
        <el-form-item label="操作人" prop="operator">
          <el-input v-model="form.operator" placeholder="请输入操作人" />
        </el-form-item>
      </el-col>
    </el-row>
    <el-form-item label="备注" prop="remark">
      <el-input v-model="form.remark" type="textarea" :rows="3" placeholder="请填写矫直备注" />
    </el-form-item>
  </el-form>
</template>
