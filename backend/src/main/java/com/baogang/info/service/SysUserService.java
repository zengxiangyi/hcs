package com.baogang.info.service;

import com.baogang.info.common.PageResult;
import com.baogang.info.dto.SysUserQuery;
import com.baogang.info.entity.SysUser;
import com.baogang.info.exception.ResourceNotFoundException;
import com.baogang.info.mapper.SysUserMapper;
import com.baogang.info.repository.SysUserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SysUserService {

    private final SysUserRepository sysUserRepository;
    private final SysUserMapper sysUserMapper;

    public SysUserService(SysUserRepository sysUserRepository, SysUserMapper sysUserMapper) {
        this.sysUserRepository = sysUserRepository;
        this.sysUserMapper = sysUserMapper;
    }

    public PageResult<SysUser> listPaged(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<SysUser> result = sysUserRepository.findAll(pageable);
        return PageResult.of(result.getContent(), result.getTotalElements(), page, size);
    }

    public SysUser getById(Long id) {
        return sysUserRepository.findById(id).orElse(null);
    }

    public SysUser getByCode(String code) {
        return sysUserRepository.findByCode(code).orElse(null);
    }

    @Transactional
    public SysUser save(SysUser sysUser) {
        sysUser.setId(null); // 新增时忽略客户端传入的 id
        if (sysUserRepository.existsByCode(sysUser.getCode())) {
            throw new IllegalArgumentException("code already exists: " + sysUser.getCode());
        }
        return sysUserRepository.save(sysUser);
    }

    @Transactional
    public SysUser update(Long id, SysUser sysUser) {
        SysUser existing = getById(id);
        existing.setCode(sysUser.getCode());
        existing.setName(sysUser.getName());
        existing.setRemark(sysUser.getRemark());
        existing.setDepartment(sysUser.getDepartment());
        existing.setPosition(sysUser.getPosition());
        existing.setState(sysUser.getState());
        return sysUserRepository.save(existing);
    }

    @Transactional
    public void deleteByCode(String code) {
        sysUserRepository.deleteByCode(code);
    }

    // 可变条件查询：接收 SysUserQuery，按非空字段动态拼接 WHERE（空条件即查全部）
    public PageResult<SysUser> search(SysUserQuery query, int pageOffset, int size) {
        long total = sysUserMapper.countByQuery(query);
        List<SysUser> content = sysUserMapper.query(query, (long) pageOffset * size, size);
        return PageResult.of(content, total, pageOffset + 1, size);
    }
}
