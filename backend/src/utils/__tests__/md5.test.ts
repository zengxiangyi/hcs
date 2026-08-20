import { test } from 'node:test'
import assert from 'node:assert/strict'
import { md5 } from '../md5.js'

// 契约测试：md5 实现必须与前后端约定的历史值一致，
// 否则 seed 中的 admin 账号、前端提交的密码密文将无法比对。
test('md5 对空字符串返回预期值', () => {
  assert.equal(md5(''), 'd41d8cd98f00b204e9800998ecf8427e')
})

test('md5 对 seed 中的 123456 返回约定密文', () => {
  // 与 src/db/seed.ts 中 md5('123456') 必须一致，否则 admin/123456 无法登录
  assert.equal(md5('123456'), 'e10adc3949ba59abbe56e057f20f883e')
})

test('md5 输出稳定为 32 位小写十六进制', () => {
  const out = md5('Hello World')
  assert.match(out, /^[0-9a-f]{32}$/)
})

test('md5 对相同输入幂等', () => {
  assert.equal(md5('password'), md5('password'))
})
