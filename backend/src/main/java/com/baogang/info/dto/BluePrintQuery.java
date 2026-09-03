package com.baogang.info.dto;

/**
 * 蓝图查询条件（可变、可选）。所有字段默认 null，表示不参与过滤。
 * 通过 POST 请求体接收，支持任意字段组合的过滤条件。
 */
public class BluePrintQuery {

    // 跨多字段模糊匹配（沿用原 search 语义）
    private String keyword;

    private String code;
    private String name;
    private String graph;
    private String firstLevel;
    private String secondLevel;
    private String materialName;
    private String materialCode;
    private String model;
    private String specs;
    private String customer;
    private String edition;
    private String state;
    private String createUser;
    private String remark;
    private String isFirstCheck;
    private String busbarNum;
    private String testNum;
    private String coolTime;
    private String hardnessDepth;
    private String chamfer;
    private String fallHead;
    private String quenching;
    private String attention;
    private String firstHardness;
    private String lastHardness;
    private String category;

    private Integer page = 1;
    private Integer pageSize = 10;

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
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

    public String getMaterialCode() {
        return materialCode;
    }

    public void setMaterialCode(String materialCode) {
        this.materialCode = materialCode;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
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

    public String getCreateUser() {
        return createUser;
    }

    public void setCreateUser(String createUser) {
        this.createUser = createUser;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
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

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
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
