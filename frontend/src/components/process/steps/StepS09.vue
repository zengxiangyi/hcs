<script setup lang="ts">
import { ref } from 'vue'
import type { StepForm } from './shared'

defineOptions({ name: 'StepS09' })

const form = defineModel<StepForm>({ required: true })
const formRef = ref()

const rules = {
  temp: [{ required: true, message: '请输入冷处理温度', trigger: 'blur' }],
  duration: [{ required: true, message: '请输入保温时长', trigger: 'blur' }],
  rewarm: [{ required: true, message: '请选择回温方式', trigger: 'change' }]
}

const rewarmOptions = ['自然回温', '水浴回温', '油浴回温']

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
        <el-form-item label="冷处理温度(℃)" prop="temp">
          <el-input-number v-model="form.temp" :max="0" style="width: 100%" />
        </el-form-item>
      </el-col>
      <el-col :span="12">
        <el-form-item label="保温时长(h)" prop="duration">
          <el-input-number v-model="form.duration" :min="0" style="width: 100%" />
        </el-form-item>
      </el-col>
      <el-col :span="12">
        <el-form-item label="冷却介质" prop="medium">
          <el-input v-model="form.medium" placeholder="如：液氮 / 低温箱" />
        </el-form-item>
      </el-col>
      <el-col :span="12">
        <el-form-item label="回温方式" prop="rewarm">
          <el-select v-model="form.rewarm" placeholder="请选择回温方式" style="width: 100%">
            <el-option v-for="r in rewarmOptions" :key="r" :label="r" :value="r" />
          </el-select>
        </el-form-item>
      </el-col>
    </el-row>
    <el-form-item label="备注" prop="remark">
      <el-input v-model="form.remark" type="textarea" :rows="3" placeholder="请填写冷处理备注" />
    </el-form-item>
  </el-form>
</template>
