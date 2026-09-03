package com.baogang.info.repository;

import com.baogang.info.entity.TechStep;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TechStepRepository extends JpaRepository<TechStep, Long> {

    List<TechStep> findByFirstLevel(String firstLevel);

    // sort 为字符串列，此处按字典序升序；需数值序请改用 MyBatis 查询
    List<TechStep> findByFirstLevelAndSecondLevelOrderBySortAsc(String firstLevel, String secondLevel);

    List<TechStep> findByStep(String step);
}
