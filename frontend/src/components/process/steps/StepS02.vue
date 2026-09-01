<script setup lang="ts">
import { ref } from 'vue'
import type { StepForm } from './shared'

defineOptions({ name: 'StepS02' })

const form = defineModel<StepForm>({ required: true })
const formRef = ref()

const rules = {
  position: [{ required: true, message: '请输入检测位置', trigger: 'blur' }],
  require: [{ required: true, message: '请输入硬度要求', trigger: 'blur' }],
  actual: [{ required: true, message: '请输入实测值', trigger: 'blur' }],
  result: [{ required: true, message: '请选择判定结论', trigger: 'change' }]
}

const resultOptions = ['合格', '不合格', '待复检']

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
        <el-form-item label="检测位置" prop="position">
          <el-input v-model="form.position" placeholder="如：辊颈 A 端" />
        </el-form-item>
      </el-col>
      <el-col :span="12">
        <el-form-item label="硬度要求(HRC)" prop="require">
          <el-input v-model="form.require" placeholder="如：58~62" />
        </el-form-item>
      </el-col>
      <el-col :span="12">
        <el-form-item label="实测值(HRC)" prop="actual">
          <el-input v-model="form.actual" placeholder="请输入实测硬度值" />
        </el-form-item>
      </el-col>
      <el-col :span="12">
        <el-form-item label="检测工具" prop="tool">
          <el-input v-model="form.tool" placeholder="如：里氏硬度计" />
        </el-form-item>
      </el-col>
      <el-col :span="12">
        <el-form-item label="检测人" prop="operator">
          <el-input v-model="form.operator" placeholder="请输入检测人" />
        </el-form-item>
      </el-col>
      <el-col :span="12">
        <el-form-item label="判定结论" prop="result">
          <el-select v-model="form.result" placeholder="请选择判定结论" style="width: 100%">
            <el-option v-for="r in resultOptions" :key="r" :label="r" :value="r" />
          </el-select>
        </el-form-item>
      </el-col>
    </el-row>
    <el-form-item label="备注" prop="remark">
      <el-input v-model="form.remark" type="textarea" :rows="3" placeholder="请填写检测备注" />
    </el-form-item>
  </el-form>
</template>
