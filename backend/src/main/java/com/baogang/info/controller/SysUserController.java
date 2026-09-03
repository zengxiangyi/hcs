package com.baogang.info.controller;

import com.baogang.info.common.ApiResponse;
import com.baogang.info.common.PageResult;
import com.baogang.info.dto.SysUserQuery;
import com.baogang.info.entity.SysUser;
import com.baogang.info.service.SysRoleUserService;
import com.baogang.info.service.SysUserService;
import com.baogang.info.tool.StringTool;
import jakarta.validation.Valid;
import java.util.List;
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
    private final SysRoleUserService sysRoleUserService;

    public SysUserController(SysUserService sysUserService, SysRoleUserService sysRoleUserService) {
        this.sysUserService = sysUserService;
        this.sysRoleUserService = sysRoleUserService;
    }

    // 复杂/可变条件查询：POST 请求体承载 SysUserQuery，支持任意字段组合过滤
    @PostMapping("/search")
    public ApiResponse<PageResult<SysUser>> searchByQuery(@RequestBody SysUserQuery query) {
        int page = query.getPage();
        int size = query.getPageSize();
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

    @DeleteMapping("/code/{code}")
    public ApiResponse<String> delete(@PathVariable String code) {
        if(StringTool.isNotBlank(code)){
            sysRoleUserService.deleteByUserCode(code);
            sysUserService.deleteByCode(code);
            return ApiResponse.success("删除处理完毕");
        }
        return ApiResponse.error(400,"参数错误");
    }

}
