package com.baogang.info.repository;

import com.baogang.info.entity.Workflow;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WorkflowRepository extends JpaRepository<Workflow, Long> {

    Optional<Workflow> findByCode(String code);

    boolean existsByCode(String code);

    List<Workflow> findByState(String state);

    List<Workflow> findByFlowGraph(String flowGraph);

    Page<Workflow> findBySender(String sender, Pageable pageable);
}