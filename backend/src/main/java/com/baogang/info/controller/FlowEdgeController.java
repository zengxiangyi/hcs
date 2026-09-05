package com.baogang.info.controller;

import com.baogang.info.common.ApiResponse;
import com.baogang.info.common.PageParam;
import com.baogang.info.common.PageResult;
import com.baogang.info.entity.FlowEdge;
import com.baogang.info.exception.ResourceNotFoundException;
import com.baogang.info.service.FlowEdgeService;
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
@RequestMapping("/flowEdge")
public class FlowEdgeController {

    private final FlowEdgeService flowEdgeService;

    public FlowEdgeController(FlowEdgeService flowEdgeService) {
        this.flowEdgeService = flowEdgeService;
    }

    @GetMapping("/list")
    public ApiResponse<PageResult<FlowEdge>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        PageParam p = PageParam.of(page, size);
        return ApiResponse.success(flowEdgeService.listPaged(p.offset(), p.size()));
    }

    @GetMapping("/{id}")
    public ApiResponse<FlowEdge> getById(@PathVariable Long id) {
        FlowEdge flowEdge = flowEdgeService.getById(id);
        if (flowEdge == null) {
            throw new ResourceNotFoundException("flowEdge not found: " + id);
        }
        return ApiResponse.success(flowEdge);
    }

    @GetMapping("/flowGraph/{flowGraph}")
    public ApiResponse<List<FlowEdge>> getByCode(@PathVariable String flowGraph) {
        return ApiResponse.success(flowEdgeService.findByFlowGraph(flowGraph));
    }

    @PostMapping("/save")
    public ApiResponse<FlowEdge> save(@Valid @RequestBody FlowEdge flowEdge) {
        return ApiResponse.success(flowEdgeService.save(flowEdge));
    }

    @PutMapping("/update")
    public ApiResponse<FlowEdge> update(@Valid @RequestBody FlowEdge flowEdge) {
        return ApiResponse.success(flowEdgeService.update(flowEdge));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        flowEdgeService.deleteById(id);
        return ApiResponse.success();
    }

    @GetMapping("/from/{fromNode}")
    public ApiResponse<List<FlowEdge>> findByFromNode(@PathVariable String fromNode) {
        return ApiResponse.success(flowEdgeService.findByFromNode(fromNode));
    }

    @GetMapping("/to/{toNode}")
    public ApiResponse<List<FlowEdge>> findByToNode(@PathVariable String toNode) {
        return ApiResponse.success(flowEdgeService.findByToNode(toNode));
    }
}
