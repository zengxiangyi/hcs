import http from './http'

/** 调拨单查询参数 */
export interface TransferQueryParams {
  code?: string
  name?: string
  state?: string
  page?: number
  pageSize?: number
}

/** 调拨单列表返回 */
export interface TransferListResult {
  content: TransferRow[]
  total: number
  page: number
  pageSize: number
}

/** 调拨单行（与后端表结构对齐） */
export interface TransferRow {
  id: number
  code: string           // 编码（调拨单号）
  name: string           // 名称（产品名称）
  category: string       // 调拨类型
  datetime: string       // 调拨时间
  materialCode: string   // 物料编码
  num: number            // 数量
  weight: number         // 单重
  material: string       // 材质
  rollNum: string        // 辊号
  outProcess: string     // 调出工序组
  inProcess: string      // 调入工序组
  outRoom: string        // 调出仓库
  inRoom: string         // 调入仓库
  remark: string         // 急件说明
  prompt: string         // 质量提示
  quenching: string      // 淬火设备
  supplier: string       // 供应商
  createUser: string     // 创建人
  createTime: string     // 创建时间
  receiveUser: string    // 接收人
  receiveTime: string    // 接收时间
  state: string          // 状态
}

/** 蓝本绑定入参 */
export interface BindBlueprintParams {
  transferId: number
  blueprintId: number
}

/** 新增调拨单入参（与后端表结构对齐） */
export interface TransferCreateParams {
  id?: number            // 调拨单id
  code: string           // 编码（调拨单号）
  name: string           // 名称（产品名称）
  category: string       // 调拨类型
  transferDate: string   // 调拨日期
  materialCode: string   // 物料编码
  num: number            // 数量
  weight: number         // 单重
  material: string       // 材质
  rollNum: string        // 辊号
  outProcess: string     // 调出工序组
  inProcess: string      // 调入工序组
  outRoom: string        // 调出仓库
  inRoom: string         // 调入仓库
  remark: string         // 急件说明
  prompt: string         // 质量提示
  quenching: string      // 淬火设备
  supplier: string       // 供应商
  createUser: string     // 创建人
  createTime: string     // 创建日期
  state: string          // 状态
}

/** 调拨单接口（后端接口路径为约定，待后端实现后生效） */
export const transferAPI = {
  /** 调拨单列表（服务端分页） */
  search: (params?: TransferQueryParams) =>
    http.post<TransferListResult>('/api/transfer/search', params),

  /** 新增调拨单 */
  create: (data: TransferCreateParams) =>
    http.post<null>('/api/transfer/save', data),

  /** 绑定蓝本到调拨单 */
  bindBlueprint: (data: BindBlueprintParams) =>
    http.post<  null>('/api/transfer/bind', data),

  /** 编辑调拨单（按 id 更新） */
  update: (data: TransferCreateParams) =>
    http.post<null>('/api/transfer/update', data),

  /** 删除调拨单（按 id） */
  delete: (id: number) =>
    http.delete<null>(`/api/transfer/${id}`),
}
