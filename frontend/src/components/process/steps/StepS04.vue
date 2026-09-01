<script setup lang="ts">
import { ref } from 'vue'
import type { StepForm } from './shared'

defineOptions({ name: 'StepS04' })

const form = defineModel<StepForm>({ required: true })
const formRef = ref()

const rules = {
  machine: [{ required: true, message: '请输入机床编号', trigger: 'blur' }],
  temp: [{ required: true, message: '请输入加热温度', trigger: 'blur' }],
  medium: [{ required: true, message: '请选择冷却介质', trigger: 'change' }]
}

const mediumOptions = ['水冷', '油冷', '聚合物', '空冷']

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
        <el-form-item label="机床编号" prop="machine">
          <el-input v-model="form.machine" placeholder="请输入淬火机床编号" />
        </el-form-item>
      </el-col>
      <el-col :span="12">
        <el-form-item label="加热温度(℃)" prop="temp">
          <el-input-number v-model="form.temp" :min="0" :max="1200" style="width: 100%" />
        </el-form-item>
      </el-col>
      <el-col :span="12">
        <el-form-item label="冷却介质" prop="medium">
          <el-select v-model="form.medium" placeholder="请选择冷却介质" style="width: 100%">
            <el-option v-for="m in mediumOptions" :key="m" :label="m" :value="m" />
          </el-select>
        </el-form-item>
      </el-col>
      <el-col :span="12">
        <el-form-item label="主轴转速(r/min)" prop="speed">
          <el-input-number v-model="form.speed" :min="0" style="width: 100%" />
        </el-form-item>
      </el-col>
      <el-col :span="12">
        <el-form-item label="进给量(mm/r)" prop="feed">
          <el-input v-model="form.feed" placeholder="请输入进给量" />
        </el-form-item>
      </el-col>
      <el-col :span="12">
        <el-form-item label="淬火时长(s)" prop="time">
          <el-input-number v-model="form.time" :min="0" style="width: 100%" />
        </el-form-item>
      </el-col>
    </el-row>
    <el-form-item label="备注" prop="remark">
      <el-input v-model="form.remark" type="textarea" :rows="3" placeholder="请填写淬火备注" />
    </el-form-item>
  </el-form>
</template>
