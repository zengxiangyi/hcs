package com.baogang.info.common;

/**
 * 分页参数归一结果，供 Controller 入口统一使用。
 * <p>
 * 契约页码为 1 基（前端请求与响应一致），<b>Service 一律按 0 基页码工作</b>：
 * JPA 侧直接 {@code PageRequest.of(p, size)}，
 * MyBatis 侧由 Service 自行 {@code (long) p * size} 得到 LIMIT 偏移。
 * 转换只在这里做一次，Controller 一律取 {@link #page0()} 传给 Service，
 * Service 返回 PageResult 时再 +1 还原为契约 1 基页码。
 */
public record PageParam(int page, int size) {

    private static final int DEFAULT_PAGE = 1;
    private static final int DEFAULT_SIZE = 10;

    /** 0 基页码（= 契约页码 - 1），直接传给 Service */
    public int page0() {
        return page - 1;
    }

    /**
     * 分页参数归一：page/pageSize 为 null 或缺值时取默认 1/10；page 下限 1，
     * size 夹在 1~{@link ConstValue#MAX_PAGE_SIZE} 之间。
     * 既避免 page=0 算出负 offset 触发 SQL 报错，也避免一次性拉回超大结果集。
     */
    public static PageParam of(Integer page, Integer size) {
        int p = page == null ? DEFAULT_PAGE : Math.max(1, page);
        int s = size == null ? DEFAULT_SIZE : Math.min(Math.max(1, size), ConstValue.MAX_PAGE_SIZE);
        return new PageParam(p, s);
    }
}
