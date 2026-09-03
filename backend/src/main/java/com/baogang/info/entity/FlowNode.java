package com.baogang.info.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.DynamicUpdate;

/** 流程节点表，定义流程图中每个节点的属性。 */
@Entity
@Table(name = "flownode")
@DynamicUpdate
public class FlowNode {

    // id
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    // 流程图
    @Size(max = 30, message = "flowgraph 长度不能超过 30")
    @Column(name = "flowgraph", length = 30)
    private String flowGraph;
    // 节点编号
    @Size(max = 30, message = "code 长度不能超过 30")
    @Column(name = "code", length = 30)
    private String code;
    // 节点名称
    @Size(max = 50, message = "name 长度不能超过 50")
    @Column(name = "name", length = 50)
    private String name;
    // S:开始，M:中间，E:结束
    @Size(max = 20, message = "category 长度不能超过 20")
    @Column(name = "category", length = 20)
    private String category;
    // 形状
    @Size(max = 20, message = "shape 长度不能超过 20")
    @Column(name = "shape", length = 20)
    private String shape;
    // 颜色
    @Size(max = 10, message = "color 长度不能超过 10")
    @Column(name = "color", length = 10)
    private String color;
    // R:角色，U:用户,M:角色和用户,P:角色或用户
    @Size(max = 10, message = "operator 长度不能超过 10")
    @Column(name = "operator", length = 10)
    private String operator;
    // 角色列表
    @Size(max = 100, message = "rolelist 长度不能超过 100")
    @Column(name = "rolelist", length = 100)
    private String roleList;
    // 用户列表
    @Size(max = 100, message = "userlist 长度不能超过 100")
    @Column(name = "userlist", length = 100)
    private String userList;

    @Size(max = 45, message = "userlist 长度不能超过 45")
    @Column(name = "X", length = 45)
    private String X;

    @Size(max = 45, message = "userlist 长度不能超过 45")
    @Column(name = "Y", length = 45)
    private String Y;

    @Size(max = 45, message = "userlist 长度不能超过 45")
    @Column(name = "W", length = 45)
    private String W;

    @Size(max = 45, message = "userlist 长度不能超过 45")
    @Column(name = "H", length = 45)
    private String H;

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

    public String getShape() {
        return shape;
    }

    public void setShape(String shape) {
        this.shape = shape;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getFlowGraph() {
        return flowGraph;
    }

    public void setFlowGraph(String flowGraph) {
        this.flowGraph = flowGraph;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
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

    public String getX() {
        return X;
    }

    public void setX(String x) {
        X = x;
    }

    public String getY() {
        return Y;
    }

    public void setY(String y) {
        Y = y;
    }

    public String getW() {
        return W;
    }

    public void setW(String w) {
        W = w;
    }

    public String getH() {
        return H;
    }

    public void setH(String h) {
        H = h;
    }
}
