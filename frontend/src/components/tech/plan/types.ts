/**
 * 工艺编制方案组件的公共契约。
 *
 * 每个具体方案组件（如 TZ01.vue）通过 defineModel<PlanModel>() 双向绑定自身数据，
 * 父组件 board.vue 在保存时把 basicForm / requirementForm / planModel 合并提交。
 */

/** 方案组件对外暴露的数据模型：key 为字段名，value 为任意可序列化值 */
export interface PlanModel {
  /** 方案自身的表单字段（不同方案字段差异很大） */
  fields: Record<string, any>
  /** 方案自身的编制明细表（行结构由方案自行定义） */
  rows: Record<string, any>[]
}

/** 构建一个空的方案数据模型，供父组件初始化使用 */
export function createEmptyPlan(): PlanModel {
  return { fields: {}, rows: [] }
}
