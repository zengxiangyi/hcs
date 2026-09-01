<script setup lang="ts">
import { ref } from 'vue'
import type { StepForm } from './shared'

defineOptions({ name: 'StepS10' })

const form = defineModel<StepForm>({ required: true })
const formRef = ref()

const rules = {
  furnace: [{ required: true, message: '请输入回火炉号', trigger: 'blur' }],
  temp: [{ required: true, message: '请输入回火温度', trigger: 'blur' }],
  hold: [{ required: true, message: '请输入保温时间', trigger: 'blur' }],
  times: [{ required: true, message: '请输入回火次数', trigger: 'blur' }]
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
        <el-form-item label="回火炉号" prop="furnace">
          <el-input v-model="form.furnace" placeholder="请输入回火炉号" />
        </el-form-item>
      </el-col>
      <el-col :span="12">
        <el-form-item label="回火温度(℃)" prop="temp">
          <el-input-number v-model="form.temp" :min="0" :max="1200" style="width: 100%" />
        </el-form-item>
      </el-col>
      <el-col :span="12">
        <el-form-item label="保温时间(h)" prop="hold">
          <el-input-number v-model="form.hold" :min="0" style="width: 100%" />
        </el-form-item>
      </el-col>
      <el-col :span="12">
        <el-form-item label="回火次数" prop="times">
          <el-input-number v-model="form.times" :min="1" :max="10" style="width: 100%" />
        </el-form-item>
      </el-col>
      <el-col :span="12">
        <el-form-item label="出炉温度(℃)" prop="outTemp">
          <el-input-number v-model="form.outTemp" :min="0" style="width: 100%" />
        </el-form-item>
      </el-col>
    </el-row>
    <el-form-item label="备注" prop="remark">
      <el-input v-model="form.remark" type="textarea" :rows="3" placeholder="请填写辊身回火备注" />
    </el-form-item>
  </el-form>
</template>
