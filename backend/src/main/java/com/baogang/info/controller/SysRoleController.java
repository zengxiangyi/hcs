package com.baogang.info.controller;

import com.baogang.info.common.ApiResponse;
import com.baogang.info.common.PageParam;
import com.baogang.info.common.PageResult;
import com.baogang.info.dto.SysRoleQuery;
import com.baogang.info.entity.SysRole;
import com.baogang.info.exception.ResourceNotFoundException;
import com.baogang.info.service.SysRoleService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/sysRole")
public class SysRoleController {

    private final SysRoleService sysRoleService;

    public SysRoleController(SysRoleService sysRoleService) {
        this.sysRoleService = sysRoleService;
    }

    // 复杂/可变条件查询：POST 请求体承载 SysRoleQuery，支持任意字段组合过滤
    @PostMapping("/search")
    public ApiResponse<PageResult<SysRole>> searchByQuery(@RequestBody SysRoleQuery query) {
        PageParam p = PageParam.of(query.getPage(), query.getPageSize());
        return ApiResponse.success(sysRoleService.search(query, p.page0(), p.size()));
    }

    @GetMapping("/{id}")
    public ApiResponse<SysRole> getById(@PathVariable Long id) {
        SysRole sysRole = sysRoleService.getById(id);
        if (sysRole == null) {
            throw new ResourceNotFoundException("sysRole not found: " + id);
        }
        return ApiResponse.success(sysRole);
    }

    @GetMapping("/code/{code}")
    public ApiResponse<SysRole> getByCode(@PathVariable String code) {
        SysRole sysRole = sysRoleService.getByCode(code);
        if (sysRole == null) {
            throw new ResourceNotFoundException("sysRole not found: " + code);
        }
        return ApiResponse.success(sysRole);
    }

    @PostMapping("/save")
    public ApiResponse<SysRole> save(@Valid @RequestBody SysRole sysRole) {
        return ApiResponse.success(sysRoleService.save(sysRole));
    }

    // 修改：路由统一为 PUT /update，id 由请求体携带
    @PutMapping("/update")
    public ApiResponse<SysRole> update(@Valid @RequestBody SysRole sysRole) {
        if (sysRole.getId() == null) {
            throw new IllegalArgumentException("修改操作必须传入 id");
        }
        return ApiResponse.success(sysRoleService.update(sysRole.getId(), sysRole));
    }

    @DeleteMapping("/code/{code}")
    public ApiResponse<String> delete(@PathVariable String code) {
        // 级联删除（权限绑定 + 用户绑定 + 角色本体）已下沉到 Service 单事务内完成
        sysRoleService.deleteCascadeByCode(code);
        return ApiResponse.success("处理完毕");
    }

    @GetMapping("/category/{category}")
    public ApiResponse<List<SysRole>> findByCategory(@PathVariable String category) {
        return ApiResponse.success(sysRoleService.findByCategory(category));
    }
}
