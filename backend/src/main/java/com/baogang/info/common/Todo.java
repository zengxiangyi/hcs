package com.baogang.info.common;

public class Todo {
    // 流程实例
    private String workflow;
    // 流程图编号
    private String flowGraph;
    // 流程节点
    private String flowNode;
    // 开始时间
    private String startTime;
    // 备注
    private String remark;
    // 节点名称
    private String nodeName;
    // 操作人分类
    private String operator;
    // 角色列表
    private String roleList;
    // 用户列表
    private String userList;
    // 流程编号
    private String code;
    // 流程名称
    private String name;
    // 流程分类
    private String category;
    // 目标对象ID
    private String targetCode;
    // 发起人
    private String sender;
    // 发起时间
    private String beginTime;
    // 流程状态
    private String state;

    public String getWorkflow() {
        return workflow;
    }

    public void setWorkflow(String workflow) {
        this.workflow = workflow;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getNodeName() {
        return nodeName;
    }

    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getBeginTime() {
        return beginTime;
    }

    public void setBeginTime(String beginTime) {
        this.beginTime = beginTime;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFlowGraph() {
        return flowGraph;
    }

    public void setFlowGraph(String flowGraph) {
        this.flowGraph = flowGraph;
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

    public String getRoleList() {
        return roleList;
    }

    public void setRoleList(String roleList) {
        this.roleList = roleList;
    }

    public String getUserList() {
        return userList;
    }

    public void setUserList(String userList) {
        this.userList = userList;
    }

    public String getTargetCode() {
        return targetCode;
    }

    public void setTargetCode(String targetCode) {
        this.targetCode = targetCode;
    }
}
