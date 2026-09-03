package com.baogang.info.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.DynamicUpdate;

/** 调拨单：记录物料调拨流转信息。 */
@Entity
@Table(name = "transferorder")
@DynamicUpdate
public class TransferOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Size(max = 100, message = "code 长度不能超过 100")
    @Column(name = "code", length = 100)
    private String code;

    @Size(max = 100, message = "name 长度不能超过 100")
    @Column(name = "name", length = 100)
    private String name;

    @Size(max = 100, message = "category 长度不能超过 100")
    @Column(name = "category", length = 100)
    private String category;

    @Size(max = 100, message = "transferdate 长度不能超过 100")
    @Column(name = "transferdate", length = 100)
    private String transferDate;

    @Size(max = 100, message = "materialcode 长度不能超过 100")
    @Column(name = "materialcode", length = 100)
    private String materialCode;

    @Size(max = 100, message = "num 长度不能超过 100")
    @Column(name = "num", length = 100)
    private String num;

    @Size(max = 100, message = "weight 长度不能超过 100")
    @Column(name = "weight", length = 100)
    private String weight;

    @Size(max = 100, message = "material 长度不能超过 100")
    @Column(name = "material", length = 100)
    private String material;

    @Size(max = 100, message = "rollnum 长度不能超过 100")
    @Column(name = "rollnum", length = 100)
    private String rollNum;

    @Size(max = 100, message = "outprocess 长度不能超过 100")
    @Column(name = "outprocess", length = 100)
    private String outProcess;

    @Size(max = 100, message = "inprocess 长度不能超过 100")
    @Column(name = "inprocess", length = 100)
    private String inProcess;

    @Size(max = 100, message = "outroom 长度不能超过 100")
    @Column(name = "outroom", length = 100)
    private String outRoom;

    @Size(max = 100, message = "inroom 长度不能超过 100")
    @Column(name = "inroom", length = 100)
    private String inRoom;

    @Size(max = 100, message = "remark 长度不能超过 100")
    @Column(name = "remark", length = 100)
    private String remark;

    @Size(max = 100, message = "prompt 长度不能超过 100")
    @Column(name = "prompt", length = 100)
    private String prompt;

    @Size(max = 100, message = "quenching 长度不能超过 100")
    @Column(name = "quenching", length = 100)
    private String quenching;

    @Size(max = 100, message = "supplier 长度不能超过 100")
    @Column(name = "supplier", length = 100)
    private String supplier;

    @Size(max = 100, message = "createuser 长度不能超过 100")
    @Column(name = "createuser", length = 100)
    private String createUser;

    @Size(max = 100, message = "createtime 长度不能超过 100")
    @Column(name = "createtime", length = 100)
    private String createTime;

    @Size(max = 100, message = "receiveuser 长度不能超过 100")
    @Column(name = "receiveuser", length = 100)
    private String receiveUser;

    @Size(max = 100, message = "receivetime 长度不能超过 100")
    @Column(name = "receivetime", length = 100)
    private String receiveTime;

    @Size(max = 100, message = "state 长度不能超过 100")
    @Column(name = "state", length = 100)
    private String state;

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

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getTransferDate() {
        return transferDate;
    }

    public void setTransferDate(String datetime) {
        this.transferDate = datetime;
    }

    public String getMaterialCode() {
        return materialCode;
    }

    public void setMaterialCode(String materialCode) {
        this.materialCode = materialCode;
    }

    public String getNum() {
        return num;
    }

    public void setNum(String num) {
        this.num = num;
    }

    public String getWeight() {
        return weight;
    }

    public void setWeight(String weight) {
        this.weight = weight;
    }

    public String getMaterial() {
        return material;
    }

    public void setMaterial(String material) {
        this.material = material;
    }

    public String getRollNum() {
        return rollNum;
    }

    public void setRollNum(String rollNum) {
        this.rollNum = rollNum;
    }

    public String getOutProcess() {
        return outProcess;
    }

    public void setOutProcess(String outProcess) {
        this.outProcess = outProcess;
    }

    public String getInProcess() {
        return inProcess;
    }

    public void setInProcess(String inProcess) {
        this.inProcess = inProcess;
    }

    public String getOutRoom() {
        return outRoom;
    }

    public void setOutRoom(String outRoom) {
        this.outRoom = outRoom;
    }

    public String getInRoom() {
        return inRoom;
    }

    public void setInRoom(String inRoom) {
        this.inRoom = inRoom;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getPrompt() {
        return prompt;
    }

    public void setPrompt(String prompt) {
        this.prompt = prompt;
    }

    public String getQuenching() {
        return quenching;
    }

    public void setQuenching(String quenching) {
        this.quenching = quenching;
    }

    public String getSupplier() {
        return supplier;
    }

    public void setSupplier(String supplier) {
        this.supplier = supplier;
    }

    public String getCreateUser() {
        return createUser;
    }

    public void setCreateUser(String createUser) {
        this.createUser = createUser;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public String getReceiveUser() {
        return receiveUser;
    }

    public void setReceiveUser(String receiveUser) {
        this.receiveUser = receiveUser;
    }

    public String getReceiveTime() {
        return receiveTime;
    }

    public void setReceiveTime(String receiveTime) {
        this.receiveTime = receiveTime;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }
}
