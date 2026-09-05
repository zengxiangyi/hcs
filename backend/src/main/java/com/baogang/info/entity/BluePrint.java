package com.baogang.info.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.DynamicUpdate;

/** 蓝图表，记录物料/工艺蓝图信息。 */
@Entity
@Table(name = "blueprint")
@DynamicUpdate
public class BluePrint {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 编号
    @Size(max = 100, message = "code 长度不能超过 100")
    @Column(name = "code", length = 100)
    private String code;

    // 名称（库 varchar(100)）
    @Size(max = 100, message = "name 长度不能超过 100")
    @Column(name = "name", length = 100)
    private String name;

    // 图形
    @Size(max = 100, message = "graph 长度不能超过 100")
    @Column(name = "graph", length = 100)
    private String graph;

    // 一级工艺
    @Size(max = 100, message = "firstLevel 长度不能超过 100")
    @Column(name = "firstlevel", length = 100)
    private String firstLevel;

    // 二级工艺
    @Size(max = 30, message = "secondLevel 长度不能超过 30")
    @Column(name = "secondlevel", length = 30)
    private String secondLevel;

    // 物料名称
    @Size(max = 100, message = "materialName 长度不能超过 100")
    @Column(name = "materialname", length = 100)
    private String materialName;

    // 单重（库 varchar(100)）
    @Size(max = 100, message = "weight 长度不能超过 100")
    @Column(name = "weight", length = 100)
    private String weight;

    // 物料编码
    @Size(max = 100, message = "materialCode 长度不能超过 100")
    @Column(name = "materialcode", length = 100)
    private String materialCode;

    // 是否首检
    @Size(max = 100, message = "isFirstCheck 长度不能超过 100")
    @Column(name = "isfirstcheck", length = 100)
    private String isFirstCheck;

    // 母线数量
    @Size(max = 100, message = "busbarNum 长度不能超过 100")
    @Column(name = "busbarnum", length = 100)
    private String busbarNum;

    // 测点数量
    @Size(max = 100, message = "testNum 长度不能超过 100")
    @Column(name = "testnum", length = 100)
    private String testNum;

    // 冷却时间
    @Size(max = 100, message = "coolTime 长度不能超过 100")
    @Column(name = "cooltime", length = 100)
    private String coolTime;

    // 硬化层深度
    @Size(max = 100, message = "hardnessDepth 长度不能超过 100")
    @Column(name = "hardnessdepth", length = 100)
    private String hardnessDepth;

    // 辊身倒角
    @Size(max = 100, message = "chamfer 长度不能超过 100")
    @Column(name = "chamfer", length = 100)
    private String chamfer;

    // 身颈落差
    @Size(max = 100, message = "fallHead 长度不能超过 100")
    @Column(name = "fallhead", length = 100)
    private String fallHead;

    // 淬火部位
    @Size(max = 100, message = "quenching 长度不能超过 100")
    @Column(name = "quenching", length = 100)
    private String quenching;

    // 注意事项
    @Size(max = 100, message = "attention 长度不能超过 100")
    @Column(name = "attention", length = 100)
    private String attention;

    // 材质
    @Size(max = 100, message = "model 长度不能超过 100")
    @Column(name = "model", length = 100)
    private String model;

    // 首检硬度要求
    @Size(max = 100, message = "firstHardness 长度不能超过 100")
    @Column(name = "firsthardness", length = 100)
    private String firstHardness;

    // 完工硬度要求
    @Size(max = 100, message = "lastHardness 长度不能超过 100")
    @Column(name = "lasthardness", length = 100)
    private String lastHardness;

    // 规格
    @Size(max = 100, message = "specs 长度不能超过 100")
    @Column(name = "specs", length = 100)
    private String specs;

    // 客户
    @Size(max = 100, message = "customer 长度不能超过 100")
    @Column(name = "customer", length = 100)
    private String customer;

    // 版本
    @Size(max = 30, message = "edition 长度不能超过 30")
    @Column(name = "edition", length = 30)
    private String edition;

    // 状态
    @Size(max = 100, message = "state 长度不能超过 100")
    @Column(name = "state", length = 100)
    private String state;

    // 备注
    @Size(max = 100, message = "remark 长度不能超过 100")
    @Column(name = "remark", length = 100)
    private String remark;

