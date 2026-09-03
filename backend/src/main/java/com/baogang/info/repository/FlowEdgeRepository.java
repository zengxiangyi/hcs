package com.baogang.info.repository;

import com.baogang.info.entity.FlowEdge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FlowEdgeRepository extends JpaRepository<FlowEdge, Long> {

    Optional<FlowEdge> findByCode(String code);

    boolean existsByCode(String code);

    List<FlowEdge> findByFromNode(String fromNode);

    List<FlowEdge> findByToNode(String toNode);

    List<FlowEdge> findByFlowGraphAndFromNode(String flowGraph,String fromNode);

    List<FlowEdge> findByFlowGraphAndCode(String flowGraph,String code);

    List<FlowEdge> findByFlowGraph(String flowGraph);
}
