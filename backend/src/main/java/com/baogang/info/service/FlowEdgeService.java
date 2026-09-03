package com.baogang.info.service;

import com.baogang.info.common.PageResult;
import com.baogang.info.entity.FlowEdge;
import com.baogang.info.exception.ResourceNotFoundException;
import com.baogang.info.repository.FlowEdgeRepository;
import com.baogang.info.tool.CollectionTool;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.OpenOption;
import java.util.List;

@Service
public class FlowEdgeService {

    private final FlowEdgeRepository flowEdgeRepository;

    public FlowEdgeService(FlowEdgeRepository flowEdgeRepository) {
        this.flowEdgeRepository = flowEdgeRepository;
    }

    public PageResult<FlowEdge> listPaged(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<FlowEdge> result = flowEdgeRepository.findAll(pageable);
        return PageResult.of(result.getContent(), result.getTotalElements(), page, size);
    }

    public FlowEdge getById(Long id) {
        return flowEdgeRepository.findById(id).orElse(null);
    }

    public FlowEdge getByCode(String code) {
        return flowEdgeRepository.findByCode(code).orElse(null);
    }

    @Transactional
    public FlowEdge save(FlowEdge flowEdge) {
        flowEdge.setId(null); // 新增时忽略客户端传入的 id
        if (flowEdgeRepository.existsByCode(flowEdge.getCode())) {
            throw new IllegalArgumentException("code already exists: " + flowEdge.getCode());
        }
        return flowEdgeRepository.save(flowEdge);
    }

    @Transactional
    public FlowEdge update(FlowEdge flowEdge) {
        List<FlowEdge> oldData = findByFlowGraphAndCode(flowEdge.getFlowGraph(),flowEdge.getCode());
        if(CollectionTool.isNotEmpty(oldData)) {
            FlowEdge existing = oldData.get(0);
            existing.setCode(flowEdge.getCode());
            existing.setName(flowEdge.getName());
            existing.setColor(flowEdge.getColor());
            existing.setFromNode(flowEdge.getFromNode());
            existing.setToNode(flowEdge.getToNode());
            existing.setAxis(flowEdge.getAxis());
            existing.setFlowGraph(flowEdge.getFlowGraph());
            existing.setCategory(flowEdge.getCategory());
            existing.setCond(flowEdge.getCond());
            existing.setRemark(flowEdge.getRemark());
            return flowEdgeRepository.save(existing);
        }
        return flowEdge;
    }

    @Transactional
    public void deleteById(Long id) {
        if (!flowEdgeRepository.existsById(id)) {
            throw new ResourceNotFoundException("flowEdge not found: " + id);
        }
        flowEdgeRepository.deleteById(id);
    }

    public List<FlowEdge> findByFromNode(String fromNode) {
        return flowEdgeRepository.findByFromNode(fromNode);
    }

    public List<FlowEdge> findByToNode(String toNode) {
        return flowEdgeRepository.findByToNode(toNode);
    }


    public List<FlowEdge> findByFlowGraphAndFromNode(String flowGraph,String fromNode) {
        return flowEdgeRepository.findByFlowGraphAndFromNode(flowGraph,fromNode);
    }

    public List<FlowEdge> findByFlowGraphAndCode(String flowGraph, String code) {
        return flowEdgeRepository.findByFlowGraphAndCode(flowGraph,code);
    }

    public List<FlowEdge> findByFlowGraph(String flowGraph) {
        return flowEdgeRepository.findByFlowGraph(flowGraph);
    }
}
