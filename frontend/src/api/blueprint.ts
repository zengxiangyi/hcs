import http from './http'

/** 蓝本行（对齐后端 BluePrint 实体 / blueprint 表） */
export interface BluePrintRow {
  id: number
  code: string
  name: string
  graph: string
  firstLevel: string
  secondLevel: string
  materialName: string
  weight: number
  materialCode: string
  isFirstCheck: string
  category: string
  busbarNum: string
  testNum: string
  coolTime: string
  hardendeep: string
  chamfer: string
  fallHead: string
  quenching: string
  attention: string
  model: string
  specs: string
  customer: string
  edition: string
  state: string
  remark: string
  createTime: string
  createUser: string
}

/** 蓝本查询参数（POST /search 请求体，分页 1 基） */
export interface BluePrintListParams {
  code?: string
  name?: string
  state?: string
  firstLevel?: string
  secondLevel?: string
  page?: number
  pageSize?: number
}

/** 蓝本列表返回 */
export interface BluePrintListResult {
  content: BluePrintRow[]
  total: number
  page: number
  pageSize: number
}

/** 编制模板动态表格行 */
export interface TechTemplateRow {
  segNo: string
  temp: string
  time: string
  remark: string
}

/** 工艺看板保存入参（工艺看板页面的蓝本编辑结构） */
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

/** 蓝本接口（BluePrintController /blueprint） */
export const blueprintAPI = {
  /** 蓝本清单列表（POST 请求体查询，服务端分页） */
  search: (params?: BluePrintListParams) =>
    http.post<BluePrintListResult>('/api/blueprint/search', params),

  /** 新增蓝本（后端固定 edition=V1、state=A） */
  save: (data: TechBoardSaveDTO) =>
    http.post<BluePrintRow>('/api/blueprint/save', data),

  /** 修改蓝本（id 由请求体携带；submit 复用此接口） */
  update: (data: Partial<TechBoardSaveDTO> & { id: number }) =>
    http.put<BluePrintRow>('/api/blueprint/update', data),

  /** 提交蓝本（后端无独立 submit 端点，复用 PUT /update） */
  submit: (data: Partial<TechBoardSaveDTO> & { id: number }) =>
    http.put<BluePrintRow>('/api/blueprint/update', data),

  /** 按蓝本编号查询全部版本 */
  getByCode: (code: string) =>
    http.get<BluePrintRow[]>(`/api/blueprint/code/${code}`),

  /** 按编号 + 版本查询（原 techAPI.getByCode） */
  getByCodeAndEdition: (code: string, edition: string) =>
    http.get<BluePrintRow>(`/api/blueprint/code/${code}/${edition}`),

  /** 删除蓝本（按编号 + 版本） */
  remove: (code: string, edition: string) =>
    http.delete<string>(`/api/blueprint/code/${code}/${edition}`),
}
