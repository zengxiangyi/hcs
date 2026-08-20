import { createHash } from 'node:crypto'

/**
 * MD5 加密字符串，返回 32 位小写十六进制。
 *
 * 兼容性说明：
 * - 前端密码以 md5 小写 hex 提交，Node 内置 crypto.createHash('md5') 默认按 UTF-8 编码
 *   输入并输出小写 hex，与旧的手写实现结果一致，因此可安全替代，无需改动调用方。
 * - 仅用于登录/重置密码的简单一致性校验（seed.ts 中 md5('123456')），不涉及安全存储。
 * - 注意：MD5 已被攻破，切勿用于新系统的口令散列；此处仅因与既有前端约定保持兼容而保留。
 */
export function md5(str: string): string {
  return createHash('md5').update(str, 'utf8').digest('hex')
}
