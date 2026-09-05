package com.baogang.info.service;

import com.baogang.info.common.PageResult;
import com.baogang.info.entity.FlowNode;
import com.baogang.info.exception.ResourceNotFoundException;
import com.baogang.info.repository.FlowEdgeRepository;
import com.baogang.info.repository.FlowNodeRepository;
import com.baogang.info.tool.CollectionTool;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class FlowNodeService {

    private final FlowNodeRepository flowNodeRepository;
    private final FlowEdgeRepository flowEdgeRepository;

    public FlowNodeService(FlowNodeRepository flowNodeRepository, FlowEdgeRepository flowEdgeRepository) {
        this.flowNodeRepository = flowNodeRepository;
        this.flowEdgeRepository = flowEdgeRepository;
    }

    public PageResult<FlowNode> listPaged(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<FlowNode> result = flowNodeRepository.findAll(pageable);
        return PageResult.of(result.getContent(), result.getTotalElements(), page + 1, size);
    }

    public FlowNode getById(Long id) {
        return flowNodeRepository.findById(id).orElse(null);
    }

    public FlowNode getByCode(String code) {
        return flowNodeRepository.findByCode(code).orElse(null);
    }

    @Transactional
    public FlowNode save(FlowNode flowNode) {
        flowNode.setId(null); // 新增时忽略客户端传入的 id
        if (flowNodeRepository.existsByCode(flowNode.getCode())) {
            throw new IllegalArgumentException("code already exists: " + flowNode.getCode());
        }
        return flowNodeRepository.save(flowNode);
    }

    @Transactional
    public FlowNode update(FlowNode flowNode) {
        List<FlowNode> oldData = getByFlowGraphAndCode(flowNode.getFlowGraph(),flowNode.getCode());
        if(CollectionTool.isNotEmpty(oldData)) {
            FlowNode existing = oldData.get(0);
            existing.setCode(flowNode.getCode());
            existing.setName(flowNode.getName());
            existing.setCategory(flowNode.getCategory());
            existing.setShape(flowNode.getShape());
            existing.setH(flowNode.getH());
            existing.setW(flowNode.getW());
            existing.setX(flowNode.getX());
            existing.setY(flowNode.getY());
            existing.setColor(flowNode.getColor());
            existing.setFlowGraph(flowNode.getFlowGraph());
            existing.setOperator(flowNode.getOperator());
            existing.setRoleList(flowNode.getRoleList());
            existing.setUserList(flowNode.getUserList());
            return flowNodeRepository.save(existing);
        }
        return flowNode;
    }

    @Transactional
    public void deleteById(Long id) {
        FlowNode node = flowNodeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("flowNode not found: " + id));
        // 级联清理：删除该图中以本节点为起点/终点的边，再删节点，避免留下悬空连线
        flowEdgeRepository.deleteByFlowGraphAndFromNode(node.getFlowGraph(), node.getCode());
        flowEdgeRepository.deleteByFlowGraphAndToNode(node.getFlowGraph(), node.getCode());
        flowNodeRepository.delete(node);
    }

    public List<FlowNode> findByCategory(String category) {
        return flowNodeRepository.findByCategory(category);
    }

    public List<FlowNode> getByFlowGraphAndCategory(String flowGraph, String category) {
        return flowNodeRepository.findByFlowGraphAndCategory(flowGraph, category);
    }

    public List<FlowNode> getByFlowGraphAndCode(String flowGraph,String code) {
        return flowNodeRepository.findByFlowGraphAndCode(flowGraph,code);
    }

    public List<FlowNode> findByFlowGraph(String flowGraph) {
        return flowNodeRepository.findByFlowGraph(flowGraph);
    }

}
