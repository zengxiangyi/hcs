<script setup lang="ts">
import { ref } from 'vue'
import type { StepForm } from './shared'

defineOptions({ name: 'StepS15' })

const form = defineModel<StepForm>({ required: true })
const formRef = ref()

const rules = {
  order: [{ required: true, message: '请输入叫检单号', trigger: 'blur' }],
  caller: [{ required: true, message: '请输入叫检人', trigger: 'blur' }],
  time: [{ required: true, message: '请选择叫检时间', trigger: 'change' }],
  require: [{ required: true, message: '请输入硬度要求', trigger: 'blur' }]
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
        <el-form-item label="叫检单号" prop="order">
          <el-input v-model="form.order" placeholder="请输入叫检单号" />
        </el-form-item>
      </el-col>
      <el-col :span="12">
        <el-form-item label="叫检人" prop="caller">
          <el-input v-model="form.caller" placeholder="请输入叫检人" />
        </el-form-item>
      </el-col>
      <el-col :span="12">
        <el-form-item label="叫检时间" prop="time">
          <el-date-picker
            v-model="form.time"
            type="datetime"
            value-format="YYYY-MM-DD HH:mm:ss"
            placeholder="请选择叫检时间"
            style="width: 100%"
          />
        </el-form-item>
      </el-col>
      <el-col :span="12">
        <el-form-item label="硬度要求(HRC)" prop="require">
          <el-input v-model="form.require" placeholder="如：58~62" />
        </el-form-item>
      </el-col>
    </el-row>
    <el-form-item label="备注" prop="remark">
      <el-input v-model="form.remark" type="textarea" :rows="3" placeholder="请填写叫检备注" />
    </el-form-item>
  </el-form>
</template>
