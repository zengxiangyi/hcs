<script setup lang="ts">
import { ref } from 'vue'
import type { StepForm } from './shared'

defineOptions({ name: 'StepS03' })

const form = defineModel<StepForm>({ required: true })
const formRef = ref()

const rules = {
  furnace: [{ required: true, message: '请输入炉号', trigger: 'blur' }],
  temp: [{ required: true, message: '请输入预热温度', trigger: 'blur' }],
  hold: [{ required: true, message: '请输入保温时间', trigger: 'blur' }]
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
        <el-form-item label="炉号" prop="furnace">
          <el-input v-model="form.furnace" placeholder="请输入箱式炉炉号" />
        </el-form-item>
      </el-col>
      <el-col :span="12">
        <el-form-item label="预热温度(℃)" prop="temp">
          <el-input-number v-model="form.temp" :min="0" :max="1200" style="width: 100%" />
        </el-form-item>
      </el-col>
      <el-col :span="12">
        <el-form-item label="升温速率(℃/h)" prop="rate">
          <el-input-number v-model="form.rate" :min="0" style="width: 100%" />
        </el-form-item>
      </el-col>
      <el-col :span="12">
        <el-form-item label="保温时间(min)" prop="hold">
          <el-input-number v-model="form.hold" :min="0" style="width: 100%" />
        </el-form-item>
      </el-col>
      <el-col :span="12">
        <el-form-item label="炉内气氛" prop="atmosphere">
          <el-input v-model="form.atmosphere" placeholder="如：空气 / 保护气氛" />
        </el-form-item>
      </el-col>
    </el-row>
    <el-form-item label="备注" prop="remark">
      <el-input v-model="form.remark" type="textarea" :rows="3" placeholder="请填写预热备注" />
    </el-form-item>
  </el-form>
</template>
