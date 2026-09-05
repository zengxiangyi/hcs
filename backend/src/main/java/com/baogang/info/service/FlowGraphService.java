package com.baogang.info.service;

import com.baogang.info.common.PageResult;
import com.baogang.info.entity.FlowGraph;
import com.baogang.info.exception.ResourceNotFoundException;
import com.baogang.info.repository.FlowEdgeRepository;
import com.baogang.info.repository.FlowGraphRepository;
import com.baogang.info.repository.FlowNodeRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class FlowGraphService {

    private final FlowGraphRepository flowGraphRepository;
    private final FlowNodeRepository flowNodeRepository;
    private final FlowEdgeRepository flowEdgeRepository;

    public FlowGraphService(FlowGraphRepository flowGraphRepository,
                            FlowNodeRepository flowNodeRepository,
                            FlowEdgeRepository flowEdgeRepository) {
        this.flowGraphRepository = flowGraphRepository;
        this.flowNodeRepository = flowNodeRepository;
        this.flowEdgeRepository = flowEdgeRepository;
    }

    public PageResult<FlowGraph> listPaged(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<FlowGraph> result = flowGraphRepository.findAll(pageable);
        return PageResult.of(result.getContent(), result.getTotalElements(), page + 1, size);
    }

    public FlowGraph getById(Long id) {
        return flowGraphRepository.findById(id).orElse(null);
    }

    @Transactional
    public FlowGraph save(FlowGraph flowGraph) {
        flowGraph.setId(null); // 新增时忽略客户端传入的 id
        return flowGraphRepository.save(flowGraph);
    }

    @Transactional
    public FlowGraph update(Long id, FlowGraph flowGraph) {
        FlowGraph existing = flowGraphRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("flowGraph not found: " + id));
        existing.setFlowGraph(flowGraph.getFlowGraph());
        existing.setWidth(flowGraph.getWidth());
        existing.setHeght(flowGraph.getHeght());
        existing.setRemark(flowGraph.getRemark());
        return flowGraphRepository.save(existing);
    }

    @Transactional
    public void deleteById(Long id) {
        FlowGraph graph = flowGraphRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("flowGraph not found: " + id));
        // 级联清理：先删该图下的边与节点，再删图，单事务保证不留孤儿数据
        flowEdgeRepository.deleteByFlowGraph(graph.getFlowGraph());
        flowNodeRepository.deleteByFlowGraph(graph.getFlowGraph());
        flowGraphRepository.delete(graph);
    }

    public List<FlowGraph> findByFlowGraph(String flowGraph) {
        return flowGraphRepository.findByFlowGraph(flowGraph);
    }
}
