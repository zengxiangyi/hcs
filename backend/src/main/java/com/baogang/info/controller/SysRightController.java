package com.baogang.info.controller;

import com.baogang.info.common.ApiResponse;
import com.baogang.info.common.PageParam;
import com.baogang.info.common.PageResult;
import com.baogang.info.dto.SysRightQuery;
import com.baogang.info.entity.SysRight;
import com.baogang.info.service.SysRightService;
import com.baogang.info.service.SysRoleRightService;
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
@RequestMapping("/sysRight")
public class SysRightController {

    private final SysRightService sysRightService;
    private final SysRoleRightService sysRoleRightService;

    public SysRightController(SysRightService sysRightService, SysRoleRightService sysRoleRightService) {
        this.sysRightService = sysRightService;
        this.sysRoleRightService = sysRoleRightService;
    }

    // 复杂/可变条件查询：POST 请求体承载 SysRightQuery，支持任意字段组合过滤
    @PostMapping("/search")
    public ApiResponse<PageResult<SysRight>> searchByQuery(@RequestBody SysRightQuery query) {
        PageParam p = PageParam.of(query.getPage(), query.getPageSize());
        return ApiResponse.success(sysRightService.search(query, p.offset(), p.size()));
    }

    @GetMapping("/{id}")
    public ApiResponse<SysRight> getById(@PathVariable Long id) {
        return ApiResponse.success(sysRightService.getById(id));
    }

    @GetMapping("/code/{code}")
    public ApiResponse<SysRight> getByCode(@PathVariable String code) {
        return ApiResponse.success(sysRightService.getByCode(code));
    }

    @PostMapping("/save")
    public ApiResponse<SysRight> save(@Valid @RequestBody SysRight sysRight) {
        return ApiResponse.success(sysRightService.save(sysRight));
    }

    @PutMapping("/{id}")
    public ApiResponse<SysRight> update(@PathVariable Long id, @Valid @RequestBody SysRight sysRight) {
        return ApiResponse.success(sysRightService.update(id, sysRight));
    }

    @DeleteMapping("/code/{code}")
    public ApiResponse<String> delete(@PathVariable String code) {
        if(StringTool.isNotBlank(code)) {
            sysRightService.deleteByCode(code);
            // 级联删除角色权限绑定
            sysRoleRightService.deleteByRightCode(code);
            return ApiResponse.success("处理完毕");
        }
        return ApiResponse.error(400,"参数错误");
    }

    @GetMapping("/category/{category}")
    public ApiResponse<List<SysRight>> findByCategory(@PathVariable String category) {
        return ApiResponse.success(sysRightService.findByCategory(category));
    }
}
