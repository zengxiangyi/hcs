package com.baogang.info.repository;

import com.baogang.info.entity.FlowGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FlowGraphRepository extends JpaRepository<FlowGraph, Long> {

    @Override
    Optional<FlowGraph> findById(Long aLong);

    List<FlowGraph> findByFlowGraph(String flowGraph);
}
