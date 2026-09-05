package com.baogang.info.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.DynamicUpdate;

/** 工艺工序表，定义一/二级工艺下的工序明细。 */
@Entity
@Table(name = "techstep")
@DynamicUpdate
public class TechStep {

    // id
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    // 一级工艺（库 varchar(45)）
    @Size(max = 45, message = "firstLevel 长度不能超过 45")
    @Column(name = "firstlevel", length = 45)
    private String firstLevel;
    // 二级工艺（库 varchar(45)）
    @Size(max = 45, message = "secondLevel 长度不能超过 45")
    @Column(name = "secondlevel", length = 45)
    private String secondLevel;
    // 工序编号（库 varchar(45)）
    @Size(max = 45, message = "step 长度不能超过 45")
    @Column(name = "step", length = 45)
    private String step;
    // 工序名称（库 varchar(45)）
    @Size(max = 45, message = "stepName 长度不能超过 45")
    @Column(name = "stepname", length = 45)
    private String stepName;
    // 排序（库 varchar(45)）
    @Size(max = 45, message = "sort 长度不能超过 45")
    @Column(name = "sort", length = 45)
    private String sort;
    // 可选（库 varchar(45)）
    @Size(max = 45, message = "isNeed 长度不能超过 45")
    @Column(name = "isneed", length = 45)
    private String isNeed;
    // 备注（库 varchar(100)）
    @Size(max = 100, message = "remark 长度不能超过 100")
    @Column(name = "remark", length = 100)
    private String remark;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getStep() {
        return step;
    }

    public void setStep(String step) {
        this.step = step;
    }

    public String getStepName() {
        return stepName;
    }

    public void setStepName(String stepName) {
        this.stepName = stepName;
    }

    public String getSort() {
        return sort;
    }

    public void setSort(String sort) {
        this.sort = sort;
    }

    public String getIsNeed() {
        return isNeed;
    }

    public void setIsNeed(String isNeed) {
        this.isNeed = isNeed;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}
