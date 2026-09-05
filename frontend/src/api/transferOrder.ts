import http from './http'

/** 调拨单查询参数（POST /search 请求体，分页 1 基） */
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

/** 新增/编辑调拨单入参（与后端表结构对齐） */
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

/** 调拨单行（与后端表结构对齐，含接收信息） */
export interface TransferRow extends TransferCreateParams {
  id: number             // 调拨单id
  receiveUser: string    // 接收人
  receiveTime: string    // 接收时间
}

/** 调拨单接口（TransferOrderController /transfer） */
export const transferAPI = {
  /** 调拨单列表（服务端分页） */
  search: (params?: TransferQueryParams) =>
    http.post<TransferListResult>('/api/transfer/search', params),

  /** 分页列表（query 参数 page/size） */
  list: (page = 1, size = 10) =>
    http.get<TransferListResult>(`/api/transfer/list?page=${page}&size=${size}`),

  /** 按 id 查询 */
  get: (id: number) => http.get<TransferRow>(`/api/transfer/${id}`),

  /** 按调拨单号查询（可能多行） */
  getByCode: (code: string) => http.get<TransferRow[]>(`/api/transfer/code/${code}`),

  /** 新增调拨单 */
  create: (data: TransferCreateParams) =>
    http.post<TransferRow>('/api/transfer/save', data),

  /** 编辑调拨单（id 由请求体携带） */
  update: (data: TransferCreateParams) =>
    http.put<TransferRow>('/api/transfer/update', data),

  /** 删除调拨单（按 id） */
  delete: (id: number) =>
    http.delete<string>(`/api/transfer/${id}`),
}
