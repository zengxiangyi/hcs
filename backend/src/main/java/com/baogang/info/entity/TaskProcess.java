package com.baogang.info.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.DynamicUpdate;

/** 任务流程：记录任务流转的审核与步骤信息。 */
@Entity
@Table(name = "taskprocess")
@DynamicUpdate
public class TaskProcess {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Size(max = 100, message = "transfer 长度不能超过 100")
    @Column(name = "transfer", length = 100)
    private String transfer;

    @Size(max = 100, message = "blueprint 长度不能超过 100")
    @Column(name = "blueprint", length = 100)
    private String blueprint;

    @Size(max = 100, message = "auditUser 长度不能超过 100")
    @Column(name = "audituser", length = 100)
    private String auditUser;

    @Size(max = 100, message = "auditTime 长度不能超过 100")
    @Column(name = "audittime")
    private String auditTime;

    @Size(max = 100, message = "auditMessage 长度不能超过 100")
    @Column(name = "auditmessage", length = 100)
    private String auditMessage;

    @Size(max = 50, message = "auditState 长度不能超过 50")
    @Column(name = "auditstate", length = 50)
    private String auditState;

    @Size(max = 50, message = "step 长度不能超过 50")
    @Column(name = "step", length = 50)
    private String step;

    @Size(max = 50, message = "state 长度不能超过 50")
    @Column(name = "state", length = 50)
    private String state;

    @Size(max = 100, message = "createUser 长度不能超过 100")
    @Column(name = "createuser", length = 100)
    private String createUser;

    @Size(max = 100, message = "createTime 长度不能超过 100")
    @Column(name = "createtime")
    private String createTime;

    @Size(max = 100, message = "updateUser 长度不能超过 100")
    @Column(name = "updateuser", length = 100)
    private String updateUser;

    @Size(max = 100, message = "updateTime 长度不能超过 100")
    @Column(name = "updatetime")
    private String updateTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTransfer() {
        return transfer;
    }

    public void setTransfer(String transfer) {
        this.transfer = transfer;
    }

    public String getBlueprint() {
        return blueprint;
    }

    public void setBlueprint(String blueprint) {
        this.blueprint = blueprint;
    }

    public String getAuditUser() {
        return auditUser;
    }

    public void setAuditUser(String auditUser) {
        this.auditUser = auditUser;
    }

    public String getAuditMessage() {
        return auditMessage;
    }

    public void setAuditMessage(String auditMessage) {
        this.auditMessage = auditMessage;
    }

    public String getAuditState() {
        return auditState;
    }

    public void setAuditState(String auditState) {
        this.auditState = auditState;
    }

    public String getStep() {
        return step;
    }

    public void setStep(String step) {
        this.step = step;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getCreateUser() {
        return createUser;
    }

    public void setCreateUser(String createUser) {
        this.createUser = createUser;
    }


    public String getUpdateUser() {
        return updateUser;
    }

    public void setUpdateUser(String updateUser) {
        this.updateUser = updateUser;
    }

    public String getAuditTime() {
        return auditTime;
    }

    public void setAuditTime(String auditTime) {
        this.auditTime = auditTime;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public String getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(String updateTime) {
        this.updateTime = updateTime;
    }
}
