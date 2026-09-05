package com.baogang.info.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.DynamicUpdate;

/** 用户表：存储系统用户基本信息及账户状态。 */
@Entity
@Table(name = "sysuser")
@DynamicUpdate
public class SysUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // code 为登录账号名，创建时必填
    @NotBlank(message = "code 不能为空")
    @Size(max = 10, message = "code 长度不能超过 10")
    @Column(name = "code", length = 10)
    private String code;

    @Size(max = 20, message = "name 长度不能超过 20")
    @Column(name = "name", length = 20)
    private String name;

    // WRITE_ONLY：请求体可写入、响应体不序列化，防止分页列表/详情明文回传密码
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Size(max = 100, message = "password 长度不能超过 100")
    @Column(name = "password", length = 100)
    private String password;

    @Size(max = 100, message = "remark 长度不能超过 100")
    @Column(name = "remark", length = 100)
    private String remark;

    @Size(max = 100, message = "email 长度不能超过 100")
    @Column(name = "email", length = 100)
    private String email;

    @Size(max = 30, message = "department 长度不能超过 30")
    @Column(name = "department", length = 30)
    private String department;

    @Size(max = 30, message = "position 长度不能超过 30")
    @Column(name = "position", length = 30)
    private String position;

    @Size(max = 15, message = "cellphone 长度不能超过 15")
    @Column(name = "cellphone", length = 15)
    private String cellphone;

    @Size(max = 10, message = "state 长度不能超过 10")
    @Column(name = "state", length = 10)
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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public String getCellphone() {
        return cellphone;
    }

    public void setCellphone(String cellphone) {
        this.cellphone = cellphone;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }
}
