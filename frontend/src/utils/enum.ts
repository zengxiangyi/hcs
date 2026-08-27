/**
 * 通用枚举映射工具
 * 将后端的 key（存储值）映射为前端展示文本 / 标签类型，多个页面可复用。
 */

/** el-tag 可用类型 */
export type TagType = '' | 'success' | 'warning' | 'info' | 'danger'

/** 状态枚举项：展示文本 + el-tag 颜色类型 */
export interface EnumStateItem {
  label: string
  type: TagType
}

/** 文本型枚举映射：key -> 展示文本 */
export type TextEnumMap = Record<string, string>
/** 状态型枚举映射：key -> { label, type } */
export type StateEnumMap = Record<string, EnumStateItem>

/**
 * 由文本型枚举映射生成一个格式化函数。
 * 未匹配时回退为原 key（可通过 fallback 覆盖）。
 */
export function createTextFormatter(
  map: TextEnumMap,
  fallback?: (key: string) => string,
): (key: string) => string {
  return (key: string) => map[key] ?? fallback?.(key) ?? key
}

/**
 * 由状态型枚举映射生成文本 / 类型两个格式化函数。
 * 文本未匹配回退为原 key；类型未匹配回退为 'warning'。
 */
export function createStateFormatter(map: StateEnumMap): {
  label: (key: string) => string
  type: (key: string) => TagType
} {
  return {
    label: (key: string) => map[key]?.label ?? key,
    type: (key: string) => map[key]?.type ?? 'warning',
  }
}
