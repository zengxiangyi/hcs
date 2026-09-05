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

    // 定向删除：只清某个节点的当前记录，保留同流程其它并行分支
    void deleteByWorkflowAndFlowNode(String workflow, String flowNode);
}
