package com.baogang.info.controller;

import com.baogang.info.common.ApiResponse;
import com.baogang.info.common.PageParam;
import com.baogang.info.common.PageResult;
import com.baogang.info.dto.SysUserQuery;
import com.baogang.info.entity.SysUser;
import com.baogang.info.exception.ResourceNotFoundException;
import com.baogang.info.service.SysUserService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/sysUser")
public class SysUserController {

    private final SysUserService sysUserService;

    public SysUserController(SysUserService sysUserService) {
        this.sysUserService = sysUserService;
    }

    // 复杂/可变条件查询：POST 请求体承载 SysUserQuery，支持任意字段组合过滤
    @PostMapping("/search")
    public ApiResponse<PageResult<SysUser>> searchByQuery(@RequestBody SysUserQuery query) {
        PageParam p = PageParam.of(query.getPage(), query.getPageSize());
        return ApiResponse.success(sysUserService.search(query, p.offset(), p.size()));
    }

    @GetMapping("/{id}")
    public ApiResponse<SysUser> getById(@PathVariable Long id) {
        SysUser sysUser = sysUserService.getById(id);
        if (sysUser == null) {
            throw new ResourceNotFoundException("sysUser not found: " + id);
        }
        return ApiResponse.success(sysUser);
    }

    @GetMapping("/code/{code}")
    public ApiResponse<SysUser> getByCode(@PathVariable String code) {
        SysUser sysUser = sysUserService.getByCode(code);
        if (sysUser == null) {
            throw new ResourceNotFoundException("sysUser not found: " + code);
        }
        return ApiResponse.success(sysUser);
    }

    @PostMapping("/save")
    public ApiResponse<SysUser> save(@Valid @RequestBody SysUser sysUser) {
        return ApiResponse.success(sysUserService.save(sysUser));
    }

    // 修改：路由统一为 PUT /update，id 由请求体携带，createTime/createUser 由 service 保留原值
    @PutMapping("/update")
    public ApiResponse<SysUser> update(@Valid @RequestBody SysUser sysUser) {
        if (sysUser.getId() == null) {
            throw new IllegalArgumentException("修改操作必须传入 id");
        }
        return ApiResponse.success(sysUserService.update(sysUser.getId(), sysUser));
    }

    // 级联删除收进服务层单事务（角色绑定 + 用户），不存在时由服务层抛 404
    @DeleteMapping("/code/{code}")
    public ApiResponse<String> delete(@PathVariable String code) {
        sysUserService.deleteCascadeByCode(code);
        return ApiResponse.success("删除处理完毕");
    }
}
