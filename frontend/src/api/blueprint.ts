import http from './http'

/** 蓝本清单行 */
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

/** 蓝本列表查询参数 */
export interface BluePrintListParams {
  code?: string
  name?: string
  state?: string
  firstLevel?: string
  secondLevel?: string
  page?: number
  pageSize?: number
}

/**蓝本列表返回 */
export interface BluePrintListResult {
  content: BluePrintRow[]
  total: number
  page: number
  pageSize: number
}

export const blueprintAPI = {
  /** 蓝本清单列表 */
  getList: (params?: BluePrintListParams) =>
    http.get<BluePrintListResult>('/api/blueprint/list', { params }),


  search: (params?: BluePrintListParams) =>
    http.post<BluePrintListResult>('/api/blueprint/search', { params }),



  /** 删除蓝本 */
  delete: (code: string, edition: string) => http.delete<null>(`/api/blueprint/${code}/${edition}`),
}
