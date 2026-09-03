package com.baogang.info.common;

import java.util.List;

/**
 * 分页结果包装。
 *
 * @param <T> 行数据类型
 */
public class PageResult<T> {

    private final List<T> content;
    private final long total;
    private final int page;
    private final int size;

    private PageResult(List<T> content, long total, int page, int size) {
        this.content = content;
        this.total = total;
        this.page = page;
        this.size = size;
    }

    public List<T> getContent() {
        return content;
    }

    public long getTotal() {
        return total;
    }

    public int getPage() {
        return page;
    }

    public int getSize() {
        return size;
    }

    public static <T> PageResult<T> of(List<T> content, long total, int page, int size) {
        return new PageResult<>(content, total, page, size);
    }
}
