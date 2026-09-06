package com.baogang.info.service;

import com.baogang.info.common.PageResult;
import com.baogang.info.dto.WorkflowQuery;
import com.baogang.info.entity.Workflow;
import com.baogang.info.exception.ResourceNotFoundException;
import com.baogang.info.mapper.WorkflowMapper;
import com.baogang.info.repository.WorkflowRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class WorkflowService {

    private final WorkflowRepository workflowRepository;
    private final WorkflowMapper workflowMapper;

    public WorkflowService(WorkflowRepository workflowRepository, WorkflowMapper workflowMapper) {
        this.workflowRepository = workflowRepository;
        this.workflowMapper = workflowMapper;
    }

    public PageResult<Workflow> listPaged(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Workflow> result = workflowRepository.findAll(pageable);
        return PageResult.of(result.getContent(), result.getTotalElements(), page + 1, size);
    }

    public Workflow getById(Long id) {
        return workflowRepository.findById(id).orElse(null);
    }

    public Workflow getByCode(String code) {
        return workflowRepository.findByCode(code).orElse(null);
    }

    // 审批专用：悲观行锁读取，未命中抛 400（FlowEngine.dealNode 用）
    @Transactional
    public Workflow getByCodeForUpdate(String code) {
        return workflowRepository.findByCodeForUpdate(code)
                .orElseThrow(() -> new ResourceNotFoundException("workflow not found: " + code));
    }

    @Transactional
    public Workflow save(Workflow workflow) {
        workflow.setId(null); // 新增时忽略客户端传入的 id
        if (workflowRepository.existsByCode(workflow.getCode())) {
            throw new IllegalArgumentException("code already exists: " + workflow.getCode());
        }
        return workflowRepository.save(workflow);
    }

    @Transactional
    public Workflow update(Long id, Workflow workflow) {
        Workflow existing = workflowRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("workflow not found: " + id));
        existing.setCode(workflow.getCode());
        existing.setName(workflow.getName());
        existing.setState(workflow.getState());
        existing.setStartTime(workflow.getStartTime());
        existing.setEndTime(workflow.getEndTime());
        existing.setRemark(workflow.getRemark());
        existing.setFlowGraph(workflow.getFlowGraph());
        if (workflowRepository.existsByCodeAndIdNot(existing.getCode(), id)) {
            throw new IllegalArgumentException("code already exists: " + existing.getCode());
        }
        return workflowRepository.save(existing);
    }

    @Transactional
    public void deleteById(Long id) {
        if (!workflowRepository.existsById(id)) {
            throw new ResourceNotFoundException("workflow not found: " + id);
        }
        workflowRepository.deleteById(id);
    }

    public List<Workflow> findByState(String state) {
        return workflowRepository.findByState(state);
    }

    public List<Workflow> findByFlowGraph(String flowGraph) {
        return workflowRepository.findByFlowGraph(flowGraph);
    }

    // 以下分页方法的页码参数均为 0 基（由 Controller 的 PageParam.page0() 传入），
    // 响应 PageResult 时统一 +1 还原为契约 1 基页码

    public PageResult<Workflow> findBySender(String sender, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Workflow> result = workflowRepository.findBySender(sender, pageable);
        return PageResult.of(result.getContent(), result.getTotalElements(), page + 1, size);
    }

    public PageResult<Workflow> done(WorkflowQuery query, int pageOffset, int size) {
        long total = workflowMapper.countDone(query);
        List<Workflow> content = workflowMapper.queryDone(query, (long) pageOffset * size, size);
        return PageResult.of(content, total, pageOffset + 1, size);
    }

    public PageResult<Workflow> todo(WorkflowQuery query, int pageOffset, int size) {
        long total = workflowMapper.countTodo(query);
        List<Workflow> content = workflowMapper.queryTodo(query, (long) pageOffset * size, size);
        return PageResult.of(content, total, pageOffset + 1, size);
    }

    @Transactional
    public String changeState(String code,String state){

        int rows=workflowRepository.updateState(state,code);
        if(rows==0){
            throw new ResourceNotFoundException("workflow not found: " + code);
        }
        return "success";
    }

}
