package com.baogang.info.controller;

import com.baogang.info.common.ApiResponse;
import com.baogang.info.common.PageParam;
import com.baogang.info.common.PageResult;
import com.baogang.info.dto.ConstValueQuery;
import com.baogang.info.entity.ConstValue;
import com.baogang.info.exception.ResourceNotFoundException;
import com.baogang.info.service.ConstValueService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

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
    public ApiResponse<PageResult<ConstValue>> search(@Valid @RequestBody ConstValueQuery query) {
        PageParam p = PageParam.of(query.getPage(), query.getPageSize());
        return ApiResponse.success(constValueService.search(query, p.offset(), p.size()));
    }

    @GetMapping("/{id}")
    public ApiResponse<ConstValue> getById(@PathVariable Long id) {
        ConstValue constValue = constValueService.getById(id);
        if (constValue == null) {
            throw new ResourceNotFoundException("constValue not found: " + id);
        }
        return ApiResponse.success(constValue);
    }

    @GetMapping("/code/{code}")
    public ApiResponse<ConstValue> getByCode(@PathVariable String code) {
        ConstValue constValue = constValueService.getByCode(code);
        if (constValue == null) {
            throw new ResourceNotFoundException("constValue not found: " + code);
        }
        return ApiResponse.success(constValue);
    }

    // 新增：路由统一为 POST /save（原为 POST ""）
    @PostMapping("/save")
    public ApiResponse<ConstValue> save(@Valid @RequestBody ConstValue constValue) {
        return ApiResponse.success(constValueService.save(constValue));
    }

    // 修改：路由统一为 PUT /update，id 由请求体携带
    @PutMapping("/update")
    public ApiResponse<ConstValue> update(@Valid @RequestBody ConstValue constValue) {
        if (constValue.getId() == null) {
            throw new IllegalArgumentException("修改操作必须传入 id");
        }
        return ApiResponse.success(constValueService.update(constValue.getId(), constValue));
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
