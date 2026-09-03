package com.baogang.info.controller;

import com.baogang.info.common.ApiResponse;
import com.baogang.info.common.PageResult;
import com.baogang.info.dto.ConstValueQuery;
import com.baogang.info.entity.ConstValue;
import com.baogang.info.service.ConstValueService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/constValue")
public class ConstValueController {

    private final ConstValueService constValueService;

    public ConstValueController(ConstValueService constValueService) {
        this.constValueService = constValueService;
    }

    // 复杂/可变条件查询：POST 请求体承载 ConstValueQuery，支持任意字段组合过滤
    @PostMapping("/search")
    public ApiResponse<PageResult<ConstValue>> searchByQuery(@RequestBody ConstValueQuery query) {
        int page = query.getPage();
        int size = query.getPageSize();
        if (page < 1 || size < 1) {
            return ApiResponse.error(400, "分页参数错误");
        }
        return ApiResponse.success(constValueService.search(query, page - 1, size));
    }

    @GetMapping("/{id}")
    public ApiResponse<ConstValue> getById(@PathVariable Long id) {
        return ApiResponse.success(constValueService.getById(id));
    }

    @GetMapping("/code/{code}")
    public ApiResponse<ConstValue> getByCode(@PathVariable String code) {
        return ApiResponse.success(constValueService.getByCode(code));
    }

    @PostMapping("/save")
    public ApiResponse<ConstValue> save(@Valid @RequestBody ConstValue constValue) {
        return ApiResponse.success(constValueService.save(constValue));
    }

    @PutMapping("/{id}")
    public ApiResponse<ConstValue> update(@PathVariable Long id, @Valid @RequestBody ConstValue constValue) {
        return ApiResponse.success(constValueService.update(id, constValue));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        constValueService.deleteById(id);
        return ApiResponse.success();
    }

    @GetMapping("/category/{category}")
    public ApiResponse<List<ConstValue>> findByCategory(@PathVariable String category) {
        return ApiResponse.success(constValueService.findByCategory(category));
    }
}
