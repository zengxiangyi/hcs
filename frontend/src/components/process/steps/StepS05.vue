<script setup lang="ts">
import { ref } from 'vue'
import type { StepForm } from './shared'

defineOptions({ name: 'StepS05' })

const form = defineModel<StepForm>({ required: true })
const formRef = ref()

const rules = {
  method: [{ required: true, message: '请选择冷却方式', trigger: 'change' }],
  duration: [{ required: true, message: '请输入冷却时长', trigger: 'blur' }],
  endTemp: [{ required: true, message: '请输入终冷温度', trigger: 'blur' }]
}

const methodOptions = ['空冷', '风冷', '雾冷', '油冷']

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
        <el-form-item label="冷却方式" prop="method">
          <el-select v-model="form.method" placeholder="请选择冷却方式" style="width: 100%">
            <el-option v-for="m in methodOptions" :key="m" :label="m" :value="m" />
          </el-select>
        </el-form-item>
      </el-col>
      <el-col :span="12">
        <el-form-item label="冷却时长(min)" prop="duration">
          <el-input-number v-model="form.duration" :min="0" style="width: 100%" />
        </el-form-item>
      </el-col>
      <el-col :span="12">
        <el-form-item label="终冷温度(℃)" prop="endTemp">
          <el-input-number v-model="form.endTemp" style="width: 100%" />
        </el-form-item>
      </el-col>
      <el-col :span="12">
        <el-form-item label="环境温度(℃)" prop="envTemp">
          <el-input-number v-model="form.envTemp" style="width: 100%" />
        </el-form-item>
      </el-col>
    </el-row>
    <el-form-item label="备注" prop="remark">
      <el-input v-model="form.remark" type="textarea" :rows="3" placeholder="请填写续冷备注" />
    </el-form-item>
  </el-form>
</template>
