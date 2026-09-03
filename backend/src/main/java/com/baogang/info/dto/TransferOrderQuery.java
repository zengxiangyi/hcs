package com.baogang.info.dto;

/**
 * 调拨单查询条件（可变、可选）。所有字段默认 null，表示不参与过滤。
 * 通过 POST 请求体接收，支持任意字段组合的过滤条件。
 */
public class TransferOrderQuery {

    private String category;
    private String code;
    private String createUser;
    private String inProcess;
    private String inRoom;
    private String material;
    private String materialCode;
    private String name;
    private String outProcess;
    private String outRoom;
    private String prompt;
    private String receiveUser;
    private String remark;
    private String state;
    private String supplier;

    private Integer page = 1;
    private Integer pageSize = 10;

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getCreateUser() {
        return createUser;
    }

    public void setCreateUser(String createUser) {
        this.createUser = createUser;
    }

    public String getInProcess() {
        return inProcess;
    }

    public void setInProcess(String inProcess) {
        this.inProcess = inProcess;
    }

    public String getInRoom() {
        return inRoom;
    }

    public void setInRoom(String inRoom) {
        this.inRoom = inRoom;
    }

    public String getMaterial() {
        return material;
    }

    public void setMaterial(String material) {
        this.material = material;
    }

    public String getMaterialCode() {
        return materialCode;
    }

    public void setMaterialCode(String materialCode) {
        this.materialCode = materialCode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOutProcess() {
        return outProcess;
    }

    public void setOutProcess(String outProcess) {
        this.outProcess = outProcess;
    }

    public String getOutRoom() {
        return outRoom;
    }

    public void setOutRoom(String outRoom) {
        this.outRoom = outRoom;
    }

    public String getPrompt() {
        return prompt;
    }

    public void setPrompt(String prompt) {
        this.prompt = prompt;
    }

    public String getReceiveUser() {
        return receiveUser;
    }

    public void setReceiveUser(String receiveUser) {
        this.receiveUser = receiveUser;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getSupplier() {
        return supplier;
    }

    public void setSupplier(String supplier) {
        this.supplier = supplier;
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
