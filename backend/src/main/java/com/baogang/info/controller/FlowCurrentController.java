package com.baogang.info.controller;

import com.baogang.info.common.ApiResponse;
import com.baogang.info.common.PageParam;
import com.baogang.info.common.PageResult;
import com.baogang.info.entity.FlowCurrent;
import com.baogang.info.exception.ResourceNotFoundException;
import com.baogang.info.service.FlowCurrentService;
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
@RequestMapping("/flowCurrent")
public class FlowCurrentController {

    private final FlowCurrentService flowCurrentService;

    public FlowCurrentController(FlowCurrentService flowCurrentService) {
        this.flowCurrentService = flowCurrentService;
    }

    @GetMapping("/list")
    public ApiResponse<PageResult<FlowCurrent>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        PageParam p = PageParam.of(page, size);
        return ApiResponse.success(flowCurrentService.listPaged(p.offset(), p.size()));
    }

    @GetMapping("/{id}")
    public ApiResponse<FlowCurrent> getById(@PathVariable Long id) {
        FlowCurrent flowCurrent = flowCurrentService.getById(id);
        if (flowCurrent == null) {
            throw new ResourceNotFoundException("flowCurrent not found: " + id);
        }
        return ApiResponse.success(flowCurrent);
    }

    @PostMapping("/save")
    public ApiResponse<FlowCurrent> save(@Valid @RequestBody FlowCurrent flowCurrent) {
        return ApiResponse.success(flowCurrentService.save(flowCurrent));
    }

    @PutMapping("/{id}")
    public ApiResponse<FlowCurrent> update(@PathVariable Long id, @Valid @RequestBody FlowCurrent flowCurrent) {
        return ApiResponse.success(flowCurrentService.update(id, flowCurrent));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        flowCurrentService.deleteById(id);
        return ApiResponse.success();
    }

    @GetMapping("/workflow/{workflow}")
    public ApiResponse<List<FlowCurrent>> findByWorkflow(@PathVariable String workflow) {
        return ApiResponse.success(flowCurrentService.findByWorkflow(workflow));
    }

    @GetMapping("/node/{flowNode}")
    public ApiResponse<List<FlowCurrent>> findByFlowNode(@PathVariable String flowNode) {
        return ApiResponse.success(flowCurrentService.findByFlowNode(flowNode));
    }
}
