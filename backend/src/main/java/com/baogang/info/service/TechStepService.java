package com.baogang.info.service;

import com.baogang.info.common.PageResult;
import com.baogang.info.dto.TechStepQuery;
import com.baogang.info.entity.TechStep;
import com.baogang.info.exception.ResourceNotFoundException;
import com.baogang.info.mapper.TechStepMapper;
import com.baogang.info.repository.TechStepRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TechStepService {

    private final TechStepRepository techStepRepository;
    private final TechStepMapper techStepMapper;

    public TechStepService(TechStepRepository techStepRepository, TechStepMapper techStepMapper) {
        this.techStepRepository = techStepRepository;
        this.techStepMapper = techStepMapper;
    }

    public TechStep getById(Long id) {
        return techStepRepository.findById(id).orElse(null);
    }

    @Transactional
    public TechStep save(TechStep techStep) {
        techStep.setId(null); // 新增时忽略客户端传入的 id
        return techStepRepository.save(techStep);
    }

    // 批量保存：单事务，任一条失败整体回滚，避免逐条提交产生部分写入
    @Transactional
    public void saveBatch(List<TechStep> techStepList) {
        for (TechStep techStep : techStepList) {
            techStep.setId(null); // 新增时忽略客户端传入的 id
        }
        techStepRepository.saveAll(techStepList);
    }

    // 修改：按 id 更新已有记录，逐字段同步到托管实体
    @Transactional
    public TechStep update(TechStep techStep) {
        if (techStep.getId() == null) {
            throw new IllegalArgumentException("修改操作必须传入 id");
        }
        TechStep existing = techStepRepository.findById(techStep.getId())
                .orElseThrow(() -> new ResourceNotFoundException("工序不存在：id=" + techStep.getId()));
        existing.setFirstLevel(techStep.getFirstLevel());
        existing.setSecondLevel(techStep.getSecondLevel());
        existing.setStep(techStep.getStep());
        existing.setStepName(techStep.getStepName());
        existing.setSort(techStep.getSort());
        existing.setIsNeed(techStep.getIsNeed());
        existing.setRemark(techStep.getRemark());
        // 配合 @DynamicUpdate：仅生成被改动列的 UPDATE
        return techStepRepository.save(existing);
    }

    @Transactional
    public void deleteById(Long id) {
        if (!techStepRepository.existsById(id)) {
            throw new ResourceNotFoundException("techStep not found: " + id);
        }
        techStepRepository.deleteById(id);
    }

    // 可变条件查询：接收 TechStepQuery，按非空字段动态拼接 WHERE（空条件即查全部）
    public PageResult<TechStep> search(TechStepQuery query, int pageOffset, int size) {
        long total = techStepMapper.countByQuery(query);
        List<TechStep> content = techStepMapper.query(query, (long) pageOffset * size, size);
        return PageResult.of(content, total, pageOffset + 1, size);
    }

    public List<TechStep> getByFirstLevel(String firstLevel) {
        return techStepRepository.findByFirstLevel(firstLevel);
    }

    public List<TechStep> getByFirstLevelAndSecondLevel(String firstLevel, String secondLevel) {
        return techStepRepository.findByFirstLevelAndSecondLevelOrderBySortAsc(firstLevel, secondLevel);
    }

    public List<TechStep> getByStep(String step) {
        return techStepRepository.findByStep(step);
    }
}
