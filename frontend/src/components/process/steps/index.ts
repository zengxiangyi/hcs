import type { Component } from 'vue'
import StepS01 from './StepS01.vue'
import StepS02 from './StepS02.vue'
import StepS03 from './StepS03.vue'
import StepS04 from './StepS04.vue'
import StepS05 from './StepS05.vue'
import StepS06 from './StepS06.vue'
import StepS07 from './StepS07.vue'
import StepS08 from './StepS08.vue'
import StepS09 from './StepS09.vue'
import StepS10 from './StepS10.vue'
import StepS11 from './StepS11.vue'
import StepS12 from './StepS12.vue'
import StepS13 from './StepS13.vue'
import StepS14 from './StepS14.vue'
import StepS15 from './StepS15.vue'
import StepS16 from './StepS16.vue'

/** 步骤 key -> 对应表单组件 */
export const stepFormMap: Record<string, Component> = {
  s01: StepS01,
  s02: StepS02,
  s03: StepS03,
  s04: StepS04,
  s05: StepS05,
  s06: StepS06,
  s07: StepS07,
  s08: StepS08,
  s09: StepS09,
  s10: StepS10,
  s11: StepS11,
  s12: StepS12,
  s13: StepS13,
  s14: StepS14,
  s15: StepS15,
  s16: StepS16
}