    // 创建时间（库 varchar(45)）
    @Size(max = 45, message = "createTime 长度不能超过 45")
    @Column(name = "createtime", length = 45)
    private String createTime;
    // 创建人（库 varchar(45)）
    @Size(max = 45, message = "createUser 长度不能超过 45")
    @Column(name = "createuser", length = 45)
    private String createUser;

    // 最近修改时间（库 varchar(45)）
    @Size(max = 45, message = "updateTime 长度不能超过 45")
    @Column(name = "updatetime", length = 45)
    private String updateTime;
    // 最近修改人（库 varchar(45)）
    @Size(max = 45, message = "updateUser 长度不能超过 45")
    @Column(name = "updateuser", length = 45)
    private String updateUser;
    // 流程实例编码（库 varchar(45)）
    @Size(max = 45, message = "workflow 长度不能超过 45")
    @Column(name = "workflow", length = 45)
    private String workflow;

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

    public String getGraph() {
        return graph;
    }

    public void setGraph(String graph) {
        this.graph = graph;
    }

    public String getFirstLevel() {
        return firstLevel;
    }

    public void setFirstLevel(String firstLevel) {
        this.firstLevel = firstLevel;
    }

    public String getSecondLevel() {
        return secondLevel;
    }

    public void setSecondLevel(String secondLevel) {
        this.secondLevel = secondLevel;
    }

    public String getMaterialName() {
        return materialName;
    }

    public void setMaterialName(String materialName) {
        this.materialName = materialName;
    }

    public String getWeight() {
        return weight;
    }

    public void setWeight(String weight) {
        this.weight = weight;
    }

    public String getMaterialCode() {
        return materialCode;
    }

    public void setMaterialCode(String materialCode) {
        this.materialCode = materialCode;
    }

    public String getIsFirstCheck() {
        return isFirstCheck;
    }

    public void setIsFirstCheck(String isFirstCheck) {
        this.isFirstCheck = isFirstCheck;
    }

    public String getBusbarNum() {
        return busbarNum;
    }

    public void setBusbarNum(String busbarNum) {
        this.busbarNum = busbarNum;
    }

    public String getTestNum() {
        return testNum;
    }

    public void setTestNum(String testNum) {
        this.testNum = testNum;
    }

    public String getCoolTime() {
        return coolTime;
    }

    public void setCoolTime(String coolTime) {
        this.coolTime = coolTime;
    }

    public String getHardnessDepth() {
        return hardnessDepth;
    }

    public void setHardnessDepth(String hardnessDepth) {
        this.hardnessDepth = hardnessDepth;
    }

    public String getChamfer() {
        return chamfer;
    }

    public void setChamfer(String chamfer) {
        this.chamfer = chamfer;
    }

    public String getFallHead() {
        return fallHead;
    }

    public void setFallHead(String fallHead) {
        this.fallHead = fallHead;
    }

    public String getQuenching() {
        return quenching;
    }

    public void setQuenching(String quenching) {
        this.quenching = quenching;
    }

    public String getAttention() {
        return attention;
    }

    public void setAttention(String attention) {
        this.attention = attention;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getFirstHardness() {
        return firstHardness;
    }

    public void setFirstHardness(String firstHardness) {
        this.firstHardness = firstHardness;
    }

    public String getLastHardness() {
        return lastHardness;
    }

    public void setLastHardness(String lastHardness) {
        this.lastHardness = lastHardness;
    }

    public String getCreateUser() {
        return createUser;
    }

    public void setCreateUser(String createUser) {
        this.createUser = createUser;
    }

    public String getSpecs() {
        return specs;
    }

    public void setSpecs(String specs) {
        this.specs = specs;
    }

    public String getCustomer() {
        return customer;
    }

    public void setCustomer(String customer) {
        this.customer = customer;
    }

    public String getEdition() {
        return edition;
    }

    public void setEdition(String edition) {
        this.edition = edition;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
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

    public String getUpdateUser() {
        return updateUser;
    }

    public void setUpdateUser(String updateUser) {
        this.updateUser = updateUser;
    }

    public String getWorkflow() {
        return workflow;
    }

    public void setWorkflow(String workflow) {
        this.workflow = workflow;
    }
}
