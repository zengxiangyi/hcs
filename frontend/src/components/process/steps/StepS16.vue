<script setup lang="ts">
import { ref } from 'vue'
import type { StepForm } from './shared'

defineOptions({ name: 'StepS16' })

const form = defineModel<StepForm>({ required: true })
const formRef = ref()

const rules = {
  points: [{ required: true, message: '请输入检测点数', trigger: 'blur' }],
  actual: [{ required: true, message: '请输入硬度实测值', trigger: 'blur' }],
  report: [{ required: true, message: '请输入检测报告编号', trigger: 'blur' }],
  result: [{ required: true, message: '请选择判定结果', trigger: 'change' }],
  inspector: [{ required: true, message: '请输入检测人', trigger: 'blur' }]
}

const resultOptions = ['合格', '不合格', '让步接收']

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
        <el-form-item label="检测点数" prop="points">
          <el-input-number v-model="form.points" :min="1" style="width: 100%" />
        </el-form-item>
      </el-col>
      <el-col :span="12">
        <el-form-item label="硬度实测值(HRC)" prop="actual">
          <el-input v-model="form.actual" placeholder="如：60.5" />
        </el-form-item>
      </el-col>
      <el-col :span="12">
        <el-form-item label="检测报告编号" prop="report">
          <el-input v-model="form.report" placeholder="请输入检测报告编号" />
        </el-form-item>
      </el-col>
      <el-col :span="12">
        <el-form-item label="判定结果" prop="result">
          <el-select v-model="form.result" placeholder="请选择判定结果" style="width: 100%">
            <el-option v-for="r in resultOptions" :key="r" :label="r" :value="r" />
          </el-select>
        </el-form-item>
      </el-col>
      <el-col :span="12">
        <el-form-item label="检测人" prop="inspector">
          <el-input v-model="form.inspector" placeholder="请输入检测人" />
        </el-form-item>
      </el-col>
    </el-row>
    <el-form-item label="备注" prop="remark">
      <el-input v-model="form.remark" type="textarea" :rows="3" placeholder="请填写硬度检测备注" />
    </el-form-item>
  </el-form>
</template>
