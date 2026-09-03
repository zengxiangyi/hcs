package com.baogang.info.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
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
    // 一级工艺
    @Size(max = 100, message = "firstLevel 长度不能超过 100")
    @Column(name = "firstlevel", length = 100)
    private String firstLevel;
    // 二级工艺
    @Size(max = 100, message = "secondLevel 长度不能超过 100")
    @Column(name = "secondlevel", length = 100)
    private String secondLevel;
    // 工序编号
    @Size(max = 50, message = "step 长度不能超过 50")
    @Column(name = "step", length = 50)
    private String step;
    // 工序名称
    @Size(max = 100, message = "stepName 长度不能超过 100")
    @Column(name = "stepname", length = 100)
    private String stepName;
    // 排序
    @Size(max = 20, message = "sort 长度不能超过 20")
    @Column(name = "sort", length = 20)
    private String sort;
    // 可选
    @Size(max = 10, message = "isNeed 长度不能超过 10")
    @Column(name = "isneed", length = 10)
    private String isNeed;
    // 备注
    @Size(max = 200, message = "remark 长度不能超过 200")
    @Column(name = "remark", length = 200)
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
