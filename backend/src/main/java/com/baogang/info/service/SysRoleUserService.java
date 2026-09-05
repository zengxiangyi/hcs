package com.baogang.info.service;

import com.baogang.info.common.PageResult;
import com.baogang.info.entity.SysRoleUser;
import com.baogang.info.exception.ResourceNotFoundException;
import com.baogang.info.repository.SysRoleUserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SysRoleUserService {

    private final SysRoleUserRepository sysRoleUserRepository;

    public SysRoleUserService(SysRoleUserRepository sysRoleUserRepository) {
        this.sysRoleUserRepository = sysRoleUserRepository;
    }

    public PageResult<SysRoleUser> listPaged(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<SysRoleUser> result = sysRoleUserRepository.findAll(pageable);
        return PageResult.of(result.getContent(), result.getTotalElements(), page + 1, size);
    }

    public SysRoleUser getById(Long id) {
        return sysRoleUserRepository.findById(id).orElse(null);
    }

    @Transactional
    public SysRoleUser save(SysRoleUser sysRoleUser) {
        sysRoleUser.setId(null); // 新增时忽略客户端传入的 id
        return sysRoleUserRepository.save(sysRoleUser);
    }

    @Transactional
    public SysRoleUser update(Long id, SysRoleUser sysRoleUser) {
        SysRoleUser existing = sysRoleUserRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("sysRoleUser not found: " + id));
        existing.setRoleCode(sysRoleUser.getRoleCode());
        existing.setUserCode(sysRoleUser.getUserCode());
        existing.setRemark(sysRoleUser.getRemark());
        return sysRoleUserRepository.save(existing);
    }

    @Transactional
    public void deleteById(Long id) {
        if (!sysRoleUserRepository.existsById(id)) {
            throw new ResourceNotFoundException("sysRoleUser not found: " + id);
        }
        sysRoleUserRepository.deleteById(id);
    }

    public List<SysRoleUser> findByRoleCode(String roleCode) {
        return sysRoleUserRepository.findByRoleCode(roleCode);
    }

    public List<SysRoleUser> findByUserCode(String userCode) {
        return sysRoleUserRepository.findByUserCode(userCode);
    }

    public List<String> findRolesByuserCode(String userCode) {
        return sysRoleUserRepository.findRolesByUserCode(userCode);
    }

    @Transactional
    public void deleteByRoleCode(String roleCode) {
        sysRoleUserRepository.deleteByRoleCode(roleCode);
    }

    @Transactional
    public void deleteByUserCode(String userCode) {
        sysRoleUserRepository.deleteByUserCode(userCode);
    }

    public SysRoleUser findByRoleCodeAndUserCode(String roleCode, String userCode) {
        return sysRoleUserRepository.findByRoleCodeAndUserCode(roleCode, userCode);
    }
}
