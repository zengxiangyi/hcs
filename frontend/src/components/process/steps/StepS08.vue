<script setup lang="ts">
import { ref } from 'vue'
import type { StepForm } from './shared'

defineOptions({ name: 'StepS08' })

const form = defineModel<StepForm>({ required: true })
const formRef = ref()

const rules = {
  temp: [{ required: true, message: '请输入焖火温度', trigger: 'blur' }],
  duration: [{ required: true, message: '请输入焖火时长', trigger: 'blur' }],
  method: [{ required: true, message: '请选择焖火方式', trigger: 'change' }]
}

const methodOptions = ['炉焖', '砂焖', '坑焖']

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
        <el-form-item label="焖火温度(℃)" prop="temp">
          <el-input-number v-model="form.temp" :min="0" :max="1200" style="width: 100%" />
        </el-form-item>
      </el-col>
      <el-col :span="12">
        <el-form-item label="焖火时长(h)" prop="duration">
          <el-input-number v-model="form.duration" :min="0" style="width: 100%" />
        </el-form-item>
      </el-col>
      <el-col :span="12">
        <el-form-item label="焖火方式" prop="method">
          <el-select v-model="form.method" placeholder="请选择焖火方式" style="width: 100%">
            <el-option v-for="m in methodOptions" :key="m" :label="m" :value="m" />
          </el-select>
        </el-form-item>
      </el-col>
    </el-row>
    <el-form-item label="备注" prop="remark">
      <el-input v-model="form.remark" type="textarea" :rows="3" placeholder="请填写暂焖备注" />
    </el-form-item>
  </el-form>
</template>
