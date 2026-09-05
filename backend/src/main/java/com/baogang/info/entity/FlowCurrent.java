package com.baogang.info.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.DynamicUpdate;

/** 流程当前节点表，记录每个流程当前所处的操作节点与状态。 */
@Entity
@Table(name = "flowcurrent")
@DynamicUpdate
public class FlowCurrent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    // 流程实例
    @Size(max = 30, message = "workflow 长度不能超过 30")
    @Column(name = "workflow", length = 30)
    private String workflow;

    // 流程图
    @Size(max = 100, message = "flowGraph 长度不能超过 100")
    @Column(name = "flowgraph", length = 100)
    private String flowGraph;
    // 流程图节点
    @Size(max = 100, message = "flowNode 长度不能超过 100")
    @Column(name = "flownode", length = 100)
    private String flowNode;
    // 开始时间
    @Size(max = 30, message = "startTime 长度不能超过 100")
    @Column(name = "starttime", length = 30)
    private String startTime;
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

    public String getWorkflow() {
        return workflow;
    }

    public void setWorkflow(String workflow) {
        this.workflow = workflow;
    }

    public String getFlowNode() {
        return flowNode;
    }

    public void setFlowNode(String flowNode) {
        this.flowNode = flowNode;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
    
    public String getFlowGraph() {
        return flowGraph;
    }

    public void setFlowGraph(String flowGraph) {
        this.flowGraph = flowGraph;
    }

}
