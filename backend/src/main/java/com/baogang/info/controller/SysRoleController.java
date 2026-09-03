package com.baogang.info.controller;

import com.baogang.info.common.ApiResponse;
import com.baogang.info.common.PageResult;
import com.baogang.info.dto.SysRoleQuery;
import com.baogang.info.entity.SysRole;
import com.baogang.info.service.SysRoleRightService;
import com.baogang.info.service.SysRoleService;
import com.baogang.info.service.SysRoleUserService;
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
@RequestMapping("/sysRole")
public class SysRoleController {

    private final SysRoleService sysRoleService;
    private final SysRoleRightService sysRoleRightService;
    private final SysRoleUserService sysRoleUserService;


    public SysRoleController(SysRoleService sysRoleService, SysRoleRightService sysRoleRightService, SysRoleUserService sysRoleUserService) {
        this.sysRoleService = sysRoleService;
        this.sysRoleRightService = sysRoleRightService;
        this.sysRoleUserService = sysRoleUserService;
    }

    // 复杂/可变条件查询：POST 请求体承载 SysRoleQuery，支持任意字段组合过滤
    @PostMapping("/search")
    public ApiResponse<PageResult<SysRole>> searchByQuery(@RequestBody SysRoleQuery query) {
        int page = query.getPage();
        int size = query.getPageSize();
        return ApiResponse.success(sysRoleService.search(query, page - 1, size));
    }

    @GetMapping("/{id}")
    public ApiResponse<SysRole> getById(@PathVariable Long id) {
        return ApiResponse.success(sysRoleService.getById(id));
    }

    @GetMapping("/code/{code}")
    public ApiResponse<SysRole> getByCode(@PathVariable String code) {
        return ApiResponse.success(sysRoleService.getByCode(code));
    }

    @PostMapping("/save")
    public ApiResponse<SysRole> save(@Valid @RequestBody SysRole sysRole) {
        return ApiResponse.success(sysRoleService.save(sysRole));
    }

    @PutMapping("/{id}")
    public ApiResponse<SysRole> update(@PathVariable Long id, @Valid @RequestBody SysRole sysRole) {
        return ApiResponse.success(sysRoleService.update(id, sysRole));
    }

    @DeleteMapping("/code/{code}")
    public ApiResponse<String> delete(@PathVariable String code) {
        if(StringTool.isNotBlank(code)){
            // 级联删除角色权限绑定
            sysRoleRightService.deleteByRoleCode(code);
            sysRoleUserService.deleteByRoleCode(code);
            sysRoleService.deleteByCode(code);
            return ApiResponse.success("处理完毕");
        }
        return ApiResponse.error(400,"参数错误");
    }

    @GetMapping("/category/{category}")
    public ApiResponse<List<SysRole>> findByCategory(@PathVariable String category) {
        return ApiResponse.success(sysRoleService.findByCategory(category));
    }
}
