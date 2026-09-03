package com.baogang.info.service;

import com.baogang.info.common.PageResult;
import com.baogang.info.entity.FlowHistory;
import com.baogang.info.exception.ResourceNotFoundException;
import com.baogang.info.repository.FlowHistoryRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class FlowHistoryService {

    private final FlowHistoryRepository flowHistoryRepository;

    public FlowHistoryService(FlowHistoryRepository flowHistoryRepository) {
        this.flowHistoryRepository = flowHistoryRepository;
    }

    public PageResult<FlowHistory> listPaged(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<FlowHistory> result = flowHistoryRepository.findAll(pageable);
        return PageResult.of(result.getContent(), result.getTotalElements(), page, size);
    }

    public FlowHistory getById(Long id) {
        return flowHistoryRepository.findById(id).orElse(null);
    }

    @Transactional
    public FlowHistory save(FlowHistory flowHistory) {
        flowHistory.setId(null); // 新增时忽略客户端传入的 id
        return flowHistoryRepository.save(flowHistory);
    }

    @Transactional
    public FlowHistory update(Long id, FlowHistory flowHistory) {
        FlowHistory existing = getById(id);
        existing.setWorkflow(flowHistory.getWorkflow());
        existing.setDealTime(flowHistory.getDealTime());
        existing.setDealUser(flowHistory.getDealUser());
        existing.setUserName(flowHistory.getUserName());
        existing.setRemark(flowHistory.getRemark());
        existing.setAction(flowHistory.getAction());
        existing.setNote(flowHistory.getNote());
        return flowHistoryRepository.save(existing);
    }

    @Transactional
    public void deleteById(Long id) {
        if (!flowHistoryRepository.existsById(id)) {
            throw new ResourceNotFoundException("flowHistory not found: " + id);
        }
        flowHistoryRepository.deleteById(id);
    }

    public List<FlowHistory> findByWorkflow(String workflow) {
        return flowHistoryRepository.findByWorkflowOrderByDealTimeDesc(workflow);
    }

    public List<FlowHistory> findByDealUser(String dealUser) {
        return flowHistoryRepository.findByDealUser(dealUser);
    }
}
