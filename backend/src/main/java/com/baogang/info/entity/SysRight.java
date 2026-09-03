package com.baogang.info.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.DynamicUpdate;

/** 权限表：存储系统权限项及其分类与备注。 */
@Entity
@Table(name = "sysright")
@DynamicUpdate
public class SysRight {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Size(max = 100, message = "code 长度不能超过 100")
    @Column(name = "code", length = 100)
    private String code;

    @Size(max = 100, message = "name 长度不能超过 100")
    @Column(name = "name", length = 100)
    private String name;

    @Size(max = 20, message = "category 长度不能超过 20")
    @Column(name = "category", length = 20)
    private String category;

    @Size(max = 100, message = "parent 长度不能超过 100")
    @Column(name = "parent", length = 100)
    private String parent;

    @Size(max = 100, message = "remark 长度不能超过 100")
    @Column(name = "remark", length = 100)
    private String remark;

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

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getParent() {
        return parent;
    }

    public void setParent(String parent) {
        this.parent = parent;
    }
}
