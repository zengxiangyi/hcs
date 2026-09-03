package com.baogang.info.service;

import com.baogang.info.common.PageResult;
import com.baogang.info.dto.SysRoleQuery;
import com.baogang.info.entity.SysRole;
import com.baogang.info.exception.ResourceNotFoundException;
import com.baogang.info.mapper.SysRoleMapper;
import com.baogang.info.repository.SysRoleRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SysRoleService {

    private final SysRoleRepository sysRoleRepository;
    private final SysRoleMapper sysRoleMapper;

    public SysRoleService(SysRoleRepository sysRoleRepository, SysRoleMapper sysRoleMapper) {
        this.sysRoleRepository = sysRoleRepository;
        this.sysRoleMapper = sysRoleMapper;
    }

    public PageResult<SysRole> listPaged(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<SysRole> result = sysRoleRepository.findAll(pageable);
        return PageResult.of(result.getContent(), result.getTotalElements(), page, size);
    }

    public SysRole getById(Long id) {
        return sysRoleRepository.findById(id).orElse(null);
    }

    public SysRole getByCode(String code) {
        return sysRoleRepository.findByCode(code).orElse(null);
    }

    @Transactional
    public SysRole save(SysRole sysRole) {
        sysRole.setId(null); // 新增时忽略客户端传入的 id
        if (sysRoleRepository.existsByCode(sysRole.getCode())) {
            throw new IllegalArgumentException("code already exists: " + sysRole.getCode());
        }
        return sysRoleRepository.save(sysRole);
    }

    @Transactional
    public SysRole update(Long id, SysRole sysRole) {
        SysRole existing = getById(id);
        existing.setCode(sysRole.getCode());
        existing.setName(sysRole.getName());
        existing.setCategory(sysRole.getCategory());
        existing.setRemark(sysRole.getRemark());
        return sysRoleRepository.save(existing);
    }

    @Transactional
    public void deleteByCode(String code) {
        sysRoleRepository.deleteByCode(code);
    }

    public List<SysRole> findByCategory(String category) {
        return sysRoleRepository.findByCategory(category);
    }

    // 可变条件查询：接收 SysRoleQuery，按非空字段动态拼接 WHERE（空条件即查全部）
    public PageResult<SysRole> search(SysRoleQuery query, int pageOffset, int size) {
        long total = sysRoleMapper.countByQuery(query);
        List<SysRole> content = sysRoleMapper.query(query, (long) pageOffset * size, size);
        return PageResult.of(content, total, pageOffset + 1, size);
    }
}
