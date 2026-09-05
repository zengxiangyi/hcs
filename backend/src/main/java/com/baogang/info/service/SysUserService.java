package com.baogang.info.service;

import com.baogang.info.common.PageResult;
import com.baogang.info.dto.SysUserQuery;
import com.baogang.info.entity.SysUser;
import com.baogang.info.exception.ResourceNotFoundException;
import com.baogang.info.mapper.SysUserMapper;
import com.baogang.info.repository.SysUserRepository;
import com.baogang.info.tool.StringTool;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SysUserService {

    private final SysUserRepository sysUserRepository;
    private final SysUserMapper sysUserMapper;
    private final SysRoleUserService sysRoleUserService;

    public SysUserService(SysUserRepository sysUserRepository, SysUserMapper sysUserMapper,
                          SysRoleUserService sysRoleUserService) {
        this.sysUserRepository = sysUserRepository;
        this.sysUserMapper = sysUserMapper;
        this.sysRoleUserService = sysRoleUserService;
    }

    public SysUser getById(Long id) {
        return sysUserRepository.findById(id).orElse(null);
    }

    // 返回 null 的语义被 AuthController 登录/找回密码流程依赖，保持不变
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
        // 不存在时抛 404，避免此前 null 直接 NPE 变成 500
        SysUser existing = sysUserRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("sysUser not found: " + id));
        // code 是登录账号名，变更需查重，否则可造出重复 code
        if (StringTool.isNotBlank(sysUser.getCode()) && !sysUser.getCode().equals(existing.getCode())
                && sysUserRepository.existsByCode(sysUser.getCode())) {
            throw new IllegalArgumentException("code already exists: " + sysUser.getCode());
        }
        existing.setCode(sysUser.getCode());
        existing.setName(sysUser.getName());
        // 补齐此前遗漏的字段同步：password/email/cellphone（resetPassword 此前静默失效的根因）
        existing.setEmail(sysUser.getEmail());
        existing.setCellphone(sysUser.getCellphone());
        existing.setRemark(sysUser.getRemark());
        existing.setDepartment(sysUser.getDepartment());
        existing.setPosition(sysUser.getPosition());
        existing.setState(sysUser.getState());
        // 密码仅在传入非空时更新，避免全量 PUT 不带 password 时被清空
        if (StringTool.isNotBlank(sysUser.getPassword())) {
            existing.setPassword(sysUser.getPassword());
        }
        return sysUserRepository.save(existing);
    }

    @Transactional
    public void deleteByCode(String code) {
        if (sysUserRepository.deleteByCode(code) == 0) {
            throw new ResourceNotFoundException("sysUser not found: " + code);
        }
    }

    // 级联删除角色绑定 + 用户，收进同一事务，避免角色绑定已删而用户残留
    @Transactional
    public void deleteCascadeByCode(String code) {
        sysRoleUserService.deleteByUserCode(code);
        deleteByCode(code);
    }

    // 可变条件查询：接收 SysUserQuery，按非空字段动态拼接 WHERE（空条件即查全部）
    public PageResult<SysUser> search(SysUserQuery query, int pageOffset, int size) {
        long total = sysUserMapper.countByQuery(query);
        List<SysUser> content = sysUserMapper.query(query, (long) pageOffset * size, size);
        return PageResult.of(content, total, pageOffset + 1, size);
    }
}
