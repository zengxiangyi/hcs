package com.baogang.info.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.DynamicUpdate;

/** 流程图配置表：存储流程画布的布局参数。 */
@Entity
@Table(name = "flowgraph")
@DynamicUpdate
public class FlowGraph {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    // 流程图编号
    @Size(max = 30, message = "flowGraph 长度不能超过 30")
    @Column(name = "flowgraph", length = 30)
    private String flowGraph;
    // 备注
    @Size(max = 100, message = "title 长度不能超过 100")
    @Column(name = "title", length = 100)
    private String title;
    // 宽度
    @Size(max = 10, message = "width 长度不能超过 10")
    @Column(name = "width", length = 10)
    private String width;
    // 高度
    @Size(max = 10, message = "height 长度不能超过 10")
    @Column(name = "height", length = 10)
    private String height;
    // 备注
    @Size(max = 100, message = "remark 长度不能超过 100")
    @Column(name = "remark", length = 100)
    private String remark;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}
