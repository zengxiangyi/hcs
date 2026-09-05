package com.baogang.info.controller;

import com.baogang.info.common.ApiResponse;
import com.baogang.info.common.PageParam;
import com.baogang.info.common.PageResult;
import com.baogang.info.entity.FlowHistory;
import com.baogang.info.exception.ResourceNotFoundException;
import com.baogang.info.service.FlowHistoryService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/flowHistory")
public class FlowHistoryController {

    private final FlowHistoryService flowHistoryService;

    public FlowHistoryController(FlowHistoryService flowHistoryService) {
        this.flowHistoryService = flowHistoryService;
    }

    @GetMapping("/list")
    public ApiResponse<PageResult<FlowHistory>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        PageParam p = PageParam.of(page, size);
        return ApiResponse.success(flowHistoryService.listPaged(p.offset(), p.size()));
    }

    @GetMapping("/{id}")
    public ApiResponse<FlowHistory> getById(@PathVariable Long id) {
        FlowHistory flowHistory = flowHistoryService.getById(id);
        if (flowHistory == null) {
            throw new ResourceNotFoundException("flowHistory not found: " + id);
        }
        return ApiResponse.success(flowHistory);
    }

    @PostMapping("/save")
    public ApiResponse<FlowHistory> save(@Valid @RequestBody FlowHistory flowHistory) {
        return ApiResponse.success(flowHistoryService.save(flowHistory));
    }

    @PutMapping("/update")
    public ApiResponse<FlowHistory> update(@Valid @RequestBody FlowHistory flowHistory) {
        if (flowHistory.getId() == null) {
            throw new IllegalArgumentException("修改操作必须传入 id");
        }
        return ApiResponse.success(flowHistoryService.update(flowHistory.getId(), flowHistory));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        flowHistoryService.deleteById(id);
        return ApiResponse.success();
    }

    @GetMapping("/workflow/{workflow}")
    public ApiResponse<List<FlowHistory>> findByWorkflow(@PathVariable String workflow) {
        return ApiResponse.success(flowHistoryService.findByWorkflow(workflow));
    }

    @GetMapping("/user/{dealUser}")
    public ApiResponse<List<FlowHistory>> findByDealUser(@PathVariable String dealUser) {
        return ApiResponse.success(flowHistoryService.findByDealUser(dealUser));
    }
}
