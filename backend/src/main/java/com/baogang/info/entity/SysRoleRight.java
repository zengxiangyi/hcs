package com.baogang.info.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.DynamicUpdate;

/** 角色-权限关联表：建立角色与权限的多对多映射关系。 */
@Entity
@Table(name = "sysroleright")
@DynamicUpdate
public class SysRoleRight {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Size(max = 30, message = "roleCode 长度不能超过 30")
    @Column(name = "rolecode", length = 30)
    private String roleCode;

    @Size(max = 30, message = "rightCode 长度不能超过 30")
    @Column(name = "rightcode", length = 30)
    private String rightCode;

    @Size(max = 100, message = "remark 长度不能超过 100")
    @Column(name = "remark", length = 100)
    private String remark;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getRoleCode() {
        return roleCode;
    }

    public void setRoleCode(String roleCode) {
        this.roleCode = roleCode;
    }

    public String getRightCode() {
        return rightCode;
    }

    public void setRightCode(String rightCode) {
        this.rightCode = rightCode;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}
