package com.baogang.info.controller;

import com.baogang.info.common.ApiResponse;
import com.baogang.info.common.PageParam;
import com.baogang.info.common.PageResult;
import com.baogang.info.entity.FlowNode;
import com.baogang.info.service.FlowNodeService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/flowNode")
public class FlowNodeController {

    private final FlowNodeService flowNodeService;

    public FlowNodeController(FlowNodeService flowNodeService) {
        this.flowNodeService = flowNodeService;
    }

    @GetMapping("/list")
    public ApiResponse<PageResult<FlowNode>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        PageParam p = PageParam.of(page, size);
        return ApiResponse.success(flowNodeService.listPaged(p.offset(), p.size()));
    }

    @GetMapping("/{id}")
    public ApiResponse<FlowNode> getById(@PathVariable Long id) {
        return ApiResponse.success(flowNodeService.getById(id));
    }

    @GetMapping("/flowGraph/{flowGraph}")
    public ApiResponse<List<FlowNode>> getByCode(@PathVariable String flowGraph) {
        return ApiResponse.success(flowNodeService.findByFlowGraph(flowGraph));
    }

    @PostMapping("/save")
    public ApiResponse<FlowNode> save(@Valid @RequestBody FlowNode flowNode) {
        return ApiResponse.success(flowNodeService.save(flowNode));
    }

    @PutMapping("/update")
    public ApiResponse<FlowNode> update(@Valid @RequestBody FlowNode flowNode) {
        return ApiResponse.success(flowNodeService.update(flowNode));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        flowNodeService.deleteById(id);
        return ApiResponse.success();
    }

    @GetMapping("/category/{category}")
    public ApiResponse<List<FlowNode>> findByCategory(@PathVariable String category) {
        return ApiResponse.success(flowNodeService.findByCategory(category));
    }
}
