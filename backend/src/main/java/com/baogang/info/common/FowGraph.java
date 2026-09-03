package com.baogang.info.common;

import com.baogang.info.entity.FlowEdge;
import com.baogang.info.entity.FlowNode;

import java.util.List;

public class FowGraph {
    // 流程编号
    private String workflow;
    // 流程名称
    private String title;
    // 画布宽度
    private String width;
    // 画布高度
    private String height;
    // 节点列表
    private List<FlowNode> nodes;
    // 边线列表
    private List<FlowEdge> edges;

    public String getWorkflow() {
        return workflow;
    }

    public void setWorkflow(String workflow) {
        this.workflow = workflow;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getWidth() {
        return width;
    }

    public void setWidth(String width) {
        this.width = width;
    }

    public String getHeight() {
        return height;
    }

    public void setHeight(String height) {
        this.height = height;
    }

    public List<FlowNode> getNodes() {
        return nodes;
    }

    public void setNodes(List<FlowNode> nodes) {
        this.nodes = nodes;
    }

    public List<FlowEdge> getEdges() {
        return edges;
    }

    public void setEdges(List<FlowEdge> edges) {
        this.edges = edges;
    }

    public FowGraph(String workflow) {
        this.workflow = workflow;
    }

    public FowGraph(String workflow, String title, String width, String height, List<FlowNode> nodes, List<FlowEdge> edges) {
        this.workflow = workflow;
        this.title = title;
        this.width = width;
        this.height = height;
        this.nodes = nodes;
        this.edges = edges;
    }
    // 无参数构造方法
    public FowGraph() {
    }

}
