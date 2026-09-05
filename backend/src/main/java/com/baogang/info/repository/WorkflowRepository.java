package com.baogang.info.repository;

import com.baogang.info.entity.Workflow;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WorkflowRepository extends JpaRepository<Workflow, Long> {

    Optional<Workflow> findByCode(String code);

    // 审批用：悲观行锁，两个用户同时审批同一流程实例时串行化，防止 flowcurrent/flowhistory 双写错乱
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select w from Workflow w where w.code = :code")
    Optional<Workflow> findByCodeForUpdate(@Param("code") String code);

    boolean existsByCode(String code);

    List<Workflow> findByState(String state);

    List<Workflow> findByFlowGraph(String flowGraph);

    Page<Workflow> findBySender(String sender, Pageable pageable);
}