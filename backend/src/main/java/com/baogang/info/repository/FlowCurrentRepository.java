package com.baogang.info.repository;

import com.baogang.info.entity.FlowCurrent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FlowCurrentRepository extends JpaRepository<FlowCurrent, Long> {

    List<FlowCurrent> findByWorkflow(String workflow);

    List<FlowCurrent> findByFlowNode(String flowNode);

    void removeByWorkflow(String workflow);
}
