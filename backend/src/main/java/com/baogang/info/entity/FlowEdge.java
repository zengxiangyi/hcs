package com.baogang.info.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.DynamicUpdate;

/** 流程连线表，定义流程图中节点之间的连线关系。 */
@Entity
@Table(name = "flowedge")
@DynamicUpdate
public class FlowEdge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 连线编号
    @Size(max = 30, message = "code 长度不能超过 30")
    @Column(name = "code", length = 30)
    private String code;

    // 连线名称
    @Size(max = 100, message = "name 长度不能超过 100")
    @Column(name = "name", length = 100)
    private String name;

    // 颜色
    @Size(max = 10, message = "color 长度不能超过 10")
    @Column(name = "color", length = 10)
    private String color;

    // 起始节点
    @Size(max = 30, message = "fromNode 长度不能超过 30")
    @Column(name = "fromnode", length = 30)
    private String fromNode;

    // 结束节点
    @Size(max = 30, message = "toNode 长度不能超过 30")
    @Column(name = "tonode", length = 30)
    private String toNode;
    // 坐标轴JSON
    @Size(max = 100, message = "axis 长度不能超过 100")
    @Column(name = "axis", length = 100)
    private String axis;

    // 流程图编号
    @Size(max = 30, message = "flowGraph 长度不能超过 30")
    @Column(name = "flowgraph", length = 30)
    private String flowGraph;

    // 条件分类, P:排他,M:并行
    @Size(max = 20, message = "category 长度不能超过 20")
    @Column(name = "category", length = 20)
    private String category;

    // 条件
    @Size(max = 30, message = "cond 长度不能超过 30")
    @Column(name = "cond", length = 30)
    private String cond;

    // 备注
    @Size(max = 200, message = "remark 长度不能超过 200")
    @Column(name = "remark", length = 200)
    private String remark;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getFromNode() {
        return fromNode;
    }

    public void setFromNode(String fromNode) {
        this.fromNode = fromNode;
    }

    public String getToNode() {
        return toNode;
    }

    public void setToNode(String toNode) {
        this.toNode = toNode;
    }

    public String getAxis() {
        return axis;
    }

    public void setAxis(String axis) {
        this.axis = axis;
    }

    public String getFlowGraph() {
        return flowGraph;
    }

    public void setFlowGraph(String flowGraph) {
        this.flowGraph = flowGraph;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getCond() {
        return cond;
    }

    public void setCond(String cond) {
        this.cond = cond;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}
