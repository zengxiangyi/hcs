package com.baogang.info.service;

import com.baogang.info.common.PageResult;
import com.baogang.info.entity.FlowCurrent;
import com.baogang.info.exception.ResourceNotFoundException;
import com.baogang.info.repository.FlowCurrentRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class FlowCurrentService {

    private final FlowCurrentRepository flowCurrentRepository;

    public FlowCurrentService(FlowCurrentRepository flowCurrentRepository) {
        this.flowCurrentRepository = flowCurrentRepository;
    }

    public PageResult<FlowCurrent> listPaged(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<FlowCurrent> result = flowCurrentRepository.findAll(pageable);
        return PageResult.of(result.getContent(), result.getTotalElements(), page, size);
    }

    public FlowCurrent getById(Long id) {
        return flowCurrentRepository.findById(id).orElse(null);
    }

    @Transactional
    public FlowCurrent save(FlowCurrent flowCurrent) {
        flowCurrent.setId(null); // 新增时忽略客户端传入的 id
        return flowCurrentRepository.save(flowCurrent);
    }

    @Transactional
    public FlowCurrent update(Long id, FlowCurrent flowCurrent) {
        FlowCurrent existing = getById(id);
        existing.setWorkflow(flowCurrent.getWorkflow());
        existing.setFlowNode(flowCurrent.getFlowNode());
        existing.setStartTime(flowCurrent.getStartTime());
        existing.setRemark(flowCurrent.getRemark());
        return flowCurrentRepository.save(existing);
    }

    @Transactional
    public void deleteById(Long id) {
        if (!flowCurrentRepository.existsById(id)) {
            throw new ResourceNotFoundException("flowCurrent not found: " + id);
        }
        flowCurrentRepository.deleteById(id);
    }

    public List<FlowCurrent> findByWorkflow(String workflow) {
        return flowCurrentRepository.findByWorkflow(workflow);
    }

    public List<FlowCurrent> findByFlowNode(String flowNode) {
        return flowCurrentRepository.findByFlowNode(flowNode);
    }

    @Transactional
    public void deleteByWorkflow(String workflow) {
        flowCurrentRepository.removeByWorkflow(workflow);
    }

}
