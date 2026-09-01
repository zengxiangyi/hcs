export type StepForm = Record<string, any>

/** 每个步骤表单组件对外暴露的能力 */
export interface StepFormExpose {
  /** 校验当前步骤表单，返回是否通过 */
  validate: () => Promise<boolean>
  /** 重置当前步骤表单 */
  reset: () => void
}
