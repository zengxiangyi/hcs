package com.baogang.info.service;

import com.baogang.info.common.PageResult;
import com.baogang.info.dto.TaskProcessQuery;
import com.baogang.info.entity.TaskProcess;
import com.baogang.info.mapper.TaskProcessMapper;
import com.baogang.info.repository.TaskProcessRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TaskProcessService {

    private final TaskProcessRepository taskProcessRepository;
    private final TaskProcessMapper taskProcessMapper;

    public TaskProcessService(TaskProcessRepository taskProcessRepository,
                              TaskProcessMapper taskProcessMapper) {
        this.taskProcessRepository = taskProcessRepository;
        this.taskProcessMapper = taskProcessMapper;
    }

    public PageResult<TaskProcess> listPaged(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<TaskProcess> result = taskProcessRepository.findAll(pageable);
        return PageResult.of(result.getContent(), result.getTotalElements(), page, size);
    }

    @Transactional
    public TaskProcess save(TaskProcess taskProcess) {
        taskProcess.setId(null);  // 新增时忽略客户端传入的 id
        return taskProcessRepository.save(taskProcess);
    }

    @Transactional
    public TaskProcess update(TaskProcess taskProcess) {
        if (taskProcess.getId() == null) {
            throw new IllegalArgumentException("修改操作必须传入 id");
        }
        TaskProcess existing = taskProcessRepository.findById(taskProcess.getId())
                .orElseThrow(() -> new IllegalArgumentException("任务流程不存在：id=" + taskProcess.getId()));
        existing.setTransfer(taskProcess.getTransfer());
        existing.setBlueprint(taskProcess.getBlueprint());
        existing.setAuditUser(taskProcess.getAuditUser());
        existing.setAuditTime(taskProcess.getAuditTime());
        existing.setAuditMessage(taskProcess.getAuditMessage());
        existing.setAuditState(taskProcess.getAuditState());
        existing.setStep(taskProcess.getStep());
        existing.setState(taskProcess.getState());
        existing.setCreateUser(taskProcess.getCreateUser());
        existing.setCreateTime(taskProcess.getCreateTime());
        existing.setUpdateUser(taskProcess.getUpdateUser());
        existing.setUpdateTime(taskProcess.getUpdateTime());
        return taskProcessRepository.save(existing);
    }

    public TaskProcess getById(Long id) {
        return taskProcessRepository.findById(id).orElse(null);
    }

    public List<TaskProcess> getByState(String state) {
        return taskProcessRepository.findByState(state);
    }

    @Transactional
    public void deleteById(Long id) {
        taskProcessRepository.deleteById(id);
    }

    // 可变条件查询：接收 TaskProcessQuery，按非空字段动态拼接 WHERE（空条件即查全部）
    public PageResult<TaskProcess> search(TaskProcessQuery q, int pageOffset, int size) {
        long total = taskProcessMapper.countByQuery(q);
        List<TaskProcess> content = taskProcessMapper.query(q, (long) pageOffset * size, size);
        return PageResult.of(content, total, pageOffset + 1, size);
    }
}
