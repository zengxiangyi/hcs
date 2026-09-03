package com.baogang.info.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.DynamicUpdate;

import java.time.LocalDateTime;

/** 流程实例表：存储业务流程定义、状态及起止时间。 */
@Entity
@Table(name = "workflow")
@DynamicUpdate
public class Workflow {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    // 流程实例编码
    @Size(max = 30, message = "code 长度不能超过 30")
    @Column(name = "code", length = 30)
    private String code;
    // 流程实例名称
    @Size(max = 100, message = "name 长度不能超过 100")
    @Column(name = "name", length = 100)
    private String name;
    // 流程状态
    @Size(max = 30, message = "state 长度不能超过 30")
    @Column(name = "state", length = 30)
    private String state;
    // 发起人
    @Size(max = 30, message = "sender 长度不能超过 30")
    @Column(name = "sender", length = 30)
    private String sender;
    // 开始时间
    @Column(name = "starttime")
    private String startTime;
    // 结束时间
    @Column(name = "endtime")
    private String endTime;
    // 备注
    @Size(max = 100, message = "remark 长度不能超过 100")
    @Column(name = "remark", length = 100)
    private String remark;
    // 流程图编号
    @Size(max = 30, message = "category 长度不能超过 30")
    @Column(name = "flowgraph", length = 30)
    private String flowGraph;

    // 目标编码
    @Size(max = 45, message = "category 长度不能超过 45")
    @Column(name = "targetcode", length = 45)
    private String targetCode;

    // 流程分类
    @Size(max = 45, message = "category 长度不能超过 45")
    @Column(name = "category", length = 45)
    private String category;


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

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
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

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getTargetCode() {
        return targetCode;
    }

    public void setTargetCode(String targetCode) {
        this.targetCode = targetCode;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }
}
