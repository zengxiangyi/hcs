package com.baogang.info.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.DynamicUpdate;

/** 记录流程流转的操作。 */
@Entity
@Table(name = "flowhistory")
@DynamicUpdate
public class FlowHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    // 流程实例编号
    @Size(max = 30, message = "workflow 长度不能超过 30")
    @Column(name = "workflow", length = 30)
    private String workflow;

    // 流程图
    @Size(max = 30, message = "flowGraph 长度不能超过 30")
    @Column(name = "flowgraph", length = 30)
    private String flowGraph;
    // 边线
    @Size(max = 100, message = "edge 长度不能超过 100")
    @Column(name = "edge", length = 100)
    private String edge;

    @Size(max = 45, message = "fromNode 长度不能超过 45")
    @Column(name = "fromnode", length = 45)
    private String fromNode;

    @Size(max = 45, message = "toNode 长度不能超过 45")
    @Column(name = "tonode", length = 45)
    private String toNode;

    // 处理时间
    @Size(max = 30, message = "dealTime 长度不能超过 30")
    @Column(name = "dealtime", length = 30)
    private String dealTime;
    // 处理人工号
    @Size(max = 100, message = "dealUser 长度不能超过 100")
    @Column(name = "dealuser", length = 100)
    private String dealUser;
    // 处理人名称
    @Size(max = 100, message = "userName 长度不能超过 100")
    @Column(name = "username", length = 100)
    private String userName;
    // 备注
    @Size(max = 100, message = "remark 长度不能超过 100")
    @Column(name = "remark", length = 100)
    private String remark;
    // 操作
    @Size(max = 100, message = "action 长度不能超过 100")
    @Column(name = "action", length = 100)
    private String action;
    // 审批笔记
    @Size(max = 100, message = "note 长度不能超过 100")
    @Column(name = "note", length = 100)
    private String note;

    public String getEdge() {
        return edge;
    }

    public void setEdge(String edge) {
        this.edge = edge;
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

    public String getDealTime() {
        return dealTime;
    }

    public void setDealTime(String dealTime) {
        this.dealTime = dealTime;
    }

    public String getDealUser() {
        return dealUser;
    }

    public void setDealUser(String dealUser) {
        this.dealUser = dealUser;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getFlowGraph() {
        return flowGraph;
    }

    public void setFlowGraph(String flowGraph) {
        this.flowGraph = flowGraph;
    }

}
