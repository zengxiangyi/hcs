package com.baogang.info.repository;

import com.baogang.info.entity.FlowNode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FlowNodeRepository extends JpaRepository<FlowNode, Long> {

    Optional<FlowNode> findByCode(String code);

    boolean existsByCode(String code);

    List<FlowNode> findByCategory(String category);

    List<FlowNode> findByFlowGraphAndCategory(String flowGraph,String category);

    List<FlowNode> findByFlowGraphAndCode(String flowGraph,String category);

    List<FlowNode> findByFlowGraph(String flowGraph);

}
