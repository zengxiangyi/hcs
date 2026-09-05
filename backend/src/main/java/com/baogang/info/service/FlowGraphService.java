package com.baogang.info.service;

import com.baogang.info.common.PageResult;
import com.baogang.info.entity.FlowGraph;
import com.baogang.info.exception.ResourceNotFoundException;
import com.baogang.info.repository.FlowGraphRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class FlowGraphService {

    private final FlowGraphRepository flowGraphRepository;

    public FlowGraphService(FlowGraphRepository flowGraphRepository) {
        this.flowGraphRepository = flowGraphRepository;
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
        FlowGraph existing = getById(id);
        existing.setFlowGraph(flowGraph.getFlowGraph());
        existing.setWidth(flowGraph.getWidth());
        existing.setHeght(flowGraph.getHeght());
        existing.setRemark(flowGraph.getRemark());
        return flowGraphRepository.save(existing);
    }

    @Transactional
    public void deleteById(Long id) {
        if (!flowGraphRepository.existsById(id)) {
            throw new ResourceNotFoundException("flowMap not found: " + id);
        }
        flowGraphRepository.deleteById(id);
    }

    public List<FlowGraph> findByFlowGraph(String flowGraph) {
        return flowGraphRepository.findByFlowGraph(flowGraph);
    }
}
