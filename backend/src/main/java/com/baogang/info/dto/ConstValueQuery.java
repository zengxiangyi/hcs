package com.baogang.info.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

/**
 * 常量查询条件（可变、可选）。过滤字段默认 null，表示不参与过滤。
 * 通过 POST 请求体接收，支持任意字段组合的过滤条件。
 */
public class ConstValueQuery {

    private String category;
    private String code;
    private String mark;
    private String name;
    private String remark;

    @Min(value = 1, message = "page 不能小于 1")
    private int page = 1;

    @Min(value = 1, message = "pageSize 不能小于 1")
    @Max(value = 200, message = "pageSize 不能超过 200")
    private int pageSize = 10;

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMark() {
        return mark;
    }

    public void setMark(String mark) {
        this.mark = mark;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }
}
