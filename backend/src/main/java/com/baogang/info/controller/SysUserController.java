package com.baogang.info.controller;

import com.baogang.info.common.ApiResponse;
import com.baogang.info.common.PageResult;
import com.baogang.info.dto.SysUserQuery;
import com.baogang.info.entity.SysUser;
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

    private static final int MAX_PAGE_SIZE = 200;

    private final SysUserService sysUserService;

    public SysUserController(SysUserService sysUserService) {
        this.sysUserService = sysUserService;
    }

    // 复杂/可变条件查询：POST 请求体承载 SysUserQuery，支持任意字段组合过滤
    @PostMapping("/search")
    public ApiResponse<PageResult<SysUser>> searchByQuery(@RequestBody SysUserQuery query) {
        // 分页防护：page/pageSize 为 null 时退回 DTO 默认值；page 最小 1；size 限 1~200，防负 offset 报错与超大结果集
        int page = query.getPage() == null ? 1 : Math.max(1, query.getPage());
        int size = query.getPageSize() == null ? 10 : Math.min(Math.max(1, query.getPageSize()), MAX_PAGE_SIZE);
        return ApiResponse.success(sysUserService.search(query, page - 1, size));
    }

    @GetMapping("/{id}")
    public ApiResponse<SysUser> getById(@PathVariable Long id) {
        return ApiResponse.success(sysUserService.getById(id));
    }

    @GetMapping("/code/{code}")
    public ApiResponse<SysUser> getByCode(@PathVariable String code) {
        return ApiResponse.success(sysUserService.getByCode(code));
    }

    @PostMapping("/save")
    public ApiResponse<SysUser> save(@Valid @RequestBody SysUser sysUser) {
        return ApiResponse.success(sysUserService.save(sysUser));
    }

    @PutMapping("/{id}")
    public ApiResponse<SysUser> update(@PathVariable Long id, @Valid @RequestBody SysUser sysUser) {
        return ApiResponse.success(sysUserService.update(id, sysUser));
    }

    // 级联删除收进服务层单事务（角色绑定 + 用户），不存在时由服务层抛 404
    @DeleteMapping("/code/{code}")
    public ApiResponse<String> delete(@PathVariable String code) {
        sysUserService.deleteCascadeByCode(code);
        return ApiResponse.success("删除处理完毕");
    }
}
