package com.baogang.info.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.DynamicUpdate;

/** 常量值表，保存系统各类常量定义。 */
@Entity
@Table(name = "constvalue")
@DynamicUpdate
public class ConstValue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "code 不能为空")
    @Size(max = 10, message = "code 长度不能超过 10")
    @Column(name = "code", length = 10)
    private String code;

    @Size(max = 30, message = "name 长度不能超过 30")
    @Column(name = "name", length = 30)
    private String name;

    @Size(max = 20, message = "category 长度不能超过 20")
    @Column(name = "category", length = 20)
    private String category;

    @Size(max = 100, message = "mark 长度不能超过 100")
    @Column(name = "mark", length = 100)
    private String mark;

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

    public String getMark() {
        return mark;
    }

    public void setMark(String mark) {
        this.mark = mark;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}
