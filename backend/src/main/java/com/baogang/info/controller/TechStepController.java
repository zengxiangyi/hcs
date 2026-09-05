package com.baogang.info.controller;

import com.baogang.info.common.ApiResponse;
import com.baogang.info.common.PageParam;
import com.baogang.info.common.PageResult;
import com.baogang.info.dto.TechStepQuery;
import com.baogang.info.entity.TechStep;
import com.baogang.info.exception.ResourceNotFoundException;
import com.baogang.info.service.TechStepService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/techstep")
public class TechStepController {

    private final TechStepService techStepService;

    public TechStepController(TechStepService techStepService) {
        this.techStepService = techStepService;
    }

    // 复杂/可变条件查询：POST 请求体承载 TechStepQuery，支持任意字段组合过滤
    @PostMapping("/search")
    public ApiResponse<PageResult<TechStep>> searchByQuery(@RequestBody TechStepQuery query) {
        PageParam p = PageParam.of(query.getPage(), query.getPageSize());
        return ApiResponse.success(techStepService.search(query, p.offset(), p.size()));
    }

    @GetMapping("/{id}")
    public ApiResponse<TechStep> getById(@PathVariable Long id) {
        TechStep techStep = techStepService.getById(id);
        if (techStep == null) {
            throw new ResourceNotFoundException("工序不存在：id=" + id);
        }
        return ApiResponse.success(techStep);
    }

    @GetMapping("/first/{firstLevel}")
    public ApiResponse<List<TechStep>> getByFirstLevel(@PathVariable String firstLevel) {
        return ApiResponse.success(techStepService.getByFirstLevel(firstLevel));
    }

    @GetMapping("/first/{firstLevel}/second/{secondLevel}")
    public ApiResponse<List<TechStep>> getByFirstLevelAndSecondLevel(@PathVariable String firstLevel,
                                                                     @PathVariable String secondLevel) {
        return ApiResponse.success(techStepService.getByFirstLevelAndSecondLevel(firstLevel, secondLevel));
    }

    @GetMapping("/step/{step}")
    public ApiResponse<List<TechStep>> getByStep(@PathVariable String step) {
        return ApiResponse.success(techStepService.getByStep(step));
    }

    @PostMapping("/save")
    public ApiResponse<TechStep> save(@Valid @RequestBody TechStep techStep) {
        return ApiResponse.success(techStepService.save(techStep));
    }

    @PostMapping("/batchSave")
    public ApiResponse<String> batchSave(@Valid @RequestBody List<@Valid TechStep> techStepList) {
        if (techStepList.isEmpty()) {
            throw new IllegalArgumentException("工艺步序为空");
        }
        // 下沉到 Service 单事务批量保存，避免逐条提交产生部分写入
        techStepService.saveBatch(techStepList);
        return ApiResponse.success("工艺步序保存成功");
    }

    // 修改
    @PutMapping("/update")
    public ApiResponse<TechStep> update(@Valid @RequestBody TechStep techStep) {
        if (techStep.getId() == null) {
            throw new IllegalArgumentException("修改操作必须传入 id");
        }
        return ApiResponse.success(techStepService.update(techStep));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        techStepService.deleteById(id);
        return ApiResponse.success();
    }
}
