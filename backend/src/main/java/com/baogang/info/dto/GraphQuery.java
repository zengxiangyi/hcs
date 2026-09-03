package com.baogang.info.dto;

/**
 * 流程图查询条件（可变、可选）。所有字段默认 null，表示不参与过滤。
 * 通过 POST 请求体接收，支持任意字段组合的过滤条件。
 */
public class GraphQuery {

    private String flowGraph;
    private String title;

    private Integer page = 1;
    private Integer pageSize = 10;

    public String getFlowGraph() {
        return flowGraph;
    }

    public void setFlowGraph(String flowGraph) {
        this.flowGraph = flowGraph;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Integer getPage() {
        return page;
    }

    public void setPage(Integer page) {
        this.page = page;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }
}
