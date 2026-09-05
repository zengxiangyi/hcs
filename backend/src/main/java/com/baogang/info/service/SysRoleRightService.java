package com.baogang.info.service;

import com.baogang.info.common.PageResult;
import com.baogang.info.entity.SysRoleRight;
import com.baogang.info.exception.ResourceNotFoundException;
import com.baogang.info.repository.SysRoleRightRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SysRoleRightService {

    private final SysRoleRightRepository sysRoleRightRepository;

    public SysRoleRightService(SysRoleRightRepository sysRoleRightRepository) {
        this.sysRoleRightRepository = sysRoleRightRepository;
    }

    public PageResult<SysRoleRight> listPaged(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<SysRoleRight> result = sysRoleRightRepository.findAll(pageable);
        return PageResult.of(result.getContent(), result.getTotalElements(), page + 1, size);
    }

    public SysRoleRight getById(Long id) {
        return sysRoleRightRepository.findById(id).orElse(null);
    }

    @Transactional
    public SysRoleRight save(SysRoleRight sysRoleRight) {
        sysRoleRight.setId(null); // 新增时忽略客户端传入的 id
        return sysRoleRightRepository.save(sysRoleRight);
    }

    @Transactional
    public SysRoleRight update(Long id, SysRoleRight sysRoleRight) {
        SysRoleRight existing = sysRoleRightRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("sysRoleRight not found: " + id));
        existing.setRoleCode(sysRoleRight.getRoleCode());
        existing.setRightCode(sysRoleRight.getRightCode());
        existing.setRemark(sysRoleRight.getRemark());
        return sysRoleRightRepository.save(existing);
    }

    @Transactional
    public void deleteById(Long id) {
        if (!sysRoleRightRepository.existsById(id)) {
            throw new ResourceNotFoundException("sysRoleRight not found: " + id);
        }
        sysRoleRightRepository.deleteById(id);
    }

    public List<SysRoleRight> findByRoleCode(String roleCode) {
        return sysRoleRightRepository.findByRoleCode(roleCode);
    }

    public List<SysRoleRight> findByRightCode(String rightCode) {
        return sysRoleRightRepository.findByRightCode(rightCode);
    }

    @Transactional
    public void deleteByRoleCode(String roleCode) {
        sysRoleRightRepository.deleteByRoleCode(roleCode);
    }

    @Transactional
    public void deleteByRightCode(String rightCode) {
        sysRoleRightRepository.deleteByRightCode(rightCode);
    }

    public SysRoleRight findByRoleCodeAndRightCode(String roleCode, String rightCode) {
        return sysRoleRightRepository.findByRoleCodeAndRightCode(roleCode,rightCode);
    }
}
