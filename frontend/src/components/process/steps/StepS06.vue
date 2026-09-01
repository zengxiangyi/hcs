<script setup lang="ts">
import { ref } from 'vue'
import type { StepForm } from './shared'

defineOptions({ name: 'StepS06' })

const form = defineModel<StepForm>({ required: true })
const formRef = ref()

const rules = {
  item: [{ required: true, message: '请输入检验项目', trigger: 'blur' }],
  standard: [{ required: true, message: '请输入标准要求', trigger: 'blur' }],
  inspector: [{ required: true, message: '请输入检验员', trigger: 'blur' }],
  result: [{ required: true, message: '请选择判定结果', trigger: 'change' }]
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
        <el-form-item label="检验项目" prop="item">
          <el-input v-model="form.item" placeholder="如：表面硬度 / 外观" />
        </el-form-item>
      </el-col>
      <el-col :span="12">
        <el-form-item label="标准要求" prop="standard">
          <el-input v-model="form.standard" placeholder="请输入标准或图纸要求" />
        </el-form-item>
      </el-col>
      <el-col :span="12">
        <el-form-item label="实测值" prop="actual">
          <el-input v-model="form.actual" placeholder="请输入实测值" />
        </el-form-item>
      </el-col>
      <el-col :span="12">
        <el-form-item label="检验员" prop="inspector">
          <el-input v-model="form.inspector" placeholder="请输入检验员" />
        </el-form-item>
      </el-col>
      <el-col :span="12">
        <el-form-item label="判定结果" prop="result">
          <el-select v-model="form.result" placeholder="请选择判定结果" style="width: 100%">
            <el-option v-for="r in resultOptions" :key="r" :label="r" :value="r" />
          </el-select>
        </el-form-item>
      </el-col>
    </el-row>
    <el-form-item label="备注" prop="remark">
      <el-input v-model="form.remark" type="textarea" :rows="3" placeholder="请填写首检备注" />
    </el-form-item>
  </el-form>
</template>
