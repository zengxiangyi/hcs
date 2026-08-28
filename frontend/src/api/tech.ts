import type { BluePrintListParams, BluePrintListResult } from './blueprint'
import http from './http'
import type { ApiResponse } from './http'

/** 编制模板动态表格行 */
export interface TechTemplateRow {
  segNo: string
  temp: string
  time: string
  remark: string
}

/** 工艺看板保存入参 */
export interface TechBoardSaveDTO {
  /** 蓝本工艺编号 */
  code: string
  /** 工艺名称 */
  name: string
  /** 图号 */
  graph: string
  /** 一级工艺 */
  firstLevel: string
  /** 二级工艺 */
  secondLevel: string
  /** 物料名称 */
  materialName: string
  /** 物料编码 */
  materialCode: string
  /** 单重 */
  weight: string
  /** 规格 */
  model: string
  /** 材质 */
  specs: string
  /** 客户名称 */
  customer: string
  /** 工艺备注 */
  remark: string
  /** 是否首检 */
  isFirstCheck: string
  /** 测点数量 */
  testNum: string
  /** 冷却时间(min) */
  coolTime: string
  /** 母线数量 */
  busbarNum: string
  /** 身颈落差 */
  fallHead: string
  /** 淬火部位 */
  quenching: string
  /** 注意事项 */
  attention: string
  /** 辊身倒角 */
  chamfer: string
  /** 完工检硬度要求 */
  lastHardness: string
  /** 首检硬度要求 */
  firstHardness: string
  /** 硬化层深度(mm) */
  hardnessDepth: string
}

export const techAPI = {
  /** 保存工艺看板 */
  save: (data: TechBoardSaveDTO): Promise<ApiResponse<TechBoardSaveDTO>> =>
    http.post<TechBoardSaveDTO>('/api/blueprint/save', data) as Promise<ApiResponse<TechBoardSaveDTO>>,

    /** 保存工艺看板 */
  submit: (data: TechBoardSaveDTO): Promise<ApiResponse<TechBoardSaveDTO>> =>
    http.post<TechBoardSaveDTO>('/api/blueprint/submit', data) as Promise<ApiResponse<TechBoardSaveDTO>>,

  /** 获取工艺看板（按 id） */
  get: (id: number): Promise<ApiResponse<TechBoardSaveDTO>> =>
    http.get<TechBoardSaveDTO>(`/api/blueprint/${id}`) as Promise<ApiResponse<TechBoardSaveDTO>>,

  /** 查看草稿（按 code和edition 加载蓝本编辑信息） */
  getByCode: (code: string,edition: string): Promise<ApiResponse<TechBoardSaveDTO>> =>
    http.get<TechBoardSaveDTO>(`/api/blueprint/code/${code}/${edition}`) as Promise<ApiResponse<TechBoardSaveDTO>>,

  /** 查看草稿列表 */
  getList: (params?: BluePrintListParams): Promise<ApiResponse<BluePrintListResult>> =>
    http.get<BluePrintListResult>('/api/blueprint/list', { params }) as Promise<ApiResponse<BluePrintListResult>>,

}
