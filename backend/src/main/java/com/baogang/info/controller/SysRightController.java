package com.baogang.info.controller;

import com.baogang.info.common.ApiResponse;
import com.baogang.info.common.PageParam;
import com.baogang.info.common.PageResult;
import com.baogang.info.dto.SysRightQuery;
import com.baogang.info.entity.SysRight;
import com.baogang.info.exception.ResourceNotFoundException;
import com.baogang.info.service.SysRightService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/sysRight")
public class SysRightController {

    private final SysRightService sysRightService;

    public SysRightController(SysRightService sysRightService) {
        this.sysRightService = sysRightService;
    }

    // 复杂/可变条件查询：POST 请求体承载 SysRightQuery，支持任意字段组合过滤
    @PostMapping("/search")
    public ApiResponse<PageResult<SysRight>> searchByQuery(@RequestBody SysRightQuery query) {
        PageParam p = PageParam.of(query.getPage(), query.getPageSize());
        return ApiResponse.success(sysRightService.search(query, p.offset(), p.size()));
    }

    @GetMapping("/{id}")
    public ApiResponse<SysRight> getById(@PathVariable Long id) {
        SysRight sysRight = sysRightService.getById(id);
        if (sysRight == null) {
            throw new ResourceNotFoundException("sysRight not found: " + id);
        }
        return ApiResponse.success(sysRight);
    }

    @GetMapping("/code/{code}")
    public ApiResponse<SysRight> getByCode(@PathVariable String code) {
        SysRight sysRight = sysRightService.getByCode(code);
        if (sysRight == null) {
            throw new ResourceNotFoundException("sysRight not found: " + code);
        }
        return ApiResponse.success(sysRight);
    }

    @PostMapping("/save")
    public ApiResponse<SysRight> save(@Valid @RequestBody SysRight sysRight) {
        return ApiResponse.success(sysRightService.save(sysRight));
    }

    // 修改：路由统一为 PUT /update，id 由请求体携带
    @PutMapping("/update")
    public ApiResponse<SysRight> update(@Valid @RequestBody SysRight sysRight) {
        if (sysRight.getId() == null) {
            throw new IllegalArgumentException("修改操作必须传入 id");
        }
        return ApiResponse.success(sysRightService.update(sysRight.getId(), sysRight));
    }

    @DeleteMapping("/code/{code}")
    public ApiResponse<String> delete(@PathVariable String code) {
        // 级联删除（角色权限绑定 + 权限本体）已下沉到 Service 单事务内完成
        sysRightService.deleteCascadeByCode(code);
        return ApiResponse.success("处理完毕");
    }

    @GetMapping("/category/{category}")
    public ApiResponse<List<SysRight>> findByCategory(@PathVariable String category) {
        return ApiResponse.success(sysRightService.findByCategory(category));
    }
}
