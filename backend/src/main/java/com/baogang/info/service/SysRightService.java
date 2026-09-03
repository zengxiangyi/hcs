package com.baogang.info.service;

import com.baogang.info.common.PageResult;
import com.baogang.info.dto.SysRightQuery;
import com.baogang.info.entity.SysRight;
import com.baogang.info.exception.ResourceNotFoundException;
import com.baogang.info.mapper.SysRightMapper;
import com.baogang.info.repository.SysRightRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SysRightService {

    private final SysRightRepository sysRightRepository;
    private final SysRightMapper sysRightMapper;

    public SysRightService(SysRightRepository sysRightRepository, SysRightMapper sysRightMapper) {
        this.sysRightRepository = sysRightRepository;
        this.sysRightMapper = sysRightMapper;
    }

    public PageResult<SysRight> listPaged(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<SysRight> result = sysRightRepository.findAll(pageable);
        return PageResult.of(result.getContent(), result.getTotalElements(), page, size);
    }

    public SysRight getById(Long id) {
        return sysRightRepository.findById(id).orElse(null);
    }

    public SysRight getByCode(String code) {
        return sysRightRepository.findByCode(code).orElse(null);
    }

    @Transactional
    public SysRight save(SysRight sysRight) {
        sysRight.setId(null); // 新增时忽略客户端传入的 id
        if (sysRightRepository.existsByCode(sysRight.getCode())) {
            throw new IllegalArgumentException("code already exists: " + sysRight.getCode());
        }
        return sysRightRepository.save(sysRight);
    }

    @Transactional
    public SysRight update(Long id, SysRight sysRight) {
        SysRight existing = getById(id);
        existing.setCode(sysRight.getCode());
        existing.setName(sysRight.getName());
        existing.setCategory(sysRight.getCategory());
        existing.setParent(sysRight.getParent());
        existing.setRemark(sysRight.getRemark());
        return sysRightRepository.save(existing);
    }

    @Transactional
    public void deleteById(Long id) {
        if (!sysRightRepository.existsById(id)) {
            throw new ResourceNotFoundException("sysRight not found: " + id);
        }
        sysRightRepository.deleteById(id);
    }

    @Transactional
    public void deleteByCode(String code) {
        sysRightRepository.deleteByCode(code);
    }

    public List<SysRight> findByCategory(String category) {
        return sysRightRepository.findByCategory(category);
    }

    // 可变条件查询：接收 SysRightQuery，按非空字段动态拼接 WHERE（空条件即查全部）
    public PageResult<SysRight> search(SysRightQuery query, int pageOffset, int size) {
        long total = sysRightMapper.countByQuery(query);
        List<SysRight> content = sysRightMapper.query(query, (long) pageOffset * size, size);
        return PageResult.of(content, total, pageOffset + 1, size);
    }
}
